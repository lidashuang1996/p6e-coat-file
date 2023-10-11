package club.p6e.coat.file.service.impl;

import club.p6e.coat.file.FileReadWriteService;
import club.p6e.coat.file.model.UploadModel;
import club.p6e.coat.file.service.CloseUploadService;
import club.p6e.coat.file.context.CloseUploadContext;
import club.p6e.coat.file.Properties;
import club.p6e.coat.file.repository.UploadRepository;
import club.p6e.coat.file.utils.FileUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

/**
 * 分片上传服务
 * 步骤3: 关闭上传操作
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = CloseUploadService.class,
        ignored = CloseUploadServiceImpl.class
)
public class CloseUploadServiceImpl implements CloseUploadService {

    /**
     * 配置文件对象
     */
    private final Properties properties;

    /**
     * 上传存储库对象
     */
    private final UploadRepository repository;

    /**
     * 文件读写服务对象
     */
    private final FileReadWriteService fileReadWriteService;

    /**
     * 构造方法初始化
     *
     * @param properties           配置文件对象
     * @param repository           上传存储库对象
     * @param fileReadWriteService 文件读写服务对象
     */
    public CloseUploadServiceImpl(
            Properties properties,
            UploadRepository repository,
            FileReadWriteService fileReadWriteService
    ) {
        this.fileReadWriteService = fileReadWriteService;
        this.repository = repository;
        this.properties = properties;
    }

    @Override
    public Mono<Map<String, Object>> execute(CloseUploadContext context) {
        return repository
                .closeLock(context.getId())
                .flatMap(l -> repository.findById(context.getId()))
                .flatMap(m -> {
                    System.out.println(">>> context >> " + context.toMap());
                    final Object operator = context.get("operator");
                    if (operator instanceof final String content) {
                        m.setOperator(content);
                    }
                    final String absolutePath = FileUtil.convertAbsolutePath(
                            FileUtil.composePath(properties.getSliceUpload().getPath(), String.valueOf(m.getId())));
                    final File[] files = FileUtil.readFolder(absolutePath);
                    for (int i = 0; i < files.length; i++) {
                        for (int j = i; j < files.length; j++) {
                            final String in = files[i].getName();
                            final String jn = files[j].getName();
                            final int iw = Integer.parseInt(in.substring(0, in.indexOf("_")));
                            final int jw = Integer.parseInt(jn.substring(0, jn.indexOf("_")));
                            if (iw > jw) {
                                final File v = files[j];
                                files[j] = files[i];
                                files[i] = v;
                            }
                        }
                    }
                    System.out.println("文件合并的顺序是: " + Arrays.toString(files));
                    return fileReadWriteService
                            .write(m.getName(), context.toMap(), file -> FileUtil.mergeFileSlice(files, file))
                            .flatMap(fm -> repository
                                    .update(new UploadModel()
                                            .setId(m.getId())
                                            .setSize(fm.getLength())
                                            .setStorageType(fm.getType())
                                            .setStorageLocation(fm.getPath())
                                    ))
                            .flatMap(l -> repository.findById(m.getId()))
                            .map(UploadModel::toMap);
                });
    }

}
