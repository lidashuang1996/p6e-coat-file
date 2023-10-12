package club.p6e.coat.file.mapper;

import club.p6e.coat.file.error.ParameterException;
import club.p6e.coat.file.context.OpenUploadContext;
import club.p6e.coat.file.utils.FileUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 打开上传请求参数映射器
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = OpenUploadContextRequestParameterMapper.class,
        ignored = OpenUploadContextRequestParameterMapper.class
)
public class OpenUploadContextRequestParameterMapper extends RequestParameterMapper {

    /**
     * URL 文件名称请求参数
     */
    private static final String URL_PARAMETER_NAME = "name";

    /**
     * RAW JSON 文件名称请求参数
     */
    private static final String RAW_JSON_PARAMETER_NAME = "name";

    /**
     * FORM DATA 文件名称请求参数
     */
    private static final String FORM_DATA_PARAMETER_NAME = "name";

    @Override
    public Class<?> outputClass() {
        return OpenUploadContext.class;
    }

    @Override
    public Mono<Object> execute(ServerRequest request) {
        final OpenUploadContext context = new OpenUploadContext();
        final ServerHttpRequest httpRequest = request.exchange().getRequest();
        final MultiValueMap<String, String> queryParams = httpRequest.getQueryParams();
        context.putAll(queryParams);
        // 读取 URL 文件名称请求参数
        final List<String> names = queryParams.get(URL_PARAMETER_NAME);
        if (names != null && !names.isEmpty() && names.get(0) != null) {
            // 如果读取到了 URL 文件名称请求参数那么就写入到上下文对象中
            final String name = FileUtil.name(names.get(0));
            if (name == null) {
                return Mono.error(new ParameterException(
                        this.getClass(),
                        "fun execute(ServerRequest request). -> URL PARAM <"
                                + URL_PARAMETER_NAME + "> Request parameter format error",
                        "URL PARAM <" + URL_PARAMETER_NAME + "> Request parameter format error"
                ));
            }
            context.setName(name);
            return Mono.just(context);
        } else {
            // 读取请求的媒体类型
            final MediaType mediaType = httpRequest.getHeaders().getContentType();
            if (MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)) {
                return requestRawJsonMapper(request, context)
                        .flatMap(m -> {
                            final OpenUploadContext newContext = new OpenUploadContext(m);
                            final Object rjName = newContext.get(RAW_JSON_PREFIX + RAW_JSON_PARAMETER_NAME);
                            if (rjName instanceof final String content) {
                                final String name = FileUtil.name(content);
                                if (name == null) {
                                    return Mono.error(new ParameterException(
                                            this.getClass(),
                                            "fun execute(ServerRequest request). -> RAW JSON <"
                                                    + RAW_JSON_PARAMETER_NAME + "> Request parameter format error",
                                            "RAW JSON <" + RAW_JSON_PARAMETER_NAME + "> Request parameter format error"
                                    ));
                                }
                                newContext.setName(name);
                                return Mono.just(newContext);
                            }
                            // 如果没有读取到了 RAW JSON 文件名称请求参数那么就抛出参数异常
                            return Mono.error(new ParameterException(
                                    this.getClass(),
                                    "fun execute(ServerRequest request) -> Request parameter is null",
                                    "Request parameter is null"
                            ));
                        });
            } else if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(mediaType)) {
                return requestFormDataMapper(request, context)
                        .flatMap(m -> {
                            final OpenUploadContext newContext = new OpenUploadContext(m);
                            final Object fdName = newContext.get(FORM_DATA_PREFIX + FORM_DATA_PARAMETER_NAME);
                            if (fdName instanceof final List<?> ol && !ol.isEmpty()
                                    && ol.get(0) instanceof final String content) {
                                final String name = FileUtil.name(content);
                                if (name == null) {
                                    return Mono.error(new ParameterException(
                                            this.getClass(),
                                            "fun execute(ServerRequest request). -> FORM DATA <"
                                                    + FORM_DATA_PARAMETER_NAME + "> Request parameter format error",
                                            "FORM DATA <" + FORM_DATA_PARAMETER_NAME + "> Request parameter format error"
                                    ));
                                }
                                newContext.setName(FileUtil.name(content));
                                return Mono.just(newContext);
                            }
                            // 如果没有读取到了 FORM DATA 文件名称请求参数那么就抛出参数异常
                            return Mono.error(new ParameterException(
                                    this.getClass(),
                                    "fun execute(ServerRequest request) -> Request parameter is null",
                                    "Request parameter is null"
                            ));

                        });
            } else {
                return Mono.error(new ParameterException(
                        this.getClass(),
                        "fun execute(ServerRequest request) -> Request parameter is null",
                        "Request parameter is null"
                ));
            }
        }
    }

}
