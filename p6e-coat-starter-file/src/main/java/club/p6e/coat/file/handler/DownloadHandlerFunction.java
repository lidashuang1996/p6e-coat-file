package club.p6e.coat.file.handler;

import club.p6e.coat.file.aspect.DownloadAspect;
import club.p6e.coat.file.context.DownloadContext;
import club.p6e.coat.common.error.FileException;
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
 * 下载文件-处理函数
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
     * 下载文件服务对象
     */
    private final DownloadService service;

    /**
     * 下载文件切面列表对象
     */
    private final List<DownloadAspect> aspects;

    /**
     * 构造函数初始化
     *
     * @param service 下载文件服务对象
     * @param aspects 下载文件切面列表对象
     */
    public DownloadHandlerFunction(DownloadService service, List<DownloadAspect> aspects) {
        this.service = service;
        this.aspects = aspects;
    }

    @NonNull
    @Override
    public Mono<ServerResponse> handle(@NonNull ServerRequest request) {
        return
                // 通过请求参数映射器获取上下文对象
                RequestParameterMapper.execute(request, DownloadContext.class)
                        // 执行下载文件之前的切点
                        .flatMap(c -> before(aspects, c))
                        .flatMap(m -> service
                                // 执行下载文件
                                .execute(new DownloadContext(m))
                                // 执行下载文件之后的切点
                                .flatMap(fra -> after(aspects, m, null).map(r -> fra)))
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
                                                "Download file name parsing exception.",
                                        "Download file name parsing exception"
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
