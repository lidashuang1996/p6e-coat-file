package club.p6e.coat.file.service.impl;

import club.p6e.coat.file.FileReadWriteService;
import club.p6e.coat.file.Properties;
import club.p6e.coat.file.actuator.FileWriteActuator;
import club.p6e.coat.file.error.ResourceNodeException;
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

import java.io.File;
import java.util.HashMap;
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
     * 配置文件对象
     */
    private final Properties properties;

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
            Properties properties,
            UploadRepository repository,
            FileReadWriteService fileReadWriteService
    ) {
        this.properties = properties;
        this.repository = repository;
        this.fileReadWriteService = fileReadWriteService;
    }

    @Override
    public Mono<Map<String, Object>> execute(SimpleUploadContext context) {
        final Properties.Upload upload = properties.getUploads().get(context.getNode());
        if (upload == null) {
            return Mono.error(new ResourceNodeException(
                    this.getClass(),
                    "fun execute(SimpleUploadContext context). " +
                            "-> Unable to find corresponding resource context node.",
                    "Unable to find corresponding resource context node")
            );
        }
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
        final UploadModel createUploadModel = new UploadModel();
        final Object operator = context.get("operator");
        if (operator instanceof final String content) {
            createUploadModel.setOwner(content);
            createUploadModel.setOperator(content);
        }
        createUploadModel.setName(name);
        createUploadModel.setSource(SOURCE);
        final Map<String, Object> extend = new HashMap<>() {{
            putAll(upload.getExtend());
            putAll(context);
        }};
        return repository
                .create(createUploadModel)
                .flatMap(m -> fileReadWriteService
                        .write(name, extend, new FileWriteActuator() {
                            @Override
                            public String type() {
                                return upload.getType();
                            }

                            @Override
                            public String path() {
                                return upload.getPath();
                            }

                            @Override
                            public Mono<File> execute(File file) {
                                return filePart.transferTo(file).then(Mono.just(file));
                            }
                        })
                        .map(fam -> {
                            final UploadModel updateUploadModel = new UploadModel();
                            updateUploadModel.setId(m.getId());
                            updateUploadModel.setSize(fam.getLength());
                            updateUploadModel.setStorageType(fam.getType());
                            updateUploadModel.setStorageLocation(fam.getPath());
                            return updateUploadModel;
                        }))
                .flatMap(m -> Mono.just(m)
                        .flatMap(l -> repository.closeLock(m.getId()))
                        .flatMap(l -> repository.update(m))
                        .flatMap(l -> repository.findById(m.getId())))
                .map(UploadModel::toMap);
    }

}
