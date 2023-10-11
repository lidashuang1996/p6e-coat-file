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

/**
 * 关闭上传操作处理程序函数
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
     * 关闭切面对象
     */
    private final CloseUploadAspect aspect;

    /**
     * 关闭上传服务对象
     */
    private final CloseUploadService service;

    /**
     * 构造函数初始化
     *
     * @param aspect  关闭上传切面对象
     * @param service 关闭上传服务对象
     */
    public CloseUploadHandlerFunction(CloseUploadAspect aspect, CloseUploadService service) {
        this.aspect = aspect;
        this.service = service;
    }

    @NonNull
    @Override
    public Mono<ServerResponse> handle(@NonNull ServerRequest request) {
        return
                // 通过请求参数映射器获取上下文对象
                RequestParameterMapper.execute(request, CloseUploadContext.class)
                        // 执行关闭上传操作之前的切点
                        .flatMap(c -> before(aspect, c))
                        .flatMap(m -> {
                            final CloseUploadContext context = new CloseUploadContext(m);
                            return
                                    // 执行关闭上传服务
                                    service.execute(context)
                                            // 执行关闭上传操作之后的切点
                                            .flatMap(r -> after(aspect, context, r));
                        })
                        // 结果返回
                        .flatMap(r -> ServerResponse.ok().bodyValue(ResultContext.build(r)));
    }

}
