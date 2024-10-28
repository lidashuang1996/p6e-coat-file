package club.p6e.coat.file.service.impl;

import club.p6e.coat.common.error.ResourceException;
import club.p6e.coat.file.FilePermissionService;
import club.p6e.coat.file.actuator.FileReadActuator;
import club.p6e.coat.file.FileReadWriteService;
import club.p6e.coat.file.service.DownloadService;
import club.p6e.coat.file.context.DownloadContext;
import club.p6e.coat.common.error.DownloadNodeException;
import club.p6e.coat.file.Properties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;

/**
 * 下载文件服务
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = DownloadService.class,
        ignored = DownloadServiceImpl.class
)
public class DownloadServiceImpl implements DownloadService {

    /**
     * 配置文件对象
     */
    private final Properties properties;

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
     * @param fileReadWriteService  文件读写服务对象
     * @param filePermissionService 文件权限服务对象
     */
    public DownloadServiceImpl(
            Properties properties,
            FileReadWriteService fileReadWriteService,
            FilePermissionService filePermissionService
    ) {
        this.properties = properties;
        this.fileReadWriteService = fileReadWriteService;
        this.filePermissionService = filePermissionService;
    }

    @Override
    public Mono<FileReadActuator> execute(DownloadContext context) {
        final Properties.Download download = properties.getDownloads().get(context.getNode());
        if (download == null) {
            return Mono.error(new DownloadNodeException(
                    this.getClass(),
                    "fun execute(DownloadContext context). ==> " +
                            "execute(...) unable to find corresponding resource context node.",
                    "execute(...) unable to find corresponding resource context node.")
            );
        } else {
            return filePermissionService
                    .execute("D", context)
                    .flatMap(b -> {
                        if (b) {
                            return fileReadWriteService.read(
                                    download.getType(),
                                    download.getPath(),
                                    context.getPath(),
                                    MediaType.APPLICATION_OCTET_STREAM,
                                    new HashMap<>() {{
                                        putAll(context);
                                        putAll(download.getExtend());
                                    }}
                            );
                        } else {
                            return Mono.error(new ResourceException(
                                    this.getClass(),
                                    "fun execute(DownloadContext context). ==> " +
                                            "execute(...) exception without permission for this node.",
                                    "execute(...) exception without permission for this node.")
                            );
                        }
                    });
        }
    }
}
