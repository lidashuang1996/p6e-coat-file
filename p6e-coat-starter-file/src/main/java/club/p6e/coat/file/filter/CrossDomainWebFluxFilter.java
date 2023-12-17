package club.p6e.coat.file.filter;

import club.p6e.coat.file.Properties;
import club.p6e.coat.file.handler.AspectHandlerFunction;
import club.p6e.coat.file.utils.JsonUtil;
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

import java.nio.charset.StandardCharsets;
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
     * 通用内容
     */
    private static final String CROSS_DOMAIN_HEADER_GENERAL_CONTENT = "*";

    /**
     * 跨域配置 ACCESS_CONTROL_MAX_AGE
     */
    private static final long ACCESS_CONTROL_MAX_AGE = 3600L;

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
     * 错误结果内容
     */
    private static final String ERROR_RESULT_CONTENT = JsonUtil.toJson(
            AspectHandlerFunction.ResultContext.build(
                    HttpStatus.FORBIDDEN.value(),
                    HttpStatus.FORBIDDEN.getReasonPhrase(),
                    "Host is not allowed."
            ));

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
        if (!properties.getCrossDomain().isEnable()) {
            return chain.filter(exchange);
        }

        final ServerHttpRequest request = exchange.getRequest();
        final ServerHttpResponse response = exchange.getResponse();

        final String origin = request.getHeaders().getOrigin();
        if (origin == null) {
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return response.writeWith(Mono.just(response.bufferFactory()
                    .wrap(ERROR_RESULT_CONTENT.getBytes(StandardCharsets.UTF_8))));
        }

        boolean status = false;
        for (final String item : properties.getCrossDomain().getWhiteList()) {
            if (CROSS_DOMAIN_HEADER_GENERAL_CONTENT.equals(item) || origin.startsWith(item)) {
                status = true;
                break;
            }
        }

        if (status) {
            response.getHeaders().setAccessControlAllowOrigin(origin);
            response.getHeaders().setAccessControlMaxAge(ACCESS_CONTROL_MAX_AGE);
            response.getHeaders().setAccessControlAllowCredentials(ACCESS_CONTROL_ALLOW_CREDENTIALS);
            response.getHeaders().setAccessControlAllowHeaders(Arrays.asList(ACCESS_CONTROL_ALLOW_HEADERS));
            response.getHeaders().setAccessControlAllowMethods(Arrays.asList(ACCESS_CONTROL_ALLOW_METHODS));

            if (HttpMethod.OPTIONS.matches(request.getMethod().name().toUpperCase())) {
                response.setStatusCode(HttpStatus.NO_CONTENT);
                return Mono.empty();
            } else {
                return chain.filter(exchange);
            }
        } else {
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return response.writeWith(Mono.just(response.bufferFactory()
                    .wrap(ERROR_RESULT_CONTENT.getBytes(StandardCharsets.UTF_8))));
        }
    }
}
