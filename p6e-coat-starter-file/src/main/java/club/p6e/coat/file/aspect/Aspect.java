package club.p6e.coat.file.aspect;

import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 切面（钩子）
 *
 * @author lidashuang
 * @version 1.0
 */
public interface Aspect {

    /**
     * 在操作之前
     *
     * @param data 参数对象
     * @return 是否继续执行
     */
    public Mono<Boolean> before(Map<String, Object> data);

    /**
     * 在操作之后
     *
     * @param data   参数对象
     * @param result 结果对象
     * @return 是否继续执行
     */
    public Mono<Boolean> after(Map<String, Object> data, Map<String, Object> result);

}
