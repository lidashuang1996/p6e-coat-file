package club.p6e.coat.file.router;

import club.p6e.coat.file.handler.CloseUploadHandlerFunction;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * 关闭上传操作路由函数
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = CloseUploadRouterFunction.class,
        ignored = CloseUploadRouterFunction.class
)
public class CloseUploadRouterFunction extends BaseRouterFunction implements RouterFunction<ServerResponse> {

    /**
     * 构造方法初始化
     *
     * @param handlerFunction 处理器函数对象
     */
    public CloseUploadRouterFunction(CloseUploadHandlerFunction handlerFunction) {
        super(RequestPredicates.POST("/upload/slice/close")
                .or(RequestPredicates.DELETE("/upload/slice/close"))
                .or(RequestPredicates.POST("/upload/slice/close/{id}"))
                .or(RequestPredicates.DELETE("/upload/slice/close/{id}")), handlerFunction);
    }

}