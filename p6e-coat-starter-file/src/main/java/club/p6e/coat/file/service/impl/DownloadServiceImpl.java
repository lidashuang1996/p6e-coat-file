package club.p6e.coat.file.service.impl;

import club.p6e.coat.file.actuator.FileReadActuator;
import club.p6e.coat.file.FileReadWriteService;
import club.p6e.coat.file.service.DownloadService;
import club.p6e.coat.file.context.DownloadContext;
import club.p6e.coat.file.error.DownloadNodeException;
import club.p6e.coat.file.Properties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * 下载服务
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
     * 构造方法初始化
     *
     * @param properties           配置文件对象
     * @param fileReadWriteService 文件读写服务对象
     */
    public DownloadServiceImpl(
            Properties properties,
            FileReadWriteService fileReadWriteService
    ) {
        this.properties = properties;
        this.fileReadWriteService = fileReadWriteService;
    }

    @Override
    public Mono<FileReadActuator> execute(DownloadContext context) {
        final Properties.Download download = properties.getDownloads().get(context.getNode());
        if (download == null) {
            return Mono.error(new DownloadNodeException(
                    this.getClass(),
                    "fun execute(DownloadContext context). " +
                            "-> Unable to find corresponding download node.",
                    "Unable to find corresponding download node")
            );
        } else {
            final Map<String, Object> extend = new HashMap<>() {{
                putAll(download.getExtend());
                putAll(context);
            }};
            return fileReadWriteService.read(download.getType(),
                    download.getPath(), context.getPath(), MediaType.APPLICATION_OCTET_STREAM, extend);
        }
    }
}
