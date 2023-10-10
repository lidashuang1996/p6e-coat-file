package club.p6e.coat.file.aspect;

import club.p6e.coat.file.utils.FileUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 简单（小文件）上传操作的切面（钩子）的默认实现
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = SimpleUploadAspect.class,
        ignored = DefaultSimpleUploadAspectImpl.class
)
public class DefaultSimpleUploadAspectImpl implements SimpleUploadAspect {

    @Override
    public Mono<Boolean> before(Map<String, Object> data) {
        return Mono.just(true);
    }

    @Override
    public Mono<Boolean> after(Map<String, Object> data, Map<String, Object> result) {
        final Object id = result.get("id");
        final String fileName = String.valueOf(result.get("name"));
        final String storageLocation = String.valueOf(result.get("storageLocation"));
        result.clear();
        result.put("id", id);
        result.put("path", FileUtil.composePath(storageLocation, fileName));
        return Mono.just(true);
    }

}
