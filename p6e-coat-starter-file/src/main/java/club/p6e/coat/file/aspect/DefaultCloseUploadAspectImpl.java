package club.p6e.coat.file.aspect;

import club.p6e.coat.file.utils.FileUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 切面（钩子）的默认实现
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = CloseUploadAspect.class,
        ignored = DefaultCloseUploadAspectImpl.class
)
public class DefaultCloseUploadAspectImpl implements CloseUploadAspect {

    @Override
    public Mono<Boolean> before(Map<String, Object> data) {
        return Mono.just(true);
    }

    @Override
    public Mono<Boolean> after(Map<String, Object> data, Map<String, Object> result) {
        // 对返回的结果数据进行处理
        final Object id = result.get("id");
        final Object size = result.get("size");
        final String name = String.valueOf(result.get("name"));
        final String storage = String.valueOf(result.get("storageLocation"));
        final String path = FileUtil.composePath(storage, name);
        result.clear();
        result.put("id", id);
        result.put("size", size);
        result.put("name", name);
        result.put("path", path);
        return Mono.just(true);
    }

}
