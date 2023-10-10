package club.p6e.coat.file.aspect;

import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 下载操作的切面（钩子）
 *
 * @author lidashuang
 * @version 1.0
 */
public interface DownloadAspect extends Aspect {

    public Mono<Boolean> after(Map<String, Object> data);

    @Override
    default Mono<Boolean> after(Map<String, Object> data, Map<String, Object> result) {
        return after(data);
    }

}
