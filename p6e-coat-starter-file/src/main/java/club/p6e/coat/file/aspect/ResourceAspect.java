package club.p6e.coat.file.aspect;

import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 资源查看-切面（钩子）
 *
 * @author lidashuang
 * @version 1.0
 */
public interface ResourceAspect extends Aspect {

    public Mono<Boolean> after(Map<String, Object> data);

    @Override
    default Mono<Boolean> after(Map<String, Object> data, Map<String, Object> result) {
        return after(data);
    }

}
