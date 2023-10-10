package club.p6e.coat.file.handler;

import club.p6e.coat.file.aspect.ResourceAspect;
import club.p6e.coat.file.context.ResourceContext;
import club.p6e.coat.file.error.FileException;
import club.p6e.coat.file.error.MediaTypeException;
import club.p6e.coat.file.mapper.RequestParameterMapper;
import club.p6e.coat.file.service.ResourceService;
import club.p6e.coat.file.utils.FileUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 资源操作处理程序函数
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = ResourceHandlerFunction.class,
        ignored = ResourceHandlerFunction.class
)
public class ResourceHandlerFunction extends AspectHandlerFunction implements HandlerFunction<ServerResponse> {

    /**
     * 下载切面对象
     */
    private final ResourceAspect aspect;

    /**
     * 下载服务对象
     */
    private final ResourceService service;

    /**
     * 构造函数初始化
     *
     * @param aspect  下载切面对象
     * @param service 下载服务对象
     */
    public ResourceHandlerFunction(ResourceAspect aspect, ResourceService service) {
        this.aspect = aspect;
        this.service = service;
    }

    @NonNull
    @Override
    public Mono<ServerResponse> handle(@NonNull ServerRequest request) {
        return
                // 通过请求参数映射器获取上下文对象
                RequestParameterMapper.execute(request, ResourceContext.class)
                        // 执行下载操作之前的切点
                        .flatMap(c -> before(aspect, c.toMap()))
                        .flatMap(m -> {
                            final ResourceContext context = new ResourceContext(m);
                            return
                                    // 执行下载服务
                                    service.execute(context)
                                            // 执行下载操作之后的切点
                                            .flatMap(r -> after(aspect, context.toMap(), r));
                        })
                        // 获取返回结果中的文件路径
                        // 并将返回的文件路径转换为文件对象
                        .flatMap(r -> {
                            // Http Header Content-Range
                            final List<HttpRange> ranges = request.headers().range();
                            // 读取下载文件的绝对路径
                            final Object resourcePath = r.get("__path__");
                            final Object mediaType = r.get("__media_type__");
                            if (resourcePath == null) {
                                // 如果不存在下载文件路径数据则抛出异常
                                return Mono.error(new FileException(
                                        this.getClass(),
                                        "fun handle(ServerRequest request). -> Resource file path is null.",
                                        "Resource file path is null"
                                ));
                            } else if (resourcePath instanceof final String dps) {
                                final File file = new File(dps);
                                // 验证文件是否存在
                                if (FileUtil.checkFileExist(file)) {
                                    if (mediaType instanceof final MediaType my) {
                                        return Mono.just(new ResourceModel().setFile(file).setRanges(ranges).setMediaType(my));
                                    } else {
                                        return Mono.error(new MediaTypeException(
                                                this.getClass(),
                                                "fun handle(ServerRequest request). -> Resource file media type error.",
                                                "Resource file media type error"
                                        ));
                                    }
                                } else {
                                    // 文件不存在抛出异常
                                    return Mono.error(new FileException(
                                            this.getClass(),
                                            "fun handle(ServerRequest request). -> Resource file not exist.",
                                            "Resource file not exist"
                                    ));
                                }
                            } else {
                                // 如果为其他类型的数据则抛出异常
                                return Mono.error(new FileException(
                                        this.getClass(),
                                        "fun handle(ServerRequest request). -> " +
                                                "Download file path data type not is String.",
                                        "Download file path data type not is String"
                                ));
                            }
                        })
                        // 结果返回
                        .flatMap(m -> {
                            final File file = m.getFile();
                            final MediaType mediaType = m.getMediaType();
                            final List<HttpRange> ranges = m.getRanges();
                            if (ranges != null && ranges.size() > 0) {
                                long contentLength = 0;
                                final List<String> headers = new ArrayList<>();
                                final List<Flux<DataBuffer>> fluxes = new ArrayList<>();
                                for (HttpRange range : ranges) {
                                    final long sl = range.getRangeStart(file.length());
                                    final long el = range.getRangeEnd(file.length());
                                    contentLength = el - sl + 1;
                                    fluxes.add(FileUtil.readFile(file, sl, contentLength));
                                    headers.add("bytes " + sl + "-" + el + "/" + file.length());
                                }
                                return ServerResponse
                                        .status(HttpStatus.PARTIAL_CONTENT)
                                        .contentType(mediaType)
                                        .contentLength(contentLength)
                                        .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                                        .header(HttpHeaders.CONTENT_RANGE, headers.toArray(new String[0]))
                                        .body((response, context) -> response.writeWith(Flux.concat(fluxes)));
                            } else {
                                return ServerResponse
                                        .ok()
                                        .contentType(mediaType)
                                        .body((response, context) -> response.writeWith(FileUtil.readFile(file)));
                            }
                        });
    }

    /**
     * 资源模型
     */
    @Data
    @Accessors(chain = true)
    private static class ResourceModel implements Serializable {
        private File file;
        private MediaType mediaType;
        private List<HttpRange> ranges;
    }

}
