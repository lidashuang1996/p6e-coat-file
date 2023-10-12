package club.p6e.coat.file.service.impl;

import club.p6e.coat.file.actuator.FileReadActuator;
import club.p6e.coat.file.FileReadWriteService;
import club.p6e.coat.file.service.ResourceService;
import club.p6e.coat.file.context.ResourceContext;
import club.p6e.coat.file.error.ResourceNodeException;
import club.p6e.coat.file.Properties;
import club.p6e.coat.file.utils.FileUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * 资源查看服务
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = ResourceService.class,
        ignored = ResourceServiceImpl.class
)
public class ResourceServiceImpl implements ResourceService {

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
    public ResourceServiceImpl(Properties properties, FileReadWriteService fileReadWriteService) {
        this.properties = properties;
        this.fileReadWriteService = fileReadWriteService;
    }

    @Override
    public Mono<FileReadActuator> execute(ResourceContext context) {
        final Properties.Resource resource = properties.getResources().get(context.getNode());
        if (resource == null) {
            return Mono.error(new ResourceNodeException(
                    this.getClass(),
                    "fun execute(ResourceContext context). " +
                            "-> Unable to find corresponding resource context node.",
                    "Unable to find corresponding resource context node")
            );
        } else {
            final String path = context.getPath();
            final String suffix = FileUtil.getSuffix(path);
            final Map<String, MediaType> suffixes = resource.getSuffixes();
            if (suffixes.get(suffix) != null) {
                final MediaType mediaType = suffixes.get(suffix);
                final Map<String, Object> extend = new HashMap<>() {{
                    putAll(resource.getExtend());
                    putAll(context);
                }};
                return fileReadWriteService.read(resource.getType(), resource.getPath(), path, mediaType, extend);
            } else {
                return Mono.error(new ResourceNodeException(
                        this.getClass(),
                        "fun execute(ResourceContext context). " +
                                "-> The media resource corresponding to the resource node does not exist.",
                        "The media resource corresponding to the resource node does not exist")
                );
            }
        }
    }

}
