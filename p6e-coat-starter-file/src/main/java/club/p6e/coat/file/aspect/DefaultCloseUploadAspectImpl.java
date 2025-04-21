package club.p6e.coat.file.aspect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 关闭分片上传-切面（钩子）
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
public class DefaultCloseUploadAspectImpl implements CloseUploadAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCloseUploadAspectImpl.class);

    @Override
    public int order() {
        return 0;
    }

    @Override
    public Mono<Boolean> before(Map<String, Object> data) {
        LOGGER.info("DefaultCloseUploadAspectImpl.before() >>>>> {}", data);
        return Mono.just(true);
    }

    @Override
    public Mono<Boolean> after(Map<String, Object> data, Map<String, Object> result) {
        // 对返回的结果数据进行处理
        // 从而屏蔽一些不想给前端用户显示的数据
        final String storage = String.valueOf(result.get("storageLocation"));
        final String name = String.valueOf(result.get("name"));
        final Object size = result.get("size");
        final Object id = result.get("id");
        result.clear();
        result.put("id", id);
        result.put("size", size);
        result.put("name", name);
        result.put("path", storage);
        LOGGER.info("close upload aspect result: {}", result);
        return Mono.just(true);
    }

}
