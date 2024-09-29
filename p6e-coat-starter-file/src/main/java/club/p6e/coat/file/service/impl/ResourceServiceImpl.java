package club.p6e.coat.file.service.impl;

import club.p6e.coat.common.error.ResourceException;
import club.p6e.coat.file.actuator.FileReadActuator;
import club.p6e.coat.file.FileReadWriteService;
import club.p6e.coat.file.service.ResourceService;
import club.p6e.coat.file.context.ResourceContext;
import club.p6e.coat.common.error.ResourceNodeException;
import club.p6e.coat.file.Properties;
import club.p6e.coat.file.utils.FileUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
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
                    "fun execute(ResourceContext context). ==> " +
                            "execute(...) unable to find corresponding resource context node.",
                    "execute(...) unable to find corresponding resource context node.")
            );
        } else {
            final List<String> nodes = context.get("$node") == null
                    ? List.of() : List.of(context.get("$node").toString().split(","));
            if (nodes.contains(context.getNode())) {
                return Mono.error(new ResourceException(
                        this.getClass(),
                        "fun execute(ResourceContext context). ==> " +
                                "execute(...) exception without permission for this node.",
                        "execute(...) exception without permission for this node.")
                );
            } else {
                final String path = context.getPath();
                final String suffix = FileUtil.getSuffix(path);
                final Map<String, MediaType> suffixes = resource.getSuffixes();
                if (suffixes.get(suffix) != null) {
                    final MediaType mediaType = suffixes.get(suffix);
                    return fileReadWriteService.read(
                            resource.getType(),
                            resource.getPath(),
                            path,
                            mediaType,
                            new HashMap<>() {{
                                putAll(context);
                                putAll(resource.getExtend());
                            }}
                    );
                } else {
                    return Mono.error(new ResourceException(
                            this.getClass(),
                            "fun execute(ResourceContext context). ==> " +
                                    "execute(...) the media resource corresponding to the resource node does not exist.",
                            "execute(...) the media resource corresponding to the resource node does not exist")
                    );
                }
            }
        }
    }

}
