package club.p6e.coat.file.mapper;

import club.p6e.coat.file.error.ParameterException;
import club.p6e.coat.file.context.CloseUploadContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 关闭上传请求参数映射器
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = CloseUploadContextRequestParameterMapper.class,
        ignored = CloseUploadContextRequestParameterMapper.class
)
public class CloseUploadContextRequestParameterMapper extends RequestParameterMapper {

    /**
     * URL ID 请求参数
     */
    private static final String URL_PARAMETER_ID = "id";

    /**
     * NODE 请求参数
     */
    private static final String URL_PARAMETER_NODE = "node";

    /**
     * PATH URL ID 请求参数
     */
    private static final String PATH_URL_PARAMETER_ID = "id";

    /**
     * FORM DATA ID 请求参数
     */
    private static final String FORM_DATA_PARAMETER_ID = "id";

    /**
     * FORM DATA NODE 请求参数
     */
    private static final String FORM_DATA_PARAMETER_NODE = "node";

    /**
     * RAW JSON ID 请求参数
     */
    private static final String RAW_JSON_PARAMETER_ID = "id";

    /**
     * RAW JSON NODE 请求参数
     */
    private static final String RAW_JSON_PARAMETER_NODE = "node";

    /**
     * 请求路径后缀标记
     */
    private static final String REQUEST_PATH_FINISH_MARK = "close";

    @Override
    public Class<?> outputClass() {
        return CloseUploadContext.class;
    }

