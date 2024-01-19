package club.p6e.coat.file.filter;

import club.p6e.coat.file.Properties;
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
import java.util.List;

/**
 * 跨域过滤器
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = RefererWebFluxFilter.class,
        ignored = RefererWebFluxFilter.class
)
public class RefererWebFluxFilter implements WebFilter {

    /**
     * REFERER
     */
    private static final String REFERER_HEADER = "Referer";

    /**
     * REFERER 通用内容
     */
    private static final String REFERER_HEADER_GENERAL_CONTENT = "*";

    /**
     * 错误结果内容
     */
    private static final String ERROR_RESULT_CONTENT =
            "{\"code\":403,\"message\":\"Forbidden\",\"data\":\"Referer Exception\"}";

    /**
     * 配置文件对象
     */
    private final Properties properties;

    /**
     * 构造方法初始化
     *
     * @param properties 配置文件对象
     */
    public RefererWebFluxFilter(Properties properties) {
        this.properties = properties;
    }

    @NonNull
    @Override
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        if (!properties.getReferer().isEnable()) {
            return chain.filter(exchange);
        }
        final ServerHttpRequest request = exchange.getRequest();
        final ServerHttpResponse response = exchange.getResponse();
        final List<String> refererList = request.getHeaders().get(REFERER_HEADER);
        if (refererList != null && !refererList.isEmpty()) {
            final String r = refererList.get(0);
            final String referer = r == null ? "" : r;
            for (final String item : properties.getReferer().getWhiteList()) {
                if (REFERER_HEADER_GENERAL_CONTENT.equals(item) || referer.startsWith(item)) {
                    return chain.filter(exchange);
                }
            }
        }
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.writeWith(Mono.just(response.bufferFactory()
                .wrap(ERROR_RESULT_CONTENT.getBytes(StandardCharsets.UTF_8))));
    }
}
