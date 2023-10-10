package club.p6e.coat.file.service.impl;

import club.p6e.coat.file.service.SimpleUploadService;
import club.p6e.coat.file.context.SimpleUploadContext;
import club.p6e.coat.file.error.FileException;
import club.p6e.coat.file.error.ParameterException;
import club.p6e.coat.file.FolderStorageLocationPathService;
import club.p6e.coat.file.Properties;
import club.p6e.coat.file.model.UploadModel;
import club.p6e.coat.file.utils.FileUtil;
import club.p6e.coat.file.repository.UploadRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.File;
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
     * 上传文件夹本地存储路径服务对象
     */
    private final FolderStorageLocationPathService folderPathService;

    /**
     * 构造方法初始化
     *
     * @param properties        配置文件对象
     * @param repository        上传存储库对象
     * @param folderPathService 上传文件夹本地存储路径服务对象
     */
    public SimpleUploadServiceImpl(Properties properties,
                                   UploadRepository repository,
                                   FolderStorageLocationPathService folderPathService) {
        this.properties = properties;
        this.repository = repository;
        this.folderPathService = folderPathService;
    }

    @Override
    public Mono<Map<String, Object>> execute(SimpleUploadContext context) {
        // 读取并清除文件对象
        final FilePart filePart = context.getFilePart();
        context.setFilePart(null);
        final UploadModel model = new UploadModel();
        final String path = folderPathService.path();
        final String name = FileUtil.name(filePart.filename());
        if (name == null) {
            return Mono.error(new ParameterException(
                    this.getClass(),
                    "fun execute(SimpleUploadContext context)." +
                            "-> <name> Request parameter format error.",
                    "<name> Request parameter format error")
            );
        }
        final String absolutePath = FileUtil.convertAbsolutePath(
                FileUtil.composePath(properties.getSimpleUpload().getPath(), path));
        final Object operator = context.get("operator");
        if (operator instanceof final String content) {
            model.setOwner(content);
            model.setOperator(content);
        }
        model.setName(name);
        model.setSource(SOURCE);
        model.setStorageLocation(path);
        return repository
                .create(model)
                .map(m -> {
                    // 如果创建数据成功就创建文件夹
                    FileUtil.createFolder(absolutePath);
                    return m;
                })
                .flatMap(m -> {
                    final File file = new File(FileUtil.composePath(absolutePath, filePart.filename()));
                    return filePart
                            .transferTo(file)
                            .then(Mono.just(file))
                            .flatMap(f -> {
                                final long size = properties.getSimpleUpload().getMaxSize();
                                if (f.length() > size) {
                                    // 如果文件长度超过了最大的限制,那么就删除文件
                                    FileUtil.deleteFile(f);
                                    return Mono.error(new FileException(this.getClass(),
                                            "fun execute() -> File ("
                                                    + f.getName() + ") upload exceeds the maximum length limit",
                                            "File (" + f.getName() + ") upload exceeds the maximum length limit")
                                    );
                                }
                                return Mono.just(f.length())
                                        .flatMap(l -> repository.closeLock(m.getId()))
                                        .flatMap(l -> repository.update(m.setSize(f.length())))
                                        .flatMap(l -> repository.findById(m.getId()));
                            });
                })
                .map(UploadModel::toMap);
    }

}
