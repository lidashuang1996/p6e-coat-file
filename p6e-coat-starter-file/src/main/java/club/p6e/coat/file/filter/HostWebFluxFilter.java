package club.p6e.coat.file.filter;

import club.p6e.coat.file.Properties;
import club.p6e.coat.file.handler.AspectHandlerFunction;
import club.p6e.coat.file.utils.JsonUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 跨域过滤器
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = HostWebFluxFilter.class,
        ignored = HostWebFluxFilter.class
)
public class HostWebFluxFilter implements WebFilter {

    /**
     * 错误的结果对象
     */
    private static final String ERROR_RESULT_CONTENT = JsonUtil.toJson(AspectHandlerFunction.ResultContext.build(
            HttpStatus.FORBIDDEN.value(),
            HttpStatus.FORBIDDEN.getReasonPhrase(),
            "Host is not allowed."
    ));

    /**
     * HOST 配置
     */
    private final Properties.Host config;

    /**
     * 构造方法初始化
     *
     * @param properties 配置
     */
    public HostWebFluxFilter(Properties properties) {
        this.config = properties.getHost();
    }

    @NonNull
    @Override
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        if (config.isEnable()) {
            final ServerHttpRequest request = exchange.getRequest();
            final ServerHttpResponse response = exchange.getResponse();
            final String host = request.getHeaders().getFirst("Host");
            if (host != null) {
                for (final String item : config.getList()) {
                    if ("*".equals(item) || host.equalsIgnoreCase(item)) {
                        return chain.filter(exchange);
                    }
                }
            }
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return response.writeWith(
                    Mono.just(response.bufferFactory()
                            .wrap(ERROR_RESULT_CONTENT.getBytes(StandardCharsets.UTF_8)))
            );
        }
        return chain.filter(exchange);
    }
}