    @Override
    public Mono<Object> execute(ServerRequest request) {
        final CloseUploadContext context = new CloseUploadContext();
        final ServerHttpRequest httpRequest = request.exchange().getRequest();
        final MultiValueMap<String, String> queryParams = httpRequest.getQueryParams();
        context.putAll(queryParams);
        final List<PathContainer.Element> elements = request.requestPath().elements();
        final String requestPathFinishContent = elements.get(elements.size() - 1).value();
        // 如果不是请求后缀标记
        // 那么请求的后缀是请求参数 ID
        if (!REQUEST_PATH_FINISH_MARK.equals(requestPathFinishContent)) {
            try {
                context.setId(Integer.valueOf(requestPathFinishContent));
            } catch (Exception e) {
                return Mono.error(new ParameterException(
                        this.getClass(),
                        "fun execute(ServerRequest request). -> PATH PARAM <"
                                + PATH_URL_PARAMETER_ID + "> Request parameter type not is int",
                        "PATH PARAM <" + PATH_URL_PARAMETER_ID + "> Request parameter type not is int"
                ));
            }
        }
        if (context.getId() == null
                && queryParams.get(URL_PARAMETER_ID) != null
                && !queryParams.get(URL_PARAMETER_ID).isEmpty()
                && queryParams.get(URL_PARAMETER_ID).get(0) != null) {
            try {
                context.setId(Integer.valueOf(queryParams.get(URL_PARAMETER_ID).get(0)));
            } catch (Exception e) {
                return Mono.error(new ParameterException(
                        this.getClass(),
                        "fun execute(ServerRequest request). -> URL PARAM <"
                                + URL_PARAMETER_ID + "> Request parameter type not is int",
                        "URL PARAM <" + URL_PARAMETER_ID + "> Request parameter type not is int"
                ));
            }
        }
        if (context.getNode() == null
                && queryParams.get(URL_PARAMETER_NODE) != null
                && !queryParams.get(URL_PARAMETER_NODE).isEmpty()
                && queryParams.get(URL_PARAMETER_NODE).get(0) != null) {
            context.setNode(queryParams.get(URL_PARAMETER_NODE).get(0));
        }
        // 读取请求的媒体类型
        final MediaType mediaType = httpRequest.getHeaders().getContentType();
        if (MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)) {
            return requestRawJsonMapper(request, context)
                    .flatMap(m -> {
                        final CloseUploadContext newContext = new CloseUploadContext(m);
                        if (newContext.getId() == null) {
                            final Object rjId = newContext.get(RAW_JSON_PREFIX + RAW_JSON_PARAMETER_ID);
                            if (rjId instanceof final Integer content) {
                                newContext.setId(content);
                            } else {
                                // 如果没有读取到了 RAW JSON ID 请求参数那么就抛出参数异常
                                return Mono.error(new ParameterException(
                                        this.getClass(),
                                        "fun execute(ServerRequest request). -> RAW JSON PARAM <"
                                                + RAW_JSON_PARAMETER_ID + "> Request parameter is null",
                                        "RAW JSON PARAM <" + RAW_JSON_PARAMETER_ID + "> Request parameter is null"
                                ));
                            }
                        }
                        if (newContext.getNode() == null) {
                            final Object rjNode = newContext.get(RAW_JSON_PREFIX + RAW_JSON_PARAMETER_NODE);
                            if (rjNode instanceof final String content) {
                                newContext.setNode(content);
                            } else {
                                // 如果没有读取到了 RAW JSON ID 请求参数那么就抛出参数异常
                                return Mono.error(new ParameterException(
                                        this.getClass(),
                                        "fun execute(ServerRequest request). -> RAW JSON PARAM <"
                                                + RAW_JSON_PARAMETER_ID + "> Request parameter is null",
                                        "RAW JSON PARAM <" + RAW_JSON_PARAMETER_ID + "> Request parameter is null"
                                ));
                            }
                        }
                        return Mono.just(newContext);
                    });
        } else if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(mediaType)) {
            return requestFormDataMapper(request, context)
                    .flatMap(m -> {
                        final CloseUploadContext newContext = new CloseUploadContext(m);
                        if (newContext.getId() == null) {
                            final Object fdId = newContext.get(FORM_DATA_PREFIX + FORM_DATA_PARAMETER_ID);
                            if (fdId instanceof final List<?> ol && !ol.isEmpty()
                                    && ol.get(0) instanceof final FormFieldPart filePart) {
                                try {
                                    newContext.setId(Integer.valueOf(filePart.value()));
                                } catch (Exception e) {
                                    // 类型转换异常，请求参数不是我们需要的类型，抛出参数类型异常
                                    return Mono.error(new ParameterException(
                                            this.getClass(),
                                            "fun execute(ServerRequest request). -> FORM DATA PARAM <"
                                                    + FORM_DATA_PARAMETER_ID + "> Request parameter type is not int",
                                            "FORM DATA PARAM <" + FORM_DATA_PARAMETER_ID + "> Request parameter type is not int"
                                    ));
                                }
                            } else {
                                // 如果没有读取到了 FORM DATA ID 请求参数那么就抛出参数异常
                                return Mono.error(new ParameterException(
                                        this.getClass(),
                                        "fun execute(ServerRequest request). -> Request parameter is null",
                                        "Request parameter is null"
                                ));
                            }
                        }
                        if (newContext.getNode() == null) {
                            final Object fdNode = newContext.get(FORM_DATA_PREFIX + FORM_DATA_PARAMETER_NODE);
                            if (fdNode instanceof final List<?> ol && !ol.isEmpty()
                                    && ol.get(0) instanceof final FormFieldPart filePart) {
                                try {
                                    newContext.setNode(filePart.value());
                                } catch (Exception e) {
                                    // 类型转换异常，请求参数不是我们需要的类型，抛出参数类型异常
                                    return Mono.error(new ParameterException(
                                            this.getClass(),
                                            "fun execute(ServerRequest request). -> FORM DATA PARAM " +
                                                    "<" + FORM_DATA_PARAMETER_ID + "> Request parameter type is not int",
                                            "FORM DATA PARAM <" + FORM_DATA_PARAMETER_ID + "> Request parameter type is not int"
                                    ));
                                }
                            } else {
                                // 如果没有读取到了 FORM DATA ID 请求参数那么就抛出参数异常
                                return Mono.error(new ParameterException(
                                        this.getClass(),
                                        "fun execute(ServerRequest request). -> Request parameter is null",
                                        "Request parameter is null"
                                ));
                            }
                        }
                        return Mono.just(newContext);
                    });
        } else {
            if (context.getId() == null || context.getNode() == null) {
                return Mono.error(new ParameterException(
                        this.getClass(),
                        "fun execute(ServerRequest request). -> Request parameter is null",
                        "Request parameter is null"
                ));
            } else {
                return Mono.just(context);
            }
        }
    }
}
