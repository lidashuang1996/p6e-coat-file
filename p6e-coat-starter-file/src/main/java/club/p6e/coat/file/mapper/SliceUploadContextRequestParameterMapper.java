package club.p6e.coat.file.mapper;

import club.p6e.coat.file.error.MediaTypeException;
import club.p6e.coat.file.error.ParameterException;
import club.p6e.coat.file.context.SliceUploadContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 分片上传请求参数映射器
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = SliceUploadContextRequestParameterMapper.class,
        ignored = SliceUploadContextRequestParameterMapper.class
)
public class SliceUploadContextRequestParameterMapper extends RequestParameterMapper {

    /**
     * URL ID 请求参数
     */
    private static final String URL_PARAMETER_ID = "id";

    /**
     * URL INDEX 请求参数
     */
    private static final String URL_PARAMETER_INDEX = "index";

    /**
     * URL SIGNATURE 请求参数
     */
    private static final String URL_PARAMETER_SIGNATURE = "signature";

    /**
     * FORM ID 请求参数
     */
    private static final String FORM_DATA_PARAMETER_ID = "id";

    /**
     * FORM INDEX 请求参数
     */
    private static final String FORM_DATA_PARAMETER_INDEX = "index";

    /**
     * FORM SIGNATURE 请求参数
     */
    private static final String FORM_DATA_PARAMETER_SIGNATURE = "signature";

    /**
     * FORM FILE 请求参数
     */
    private static final String FORM_DATA_PARAMETER_FILE = "file";

    /**
     * 请求路径后缀标记
     */
    private static final String REQUEST_PATH_FINISH_MARK = "slice";

    @Override
    public Class<?> outputClass() {
        return SliceUploadContext.class;
    }

    @Override
    public Mono<Object> execute(ServerRequest request) {
        final SliceUploadContext context = new SliceUploadContext();
        final ServerHttpRequest httpRequest = request.exchange().getRequest();
        final MediaType mediaType = httpRequest.getHeaders().getContentType();
        final MultiValueMap<String, String> queryParams = httpRequest.getQueryParams();
        context.putAll(queryParams);
        if (!MediaType.MULTIPART_FORM_DATA.isCompatibleWith(mediaType)) {
            return Mono.error(new MediaTypeException(
                    this.getClass(),
                    "fun execute(ServerRequest request). -> Unrecognized media " +
                            "type [" + mediaType + "], need media type [" + MediaType.MULTIPART_FORM_DATA + "]!!",
                    "Unrecognized media type [" + mediaType + "], need media type [" + MediaType.MULTIPART_FORM_DATA + "]")
            );
        }
        // 初始化请求参数 ID
        initParameterId(request, context);
        // 初始化请求参数 INDEX
        initParameterIndex(request, context);
        // 初始化请求参数 SIGNATURE
        initParameterSignature(request, context);
        // FROM DATA 参数
        return requestFormDataMapper(request, context)
                .flatMap(m -> {
                    final SliceUploadContext newContext = new SliceUploadContext(m);
                    if (newContext.getId() == null) {
                        final Object fdId = newContext.get(FORM_DATA_PREFIX + FORM_DATA_PARAMETER_ID);
                        if (fdId instanceof final List<?> ol && !ol.isEmpty()
                                && ol.get(0) instanceof final String content) {
                            try {
                                newContext.setId(Integer.valueOf(content));
                            } catch (Exception e) {
                                // 类型转换异常，请求参数不是我们需要的类型，抛出参数类型异常
                                return Mono.error(new ParameterException(
                                        this.getClass(),
                                        "fun execute(ServerRequest request). -> FROM DATA " +
                                                "<" + FORM_DATA_PARAMETER_ID + "> Request parameter type is not int",
                                        "FROM DATA <" + FORM_DATA_PARAMETER_ID + "> Request parameter type is not int"
                                ));
                            }
                        } else {
                            // 如果没有读取到了 FORM DATA ID 请求参数那么就抛出参数异常
                            return Mono.error(new ParameterException(
                                    this.getClass(),
                                    "fun execute(ServerRequest request). Request parameter is null",
                                    "Request parameter is null"
                            ));
                        }
                    }
                    if (newContext.getIndex() == null) {
                        final Object fdIndex = newContext.get(FORM_DATA_PREFIX + FORM_DATA_PARAMETER_INDEX);
                        if (fdIndex instanceof final List<?> ol && !ol.isEmpty()
                                && ol.get(0) instanceof final String content) {
                            try {
                                newContext.setIndex(Integer.valueOf(content));
                            } catch (Exception e) {
                                // 类型转换异常，请求参数不是我们需要的类型，抛出参数类型异常
                                return Mono.error(new ParameterException(
                                        this.getClass(),
                                        "fun execute(ServerRequest request). -> FROM DATA " +
                                                "<" + FORM_DATA_PARAMETER_INDEX + "> Request parameter type is not int",
                                        "FROM DATA <" + FORM_DATA_PARAMETER_INDEX + "> Request parameter type is not int"
                                ));
                            }
                        } else {
                            // 如果没有读取到了 FORM DATA INDEX 请求参数那么就抛出参数异常
                            return Mono.error(new ParameterException(
                                    this.getClass(),
                                    "fun execute(ServerRequest request). Request parameter is null",
                                    "Request parameter is null"
                            ));
                        }
                    }
                    if (newContext.getSignature() == null) {
                        final Object fdSignature = newContext.get(FORM_DATA_PREFIX + FORM_DATA_PARAMETER_SIGNATURE);
                        if (fdSignature instanceof final List<?> ol && !ol.isEmpty()
                                && ol.get(0) instanceof final String content) {
                            newContext.setSignature(content);
                        } else {
                            // 如果没有读取到了 FORM DATA SIGNATURE 请求参数那么就抛出参数异常
                            return Mono.error(new ParameterException(
                                    this.getClass(),
                                    "fun execute(ServerRequest request). Request parameter is null",
                                    "Request parameter is null"
                            ));
                        }
                    }
                    final Object fdFile = newContext.get(FORM_DATA_PREFIX + FORM_DATA_PARAMETER_FILE);
                    if (fdFile instanceof final List<?> ol
                            && !ol.isEmpty() && ol.get(0) instanceof final FilePart fp) {
                        newContext.setFilePart(fp);
                    } else {
                        // 如果没有读取到了 FORM DATA 文件请求参数那么就抛出参数异常
                        return Mono.error(new ParameterException(
                                this.getClass(),
                                "fun execute(ServerRequest request). Request parameter is null",
                                " Request parameter is null"
                        ));
                    }
                    return Mono.just(newContext);
                });
    }

