package club.p6e.coat.file.service.impl;

import club.p6e.coat.file.FileReadWriteService;
import club.p6e.coat.file.error.FileException;
import club.p6e.coat.file.handler.DownloadHandlerFunction;
import club.p6e.coat.file.service.DownloadService;
import club.p6e.coat.file.context.DownloadContext;
import club.p6e.coat.file.error.DownloadNodeException;
import club.p6e.coat.file.Properties;
import club.p6e.coat.file.utils.FileUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    private final FileReadWriteService fileReadWriteService;

    /**
     * 构造方法初始化
     *
     * @param properties           配置文件对象
     * @param fileReadWriteService 文件读写服务对象
     */
    public DownloadServiceImpl(Properties properties, FileReadWriteService fileReadWriteService) {
        this.properties = properties;
        this.fileReadWriteService = fileReadWriteService;
    }

    @Override
    public Mono<FileReadWriteService.FileReadActuator> execute(DownloadContext context) {
        final Properties.Download download = properties.getDownloads().get(context.getNode());
        if (download == null) {
            return Mono.error(new DownloadNodeException(
                    this.getClass(),
                    "fun execute(DownloadContext context). " +
                            "-> Unable to find corresponding download node.",
                    "Unable to find corresponding download node")
            );
        } else {
            final Map<String, Object> data = new HashMap<>() {{
                putAll(download.getExtend());
            }};
            return fileReadWriteService.read(download.getType(),
                    download.getPath(), context.getPath(), MediaType.APPLICATION_OCTET_STREAM, data);
        }
    }
}
