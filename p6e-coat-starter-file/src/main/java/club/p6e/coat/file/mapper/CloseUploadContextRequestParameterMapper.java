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
     * PATH URL ID 请求参数
     */
    private static final String PATH_URL_PARAMETER_ID = "id";

    /**
     * FORM DATA ID 请求参数
     */
    private static final String FORM_DATA_PARAMETER_ID = "id";

    /**
     * RAW JSON ID 请求参数
     */
    private static final String RAW_JSON_PARAMETER_ID = "id";

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
                        "fun execute(ServerRequest request). " +
                                "<" + PATH_URL_PARAMETER_ID + "> Request parameter type not is int",
                        "<" + PATH_URL_PARAMETER_ID + "> Request parameter type not is int"
                ));
            }
            return Mono.just(context);
        } else {
            // 读取 URL ID 请求参数
            final List<String> ids = queryParams.get(URL_PARAMETER_ID);
            if (ids != null && ids.size() > 0) {
                try {
                    // 如果读取到了 URL ID 那么就写入到上下文对象中
                    context.setId(Integer.valueOf(ids.get(0)));
                    return Mono.just(context);
                } catch (Exception e) {
                    // 类型转换异常，请求参数不是我们需要的类型，抛出参数类型异常
                    return Mono.error(new ParameterException(
                            this.getClass(),
                            "fun execute(ServerRequest request). " +
                                    "<" + URL_PARAMETER_ID + "> Request parameter type is not int",
                            "<" + URL_PARAMETER_ID + "> Request parameter type is not int"
                    ));
                }
            } else {
                // 读取请求的媒体类型
                final MediaType mediaType = httpRequest.getHeaders().getContentType();
                if (MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)) {
                    return requestRawJsonMapper(request, context)
                            .flatMap(m -> {
                                final CloseUploadContext newContext = new CloseUploadContext(m);
                                final Object rjId = newContext.get(RAW_JSON_PREFIX + RAW_JSON_PARAMETER_ID);
                                if (rjId instanceof final Integer content) {
                                    newContext.setId(content);
                                    return Mono.just(newContext);
                                } else {
                                    // 如果没有读取到了 RAW JSON ID 请求参数那么就抛出参数异常
                                    return Mono.error(new ParameterException(
                                            this.getClass(),
                                            "fun execute(ServerRequest request). " +
                                                    "<" + RAW_JSON_PARAMETER_ID + "> Request parameter is null",
                                            "<" + RAW_JSON_PARAMETER_ID + "> Request parameter is null"
                                    ));
                                }
                            });
                } else if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(mediaType)) {
                    return requestFormDataMapper(request, context)
                            .flatMap(m -> {
                                final CloseUploadContext newContext = new CloseUploadContext(m);
                                final Object fdId = newContext.get(FORM_DATA_PREFIX + FORM_DATA_PARAMETER_ID);
                                if (fdId instanceof final List<?> ol && ol.size() > 0
                                        && ol.get(0) instanceof final FormFieldPart filePart) {
                                    try {
                                        newContext.setId(Integer.valueOf(filePart.value()));
                                        return Mono.just(newContext);
                                    } catch (Exception e) {
                                        // 类型转换异常，请求参数不是我们需要的类型，抛出参数类型异常
                                        return Mono.error(new ParameterException(
                                                this.getClass(),
                                                "fun execute(ServerRequest request). " +
                                                        "<" + FORM_DATA_PARAMETER_ID + "> Request parameter type is not int",
                                                "<" + FORM_DATA_PARAMETER_ID + "> Request parameter type is not int"
                                        ));
                                    }
                                } else {
                                    // 如果没有读取到了 FORM DATA ID 请求参数那么就抛出参数异常
                                    return Mono.error(new ParameterException(
                                            this.getClass(),
                                            "fun execute(ServerRequest request). " +
                                                    "<" + FORM_DATA_PARAMETER_ID + "> Request parameter is null",
                                            "<" + FORM_DATA_PARAMETER_ID + "> Request parameter is null"
                                    ));
                                }
                            });
                } else {
                    return Mono.error(new ParameterException(
                            this.getClass(),
                            "fun execute(ServerRequest request). " +
                                    "<" + URL_PARAMETER_ID + "> Request parameter is null",
                            "<" + URL_PARAMETER_ID + "> Request parameter is null"
                    ));
                }
            }
        }
    }

}
