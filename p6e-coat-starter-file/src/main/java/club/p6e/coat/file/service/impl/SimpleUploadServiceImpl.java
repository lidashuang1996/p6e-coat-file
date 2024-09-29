package club.p6e.coat.file.service.impl;

import club.p6e.coat.common.error.ResourceException;
import club.p6e.coat.file.FileReadWriteService;
import club.p6e.coat.file.Properties;
import club.p6e.coat.file.actuator.FileWriteActuator;
import club.p6e.coat.common.error.ResourceNodeException;
import club.p6e.coat.file.service.SimpleUploadService;
import club.p6e.coat.file.context.SimpleUploadContext;
import club.p6e.coat.common.error.ParameterException;
import club.p6e.coat.file.model.UploadModel;
import club.p6e.coat.file.utils.FileUtil;
import club.p6e.coat.file.repository.UploadRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.HashMap;
import java.util.List;
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
     * @param properties           配置文件对象
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
                    "fun execute(SimpleUploadContext context). ==> " +
                            "execute(...) unable to find corresponding resource context node.",
                    "execute(...) unable to find corresponding resource context node.")
            );
        }
        final List<String> nodes = context.get("$node") == null
                ? List.of() : List.of(context.get("$node").toString().split(","));
        if (nodes.contains(context.getNode())) {
            return Mono.error(new ResourceException(
                    this.getClass(),
                    "fun execute(SimpleUploadContext context). ==> " +
                            "execute(...) exception without permission for this node.",
                    "execute(...) exception without permission for this node.")
            );
        }
        final FilePart filePart = context.getFilePart();
        context.setFilePart(null);
        final String name = FileUtil.name(filePart.filename());
        if (name == null) {
            return Mono.error(new ParameterException(
                    this.getClass(),
                    "fun execute(SimpleUploadContext context). ==> " +
                            "execute(...) request parameter <name> exception.",
                    "execute(...) request parameter <name> exception.")
            );
        }
        final UploadModel pum = new UploadModel();
        final Object operator = context.get("$operator");
        if (operator instanceof final String content) {
            pum.setOwner(content);
            pum.setCreator(content);
            pum.setModifier(content);
        }
        pum.setName(name);
        pum.setSource(SOURCE);
        return repository
                .create(pum)
                .flatMap(m -> fileReadWriteService.write(name, new HashMap<>() {{
                    putAll(context);
                    putAll(upload.getExtend());
                }}, new CustomFileWriteActuator(filePart, upload)).map(fam -> {
                    final UploadModel rum = new UploadModel();
                    rum.setId(m.getId());
                    rum.setSize(fam.getLength());
                    rum.setStorageType(fam.getType());
                    rum.setStorageLocation(fam.getPath());
                    return rum;
                })).flatMap(m -> Mono.just(m)
                        .flatMap(l -> repository.closeLock(m.getId()))
                        .flatMap(l -> repository.update(m))
                        .flatMap(l -> repository.findById(m.getId()))
                ).map(UploadModel::toMap);
    }

    /**
     * 自定义的文件写入执行器
     *
     * @param filePart   文件写入对象
     * @param properties 上传配置对象
     */
    private record CustomFileWriteActuator(
            FilePart filePart,
            Properties.Upload properties
    ) implements FileWriteActuator {

        @Override
        public String type() {
            return properties.getType();
        }

        @Override
        public String path() {
            return properties.getPath();
        }

        @Override
        public Mono<File> execute(File file) {
            return filePart.transferTo(file).then(Mono.just(file));
        }

    }

}
