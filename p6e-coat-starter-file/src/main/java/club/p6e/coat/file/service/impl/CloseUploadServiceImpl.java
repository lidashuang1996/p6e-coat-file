package club.p6e.coat.file.service.impl;

import club.p6e.coat.common.error.ResourceException;
import club.p6e.coat.file.FilePermissionService;
import club.p6e.coat.file.FileReadWriteService;
import club.p6e.coat.file.actuator.FileWriteActuator;
import club.p6e.coat.common.error.ResourceNodeException;
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
import java.util.HashMap;
import java.util.Map;

/**
 * 分片上传服务
 * 步骤3: 关闭分片上传操作
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
     * 文件权限服务对象
     */
    private final FilePermissionService filePermissionService;

    /**
     * 构造方法初始化
     *
     * @param properties            配置文件对象
     * @param repository            上传存储库对象
     * @param fileReadWriteService  文件读写服务对象
     * @param filePermissionService 文件权限服务对象
     */
    public CloseUploadServiceImpl(
            Properties properties,
            UploadRepository repository,
            FileReadWriteService fileReadWriteService,
            FilePermissionService filePermissionService
    ) {
        this.properties = properties;
        this.repository = repository;
        this.fileReadWriteService = fileReadWriteService;
        this.filePermissionService = filePermissionService;
    }

    @Override
    public Mono<Map<String, Object>> execute(CloseUploadContext context) {
        final Properties.Upload upload = properties.getUploads().get(context.getNode());
        if (upload == null) {
            return Mono.error(new ResourceNodeException(
                    this.getClass(),
                    "fun execute(CloseUploadContext context). ==> " +
                            "execute(...) unable to find corresponding resource context node.",
                    "execute(...) unable to find corresponding resource context node.")
            );
        }
        return filePermissionService
                .execute("U", context)
                .flatMap(b -> {
                    if (b) {
                        return repository
                                .closeLock(context.getId())
                                .flatMap(l -> repository.findById(context.getId()))
                                .flatMap(m -> {
                                    final Object operator = context.get("$operator");
                                    if (operator instanceof final String content) {
                                        m.setModifier(content);
                                    }
                                    // 文件夹绝对路径
                                    final String absolutePath = FileUtil.convertAbsolutePath(
                                            FileUtil.composePath(properties.getSliceUpload().getPath(), String.valueOf(m.getId()))
                                    );
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
                                    return fileReadWriteService
                                            .write(m.getName(), new HashMap<>() {{
                                                putAll(context);
                                                putAll(upload.getExtend());
                                            }}, new CustomFileWriteActuator(files, upload))
                                            .flatMap(fm -> repository.update(new UploadModel().setId(m.getId()).setSize(fm.getLength()).setStorageType(fm.getType()).setStorageLocation(fm.getPath())))
                                            .flatMap(rl -> repository.findById(m.getId()))
                                            .map(UploadModel::toMap);
                                });
                    } else {
                        return Mono.error(new ResourceException(
                                this.getClass(),
                                "fun execute(CloseUploadContext context). ==> " +
                                        "execute(...) exception without permission for this node.",
                                "execute(...) exception without permission for this node.")
                        );
                    }
                });
    }

    /**
     * 自定义的文件写入执行器
     *
     * @param files      文件列表
     * @param properties 上传配置对象
     */
    private record CustomFileWriteActuator(
            File[] files,
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
            return FileUtil.mergeFileSlice(files, file);
        }

    }

}
