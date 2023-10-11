package club.p6e.coat.file.aspect;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 打开上传操作的切面（钩子）的默认实现
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = OpenUploadAspect.class,
        ignored = DefaultOpenUploadAspectImpl.class
)
public class DefaultOpenUploadAspectImpl implements OpenUploadAspect {

    @Override
    public Mono<Boolean> before(Map<String, Object> data) {
        return Mono.just(true);
    }

    @Override
    public Mono<Boolean> after(Map<String, Object> data, Map<String, Object> result) {
        // 对返回的结果数据进行处理
        final Object id = result.get("id");
        result.clear();
        result.put("id", id);
        return Mono.just(true);
    }

}
