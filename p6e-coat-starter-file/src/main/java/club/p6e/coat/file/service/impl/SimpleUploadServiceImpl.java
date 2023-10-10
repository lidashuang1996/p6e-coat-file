package club.p6e.coat.file.service.impl;

import club.p6e.coat.file.FileReadWriteService;
import club.p6e.coat.file.service.SimpleUploadService;
import club.p6e.coat.file.context.SimpleUploadContext;
import club.p6e.coat.file.error.ParameterException;
import club.p6e.coat.file.model.UploadModel;
import club.p6e.coat.file.utils.FileUtil;
import club.p6e.coat.file.repository.UploadRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 简单（小文件）上传服务
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = SimpleUploadService.class,
        ignored = SimpleUploadServiceImpl.class
)
public class SimpleUploadServiceImpl implements SimpleUploadService {

    /**
     * 源
     */
    private static final String SOURCE = "SIMPLE_UPLOAD";

    /**
     * 上传存储库对象
     */
    private final UploadRepository repository;

    /**
     * 文件读取写入服务对象
     */
    private final FileReadWriteService fileReadWriteService;

    /**
     * 构造方法初始化
     *
     * @param repository           上传存储库对象
     * @param fileReadWriteService 文件读取写入服务对象
     */
    public SimpleUploadServiceImpl(
            UploadRepository repository,
            FileReadWriteService fileReadWriteService
    ) {
        this.repository = repository;
        this.fileReadWriteService = fileReadWriteService;
    }

    @Override
    public Mono<Map<String, Object>> execute(SimpleUploadContext context) {
        // 读取并清除文件对象
        final FilePart filePart = context.getFilePart();
        context.setFilePart(null);
        final String name = FileUtil.name(filePart.filename());
        if (name == null) {
            return Mono.error(new ParameterException(
                    this.getClass(),
                    "fun execute(SimpleUploadContext context). " +
                            "-> <name> Request parameter format error.",
                    "<name> Request parameter format error")
            );
        }
        final UploadModel model = new UploadModel();
        final Object operator = context.get("operator");
        if (operator instanceof final String content) {
            model.setOwner(content);
            model.setOperator(content);
        }
        model.setName(name);
        model.setSource(SOURCE);
        return repository
                .create(model)
                .flatMap(m -> fileReadWriteService.write(name, context.toMap(),
                        file -> filePart.transferTo(file).then(Mono.just(file))))
                .map(m -> {
                    model.setSize(m.getLength());
                    model.setStorageType(m.getType());
                    model.setStorageLocation(m.getPath());
                    return model;
                })
                .flatMap(m -> Mono.just(m)
                        .flatMap(l -> repository.closeLock(m.getId()))
                        .flatMap(l -> repository.update(m))
                        .flatMap(l -> repository.findById(m.getId())))
                .map(UploadModel::toMap);
    }

}
