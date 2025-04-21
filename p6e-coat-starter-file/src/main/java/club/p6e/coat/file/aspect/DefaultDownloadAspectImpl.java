package club.p6e.coat.file.aspect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 下载文件-切面（钩子）
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
public class DefaultDownloadAspectImpl implements DownloadAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDownloadAspectImpl.class);

    @Override
    public int order() {
        return 0;
    }

    @Override
    public Mono<Boolean> before(Map<String, Object> data) {
        LOGGER.info("DefaultDownloadAspectImpl.before() >>>>> {}", data);
        return Mono.just(true);
    }

    @Override
    public Mono<Boolean> after(Map<String, Object> data) {
        LOGGER.info("DefaultDownloadAspectImpl.after() >>>>> {}", data);
        return Mono.just(true);
    }

}
