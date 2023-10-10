package club.p6e.coat.file.router;

import club.p6e.coat.file.handler.SliceUploadHandlerFunction;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * 分片上传操作路由函数
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = SliceUploadRouterFunction.class,
        ignored = SliceUploadRouterFunction.class
)
public class SliceUploadRouterFunction extends BaseRouterFunction implements RouterFunction<ServerResponse> {

    /**
     * 构造方法初始化
     *
     * @param handlerFunction 处理器函数对象
     */
    public SliceUploadRouterFunction(SliceUploadHandlerFunction handlerFunction) {
        super(RequestPredicates.POST("/upload/slice")
                .or(RequestPredicates.POST("/upload/slice/{id}")), handlerFunction);
    }

}