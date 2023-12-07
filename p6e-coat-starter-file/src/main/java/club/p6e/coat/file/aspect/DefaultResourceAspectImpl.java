package club.p6e.coat.file.aspect;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 资源查看-切面（钩子）
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = ResourceAspect.class,
        ignored = DefaultResourceAspectImpl.class
)
public class DefaultResourceAspectImpl implements ResourceAspect {

    @Override
    public Mono<Boolean> before(Map<String, Object> data) {
        return Mono.just(true);
    }

    @Override
    public Mono<Boolean> after(Map<String, Object> data) {
        return Mono.just(true);
    }

}
