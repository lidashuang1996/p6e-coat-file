package club.p6e.coat.file.handler;

import club.p6e.coat.file.aspect.OpenUploadAspect;
import club.p6e.coat.file.context.OpenUploadContext;
import club.p6e.coat.file.mapper.RequestParameterMapper;
import club.p6e.coat.file.service.OpenUploadService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * 打开上传操作处理程序函数
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = OpenUploadHandlerFunction.class,
        ignored = OpenUploadHandlerFunction.class
)
public class OpenUploadHandlerFunction extends AspectHandlerFunction implements HandlerFunction<ServerResponse> {

    /**
     * 打开上传切面对象
     */
    private final OpenUploadAspect aspect;

    /**
     * 打开上传服务对象
     */
    private final OpenUploadService service;

    /**
     * 构造函数初始化
     *
     * @param aspect  打开上传切面对象
     * @param service 打开上传服务对象
     */
    public OpenUploadHandlerFunction(OpenUploadAspect aspect, OpenUploadService service) {
        this.aspect = aspect;
        this.service = service;
    }

    @NonNull
    @Override
    public Mono<ServerResponse> handle(@NonNull ServerRequest request) {
        return
                // 通过请求参数映射器获取上下文对象
                RequestParameterMapper.execute(request, OpenUploadContext.class)
                        // 执行打开上传操作之前的切点
                        .flatMap(c -> before(aspect, c))
                        .flatMap(m -> {
                            final OpenUploadContext context = new OpenUploadContext(m);
                            return
                                    // 执行打开上传服务
                                    service.execute(context)
                                            // 执行打开上传操作之后的切点
                                            .flatMap(r -> after(aspect, context, r));
                        })
                        // 结果返回
                        .flatMap(r -> ServerResponse.ok().bodyValue(ResultContext.build(r)));
    }

}
