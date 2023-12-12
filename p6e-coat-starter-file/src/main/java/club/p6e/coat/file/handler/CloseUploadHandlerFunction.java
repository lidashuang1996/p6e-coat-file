package club.p6e.coat.file.handler;

import club.p6e.coat.file.aspect.CloseUploadAspect;
import club.p6e.coat.file.context.CloseUploadContext;
import club.p6e.coat.file.mapper.RequestParameterMapper;
import club.p6e.coat.file.service.CloseUploadService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 关闭分片上传-处理函数
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = CloseUploadHandlerFunction.class,
        ignored = CloseUploadHandlerFunction.class
)
public class CloseUploadHandlerFunction extends AspectHandlerFunction implements HandlerFunction<ServerResponse> {

    /**
     * 关闭分片上传服务对象
     */
    private final CloseUploadService service;

    /**
     * 关闭分片上传切面列表对象
     */
    private final List<CloseUploadAspect> aspects;

    /**
     * 构造函数初始化
     *
     * @param service 关闭分片上传服务对象
     * @param aspects 关闭分片上传切面列表对象
     */
    public CloseUploadHandlerFunction(CloseUploadService service, List<CloseUploadAspect> aspects) {
        this.service = service;
        this.aspects = aspects;
    }

    @NonNull
    @Override
    public Mono<ServerResponse> handle(@NonNull ServerRequest request) {
        return
                // 通过请求参数映射器获取上下文对象
                RequestParameterMapper.execute(request, CloseUploadContext.class)
                        // 执行关闭分片上传之前的切点
                        .flatMap(c -> before(aspects, c))
                        .flatMap(m -> {
                            final CloseUploadContext context = new CloseUploadContext(m);
                            return
                                    // 执行关闭分片上传
                                    service.execute(context)
                                            // 执行关闭分片上传之后的切点
                                            .flatMap(r -> after(aspects, context, r));
                        })
                        // 结果返回
                        .flatMap(r -> ServerResponse.ok().bodyValue(ResultContext.build(r)));
    }

}
