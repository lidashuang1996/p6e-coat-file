package club.p6e.coat.file.mapper;

import club.p6e.coat.file.error.MediaTypeException;
import club.p6e.coat.file.error.ParameterException;
import club.p6e.coat.file.context.SimpleUploadContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 简单（小文件）上传请求参数映射器
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = SimpleUploadContextRequestParameterMapper.class,
        ignored = SimpleUploadContextRequestParameterMapper.class
)
public class SimpleUploadContextRequestParameterMapper extends RequestParameterMapper {

    /**
     * NODE 请求参数
     */
    private static final String NODE_PARAMETER_NAME = "node";

    /**
     * FORM DATA 文件请求参数
     */
    private static final String FORM_DATA_PARAMETER_FILE = "file";

    @Override
    public Class<?> outputClass() {
        return SimpleUploadContext.class;
    }

    @Override
    public Mono<Object> execute(ServerRequest request) {
        final SimpleUploadContext context = new SimpleUploadContext();
        final ServerHttpRequest httpRequest = request.exchange().getRequest();
        final MediaType mediaType = httpRequest.getHeaders().getContentType();
        if (!MediaType.MULTIPART_FORM_DATA.isCompatibleWith(mediaType)) {
            return Mono.error(new MediaTypeException(
                    this.getClass(),
                    "fun execute(ServerRequest request). -> Unrecognized media " +
                            "type [" + mediaType + "], need media type [" + MediaType.MULTIPART_FORM_DATA + "]!!",
                    "Unrecognized media type [" + mediaType + "], need media type [" + MediaType.MULTIPART_FORM_DATA + "]")
            );
        }
        // 读取 URL 参数并写入
        final MultiValueMap<String, String> queryParams = httpRequest.getQueryParams();
        context.putAll(queryParams);
        if (context.getNode() == null
                && queryParams.get(NODE_PARAMETER_NAME) != null
                && !queryParams.get(NODE_PARAMETER_NAME).isEmpty()
                && queryParams.get(NODE_PARAMETER_NAME).get(0) != null) {
            context.setNode(queryParams.get(NODE_PARAMETER_NAME).get(0));
        }
        // 读取 FROM DATA 参数并写入
        return requestFormDataMapper(request, context)
                .flatMap(m -> {
                    final SimpleUploadContext newContext = new SimpleUploadContext(m);
                    if (newContext.getNode() == null) {
                        final Object o = newContext.get(FORM_DATA_PREFIX + NODE_PARAMETER_NAME);
                        if (o instanceof final List<?> ol && !ol.isEmpty()
                                && ol.get(0) instanceof final String content) {
                            newContext.setNode(content);
                        } else {
                            // 如果没有读取到了 FORM DATA SIGNATURE 请求参数那么就抛出参数异常
                            return Mono.error(new ParameterException(
                                    this.getClass(),
                                    "fun execute(ServerRequest request). -> " +
                                            "<" + NODE_PARAMETER_NAME + "> Request parameter is null",
                                    "<" + NODE_PARAMETER_NAME + "> Request parameter is null"
                            ));
                        }
                    }
                    // 读取 FORM DATA 文件请求参数
                    final Object o = newContext.get(FORM_DATA_PREFIX + FORM_DATA_PARAMETER_FILE);
                    if (o instanceof final List<?> ol && !ol.isEmpty()
                            && ol.get(0) instanceof final FilePart filePart) {
                        // 如果读取到了 FORM DATA 文件请求参数那么就写入到上下文对象中
                        newContext.setFilePart(filePart);
                        return Mono.just(newContext);
                    }
                    // 如果没有读取到了 FORM DATA 文件请求参数那么就抛出参数异常
                    return Mono.error(new ParameterException(
                            this.getClass(),
                            "fun execute(ServerRequest request). -> Request parameter is null",
                            "Request parameter is null"
                    ));
                });
    }

}
