package club.p6e.coat.file.service.impl;

import club.p6e.coat.file.service.OpenUploadService;
import club.p6e.coat.file.context.OpenUploadContext;
import club.p6e.coat.file.FolderStorageLocationPathService;
import club.p6e.coat.file.Properties;
import club.p6e.coat.file.model.UploadModel;
import club.p6e.coat.file.repository.UploadRepository;
import club.p6e.coat.file.utils.FileUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 分片上传服务
 * 步骤1: 打开上传操作
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = OpenUploadService.class,
        ignored = OpenUploadServiceImpl.class
)
public class OpenUploadServiceImpl implements OpenUploadService {

    /**
     * 源
     */
    private static final String SOURCE = "SLICE_UPLOAD";

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
    public OpenUploadServiceImpl(Properties properties,
                                 UploadRepository repository,
                                 FolderStorageLocationPathService folderPathService) {
        this.properties = properties;
        this.repository = repository;
        this.folderPathService = folderPathService;
    }

    @Override
    public Mono<Map<String, Object>> execute(OpenUploadContext context) {
        final UploadModel model = new UploadModel();
        final Object operator = context.get("operator");
        if (operator instanceof final String content) {
            model.setOwner(content);
            model.setOperator(content);
        }
        final String path = folderPathService.path();
        final String name = FileUtil.name(context.getName());
        final String absolutePath = FileUtil.convertAbsolutePath(
                FileUtil.composePath(properties.getSliceUpload().getPath(), path));
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
                .map(UploadModel::toMap);
    }

}
