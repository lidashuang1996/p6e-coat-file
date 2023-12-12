package club.p6e.coat.file.handler;

import club.p6e.coat.file.aspect.SimpleUploadAspect;
import club.p6e.coat.file.context.SimpleUploadContext;
import club.p6e.coat.file.mapper.RequestParameterMapper;
import club.p6e.coat.file.service.SimpleUploadService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 简单（小文件）上传-处理函数
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = SimpleUploadHandlerFunction.class,
        ignored = SimpleUploadHandlerFunction.class
)
public class SimpleUploadHandlerFunction extends AspectHandlerFunction implements HandlerFunction<ServerResponse> {

    /**
     * 简单（小文件）上传服务对象
     */
    private final SimpleUploadService service;

    /**
     * 简单（小文件）上传切面列表对象
     */
    private final List<SimpleUploadAspect> aspects;

    /**
     * 构造函数初始化
     *
     * @param aspects 简单（小文件）上传切面列表对象
     * @param service 简单（小文件）上传服务对象
     */
    public SimpleUploadHandlerFunction(SimpleUploadService service, List<SimpleUploadAspect> aspects) {
        this.service = service;
        this.aspects = aspects;
    }

    @NonNull
    @Override
    public Mono<ServerResponse> handle(@NonNull ServerRequest request) {
        return
                // 通过请求参数映射器获取上下文对象
                RequestParameterMapper.execute(request, SimpleUploadContext.class)
                        // 执行简单（小文件）上传之前的切点
                        .flatMap(c -> before(aspects, c))
                        .flatMap(m -> {
                            final SimpleUploadContext context = new SimpleUploadContext(m);
                            return
                                    // 执行简单（小文件）上传服务
                                    service.execute(context)
                                            // 执行简单（小文件）上传之后的切点
                                            .flatMap(r -> after(aspects, context, r));
                        })
                        // 结果返回
                        .flatMap(r -> ServerResponse.ok().bodyValue(ResultContext.build(r)));
    }

}
