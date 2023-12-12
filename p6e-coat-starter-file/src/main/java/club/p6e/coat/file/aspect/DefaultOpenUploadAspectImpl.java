package club.p6e.coat.file.aspect;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 打开分片上传-切面（钩子）
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
public class DefaultOpenUploadAspectImpl implements OpenUploadAspect {

    @Override
    public int order() {
        return 0;
    }

    @Override
    public Mono<Boolean> before(Map<String, Object> data) {
        return Mono.just(true);
    }

    @Override
    public Mono<Boolean> after(Map<String, Object> data, Map<String, Object> result) {
        // 对返回的结果数据进行处理
        // 从而屏蔽一些不想给前端用户显示的数据
        final Object id = result.get("id");
        result.clear();
        result.put("id", id);
        return Mono.just(true);
    }

}
