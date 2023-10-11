package club.p6e.coat.file.router;

import club.p6e.coat.file.error.CustomException;
import club.p6e.coat.file.handler.AspectHandlerFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

/**
 * 基础路由函数
 *
 * @author lidashuang
 * @version 1.0
 */
@SuppressWarnings("ALL")
public class BaseRouterFunction implements RouterFunction<ServerResponse> {

    /**
     * 日志对象
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(BaseRouterFunction.class);

    /**
     * 请求谓词对象
     */
    private RequestPredicate predicate;

    /**
     * 处理函数对象
     */
    private HandlerFunction<ServerResponse> handlerFunction;

    /**
     * 构造方法初始化
     *
     * @param predicate 请求谓词对象
     */
    public BaseRouterFunction(RequestPredicate predicate) {
        this.predicate = predicate;
        this.handlerFunction = request -> ServerResponse.ok().bodyValue(AspectHandlerFunction.ResultContext.build());
    }

    /**
     * 构造方法初始化
     *
     * @param predicate       请求谓词对象
     * @param handlerFunction 处理函数对象
     */
    public BaseRouterFunction(RequestPredicate predicate, HandlerFunction<ServerResponse> handlerFunction) {
        this.predicate = predicate;
        this.setHandlerFunction(handlerFunction);
    }

    /**
     * 请求路由
     *
     * @param request ServerRequest 对象
     * @return HandlerFunction 对象
     */
    @Override
    public @NonNull Mono<HandlerFunction<ServerResponse>> route(@NonNull ServerRequest request) {
        if (this.predicate.test(request)) {
            return Mono.just(this.handlerFunction);
        } else {
            return Mono.empty();
        }
    }

    @Override
    public void accept(RouterFunctions.Visitor visitor) {
        visitor.route(this.predicate, this.handlerFunction);
    }

    /**
     * 设置请求谓词对象
     *
     * @param predicate 请求谓词对象
     */
    public void setPredicate(RequestPredicate predicate) {
        this.predicate = predicate;
    }

    /**
     * 设置处理函数对象
     *
     * @param handlerFunction 处理函数对象
     */
    public void setHandlerFunction(HandlerFunction<ServerResponse> handlerFunction) {
        this.handlerFunction = request ->
                handlerFunction.handle(request)
                        .onErrorResume(this::throwableHandler);
    }


    /**
     * 异常处理器
     *
     * @param throwable 异常对象
     * @return Mono<ServerResponse> ServerResponse 对象
     */
    @SuppressWarnings("ALL")
    private Mono<ServerResponse> throwableHandler(Throwable throwable) {
        LOGGER.error(throwable.getMessage());
        throwable = CustomException.transformation(throwable);
        if (throwable instanceof final CustomException ce) {
            final AspectHandlerFunction.ResultContext result =
                    AspectHandlerFunction.ResultContext.build(ce.getCode(), ce.getSketch(), ce.getContent());
            return ServerResponse.ok().bodyValue(result);
        } else {
            throwable.printStackTrace();
            final AspectHandlerFunction.ResultContext result =
                    AspectHandlerFunction.ResultContext.build(500, "SERVICE_EXCEPTION", "");
            return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).bodyValue(result);
        }
    }
}
