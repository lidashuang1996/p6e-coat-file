package club.p6e.coat.file.filter;

import club.p6e.coat.file.Properties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;

/**
 * 跨域过滤器
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = CrossDomainWebFluxFilter.class,
        ignored = CrossDomainWebFluxFilter.class
)
public class CrossDomainWebFluxFilter implements WebFilter {

    /**
     * 跨域配置 ACCESS_CONTROL_MAX_AGE
     */
    private static final long ACCESS_CONTROL_MAX_AGE = 3600L;

    /**
     * 跨域配置 ACCESS_CONTROL_ALLOW_ORIGIN
     */
    private static final String ACCESS_CONTROL_ALLOW_ORIGIN = "*";

    /**
     * 跨域配置 ACCESS_CONTROL_ALLOW_ORIGIN
     */
    private static final boolean ACCESS_CONTROL_ALLOW_CREDENTIALS = true;

    /**
     * 跨域配置 ACCESS_CONTROL_ALLOW_HEADERS
     */
    private static final String[] ACCESS_CONTROL_ALLOW_HEADERS = new String[]{
            "Accept",
            "Host",
            "Origin",
            "Referer",
            "User-Agent",
            "Content-Type",
            "Authorization"
    };

    /**
     * 跨域配置 ACCESS_CONTROL_ALLOW_METHODS
     */
    private static final HttpMethod[] ACCESS_CONTROL_ALLOW_METHODS = new HttpMethod[]{
            HttpMethod.GET,
            HttpMethod.POST,
            HttpMethod.PUT,
            HttpMethod.DELETE,
            HttpMethod.OPTIONS,
    };

    /**
     * 配置文件对象
     */
    private final Properties properties;

    /**
     * 构造方法初始化
     *
     * @param properties 配置文件对象
     */
    public CrossDomainWebFluxFilter(Properties properties) {
        this.properties = properties;
    }

    @NonNull
    @Override
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        final ServerHttpRequest request = exchange.getRequest();
        final ServerHttpResponse response = exchange.getResponse();
        if (properties.getCrossDomain().isEnable()) {
            final String origin = request.getHeaders().getOrigin();
            response.getHeaders().setAccessControlMaxAge(ACCESS_CONTROL_MAX_AGE);
            response.getHeaders().setAccessControlAllowOrigin(origin == null ? ACCESS_CONTROL_ALLOW_ORIGIN : origin);
            response.getHeaders().setAccessControlAllowCredentials(ACCESS_CONTROL_ALLOW_CREDENTIALS);
            response.getHeaders().setAccessControlAllowHeaders(Arrays.asList(ACCESS_CONTROL_ALLOW_HEADERS));
            response.getHeaders().setAccessControlAllowMethods(Arrays.asList(ACCESS_CONTROL_ALLOW_METHODS));
            // OPTIONS 请求直接返回成功
            if (HttpMethod.OPTIONS.matches(request.getMethod().name().toUpperCase())) {
                response.setStatusCode(HttpStatus.OK);
                return Mono.empty();
            }
        }
        return chain.filter(exchange.mutate().response(response).build());
    }
}
