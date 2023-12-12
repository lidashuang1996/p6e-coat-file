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

import java.util.List;

/**
 * 打开分片上传-处理函数
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
     * 打开分片上传服务对象
     */
    private final OpenUploadService service;

    /**
     * 打开分片上传切面列表对象
     */
    private final List<OpenUploadAspect> aspects;

    /**
     * 构造函数初始化
     *
     * @param service 打开分片上传服务对象
     * @param aspects 打开分片上传切面对象
     */
    public OpenUploadHandlerFunction(OpenUploadService service, List<OpenUploadAspect> aspects) {
        this.service = service;
        this.aspects = aspects;
    }

    @NonNull
    @Override
    public Mono<ServerResponse> handle(@NonNull ServerRequest request) {
        return
                // 通过请求参数映射器获取上下文对象
                RequestParameterMapper.execute(request, OpenUploadContext.class)
                        // 执行打开分片上传之前的切点
                        .flatMap(c -> before(aspects, c))
                        .flatMap(m -> {
                            final OpenUploadContext context = new OpenUploadContext(m);
                            return
                                    // 执行打开分片上传
                                    service.execute(context)
                                            // 执行打开分片上传之后的切点
                                            .flatMap(r -> after(aspects, context, r));
                        })
                        // 结果返回
                        .flatMap(r -> ServerResponse.ok().bodyValue(ResultContext.build(r)));
    }

}
