package club.p6e.coat.file.aspect;

import club.p6e.coat.file.utils.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 简单（小文件）上传-切面（钩子）
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
public class DefaultSimpleUploadAspectImpl implements SimpleUploadAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSimpleUploadAspectImpl.class);

    @Override
    public int order() {
        return 0;
    }

    @Override
    public Mono<Boolean> before(Map<String, Object> data) {
        LOGGER.info("DefaultSimpleUploadAspectImpl.before() >>>>> {}", data);
        return Mono.just(true);
    }

    @Override
    public Mono<Boolean> after(Map<String, Object> data, Map<String, Object> result) {
        // 对返回的结果数据进行处理
        // 从而屏蔽一些不想给前端用户显示的数据
        final Object id = result.get("id");
        final Object size = result.get("size");
        final String name = String.valueOf(result.get("name"));
        final String storageLocation = String.valueOf(result.get("storageLocation"));
        result.clear();
        result.put("id", id);
        result.put("size", size);
        result.put("name", name);
        result.put("path", storageLocation);
        LOGGER.info("DefaultSimpleUploadAspectImpl.after() >>>>> {}", result);
        return Mono.just(true);
    }

}
