package club.p6e.coat.file.aspect;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 分片上传-切面（钩子）
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = SliceUploadAspect.class,
        ignored = DefaultSliceUploadAspectImpl.class
)
public class DefaultSliceUploadAspectImpl implements SliceUploadAspect {

    @Override
    public Mono<Boolean> before(Map<String, Object> data) {
        return Mono.just(true);
    }

    @Override
    public Mono<Boolean> after(Map<String, Object> data, Map<String, Object> result) {
        // 对返回的结果数据进行处理
        // 从而屏蔽一些不想给前端用户显示的数据
        final Object id = result.get("id");
        final Object fid = result.get("fid");
        final Object name = result.get("name");
        final Object size = result.get("size");
        result.clear();
        result.put("id", id);
        result.put("fid", fid);
        result.put("name", name);
        result.put("size", size);
        return Mono.just(true);
    }

}