    /**
     * 初始化请求参数 ID
     *
     * @param request ServerRequest 对象
     * @param context SliceUploadContext 对象
     */
    private void initParameterId(ServerRequest request, SliceUploadContext context) {
        final List<PathContainer.Element> elements = request.requestPath().elements();
        final String requestPathFinishContent = elements.get(elements.size() - 1).value();
        final MultiValueMap<String, String> queryParams = request.exchange().getRequest().getQueryParams();
        if (!REQUEST_PATH_FINISH_MARK.equals(requestPathFinishContent)) {
            try {
                context.setId(Integer.valueOf(requestPathFinishContent));
            } catch (Exception e) {
                // 忽略异常
            }
        }
        if (context.getId() == null) {
            // 读取 URL ID 请求参数
            final List<String> ids = queryParams.get(URL_PARAMETER_ID);
            if (ids != null && !ids.isEmpty() && ids.get(0) != null) {
                try {
                    // 如果读取到了 URL ID 那么就写入到上下文对象中
                    context.setId(Integer.valueOf(ids.get(0)));
                } catch (Exception e) {
                    // 忽略异常
                }
            }
        }
    }

    /**
     * 初始化请求参数 INDEX
     *
     * @param request ServerRequest 对象
     * @param context SliceUploadContext 对象
     */
    private void initParameterIndex(ServerRequest request, SliceUploadContext context) {
        // 读取 URL INDEX 请求参数
        final MultiValueMap<String, String> queryParams = request.exchange().getRequest().getQueryParams();
        final List<String> indexes = queryParams.get(URL_PARAMETER_INDEX);
        if (indexes != null && !indexes.isEmpty() && indexes.get(0) != null) {
            try {
                // 如果读取到了 URL INDEX 那么就写入到上下文对象中
                context.setIndex(Integer.valueOf(indexes.get(0)));
            } catch (Exception e) {
                // 忽略异常
            }
        }
    }

    /**
     * 初始化请求参数 SIGNATURE
     *
     * @param request ServerRequest 对象
     * @param context SliceUploadContext 对象
     */
    private void initParameterSignature(ServerRequest request, SliceUploadContext context) {
        final MultiValueMap<String, String> queryParams = request.exchange().getRequest().getQueryParams();
        // 读取 URL SIGNATURE 请求参数
        final List<String> signatures = queryParams.get(URL_PARAMETER_SIGNATURE);
        if (signatures != null && !signatures.isEmpty() && signatures.get(0) != null) {
            // 如果读取到了 URL SIGNATURE 那么就写入到上下文对象中
            context.setSignature(signatures.get(0));
        }
    }

}
