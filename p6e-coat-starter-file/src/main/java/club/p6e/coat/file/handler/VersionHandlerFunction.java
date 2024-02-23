package club.p6e.coat.file.handler;

import club.p6e.coat.file.Properties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = VersionHandlerFunction.class,
        ignored = VersionHandlerFunction.class
)
public class VersionHandlerFunction implements HandlerFunction<ServerResponse> {

    /**
     * 配置文件对象
     */
    private final Properties properties;

    /**
     * 构造函数初始化
     *
     * @param properties 配置文件对象
     */
    public VersionHandlerFunction(Properties properties) {
        this.properties = properties;
    }

    @NonNull
    @Override
    public Mono<ServerResponse> handle(@NonNull ServerRequest request) {
        return ServerResponse.ok().bodyValue("club.p6e.coat.file@version" + properties.getVersion());
    }

}
