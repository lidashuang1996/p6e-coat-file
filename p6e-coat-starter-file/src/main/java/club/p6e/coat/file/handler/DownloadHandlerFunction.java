package club.p6e.coat.file.handler;

import club.p6e.coat.file.aspect.DownloadAspect;
import club.p6e.coat.file.context.DownloadContext;
import club.p6e.coat.file.error.FileException;
import club.p6e.coat.file.mapper.RequestParameterMapper;
import club.p6e.coat.file.service.DownloadService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 下载操作处理程序函数
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = DownloadHandlerFunction.class,
        ignored = DownloadHandlerFunction.class
)
public class DownloadHandlerFunction extends AspectHandlerFunction implements HandlerFunction<ServerResponse> {

    /**
     * 下载切面对象
     */
    private final DownloadAspect aspect;

    /**
     * 下载服务对象
     */
    private final DownloadService service;

    /**
     * 构造函数初始化
     *
     * @param aspect  下载切面对象
     * @param service 下载服务对象
     */
    public DownloadHandlerFunction(DownloadAspect aspect, DownloadService service) {
        this.aspect = aspect;
        this.service = service;
    }

    @NonNull
    @Override
    public Mono<ServerResponse> handle(@NonNull ServerRequest request) {
        return
                // 通过请求参数映射器获取上下文对象
                RequestParameterMapper.execute(request, DownloadContext.class)
                        // 执行下载操作之前的切点
                        .flatMap(c -> before(aspect, c))
                        .flatMap(m -> service
                                .execute(new DownloadContext(m))
                                .flatMap(fra -> after(aspect, m, null).map(r -> fra)))
                        .flatMap(fra -> {
                            final String fc;
                            final List<HttpRange> ranges = request.headers().range();
                            try {
                                fc = URLEncoder.encode(fra.model().getName(), StandardCharsets.UTF_8);
                            } catch (Exception e) {
                                // 忽略异常
                                return Mono.error(new FileException(
                                        this.getClass(),
                                        "fun handle(ServerRequest request). -> " +
                                                "Download file name parsing error.",
                                        "Download file name parsing error"
                                ));
                            }
                            if (!ranges.isEmpty()) {
                                final HttpRange range = ranges.get(0);
                                final long length = fra.model().getLength();
                                final long el = range.getRangeEnd(length);
                                final long sl = range.getRangeStart(length);
                                final long cl = el - sl + 1;
                                return ServerResponse
                                        .status(HttpStatus.PARTIAL_CONTENT)
                                        .contentLength(cl)
                                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                        .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                                        .header(HttpHeaders.CONTENT_RANGE, "bytes " + sl + "-" + el + "/" + length)
                                        .header("Content-Disposition", "attachment; filename=" + fc)
                                        .body((response, context) -> response.writeWith(fra.execute(sl, cl)));
                            } else {
                                return ServerResponse.ok()
                                        .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                                        .header("Content-Disposition", "attachment; filename=" + fc)
                                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                        .body((response, context) -> response.writeWith(fra.execute()));
                            }
                        });
    }
}
