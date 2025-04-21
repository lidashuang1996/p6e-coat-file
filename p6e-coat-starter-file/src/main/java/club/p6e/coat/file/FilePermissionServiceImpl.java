package club.p6e.coat.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * 文件权限服务
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = FilePermissionService.class,
        ignored = FilePermissionServiceImpl.class
)
public class FilePermissionServiceImpl implements FilePermissionService {

    private static final List<String> T = List.of("R", "U", "D");

    private static final Logger LOGGER = LoggerFactory.getLogger(FilePermissionServiceImpl.class);

    @Override
    public Mono<Boolean> execute(String type, Map<String, Object> context) {
        LOGGER.info("FilePermissionServiceImpl >>>> {} ::: {}", type, context);
        final List<String> nodes = context.get("$node") == null
                ? List.of() : List.of(String.valueOf(context.get("$node")).split(","));
        if (type != null && T.contains(type) && context.get("node") instanceof String node) {
            for (final String item : nodes) {
                if (item.startsWith(node)) {
                    final int index = item.lastIndexOf(":");
                    if (index > 0) {
                        return Mono.just(item.substring(index + 1).toUpperCase().contains(type));
                    }
                }
            }
        }
        return Mono.just(false);
    }

}
