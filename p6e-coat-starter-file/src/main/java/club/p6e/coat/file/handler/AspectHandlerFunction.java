package club.p6e.coat.file.handler;

import club.p6e.coat.file.aspect.Aspect;
import club.p6e.coat.file.error.AspectContactException;
import lombok.Data;
import lombok.experimental.Accessors;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 切面处理函数
 *
 * @author lidashuang
 * @version 1.0
 */
public class AspectHandlerFunction {

    /**
     * 结果的模型对象
     */
    @Data
    @Accessors(chain = true)
    public static final class ResultContext implements Serializable {

        /**
         * 默认的状态码
         */
        private static final int DEFAULT_CODE = 0;

        /**
         * 默认的消息内容
         */
        private static final String DEFAULT_MESSAGE = "SUCCESS";

        /**
         * 默认的数据内容
         */
        private static final String DEFAULT_DATA = null;

        /**
         * 状态码
         */
        private Integer code;

        /**
         * 消息
         */
        private String message;

        /**
         * 数据的对象
         */
        private Object data;

        /**
         * 编译方法
         *
         * @return 结果上下文对象
         */
        public static ResultContext build() {
            return new ResultContext(DEFAULT_CODE, DEFAULT_MESSAGE, DEFAULT_DATA);
        }

        /**
         * 编译方法
         *
         * @param data 数据的对象
         * @return 结果上下文对象
         */
        public static ResultContext build(Object data) {
            return new ResultContext(DEFAULT_CODE, DEFAULT_MESSAGE, data);
        }

        /**
         * 编译方法
         *
         * @param code    消息状态码
         * @param message 消息内容
         * @param data    数据的对象
         * @return 结果上下文对象
         */
        public static ResultContext build(Integer code, String message, Object data) {
            return new ResultContext(code, message, data);
        }

        /**
         * 构造方法初始化
         *
         * @param code    状态码
         * @param message 消息
         * @param data    数据的对象
         */
        private ResultContext(Integer code, String message, Object data) {
            this.code = code;
            this.message = message;
            this.data = data;
        }
    }

    /**
     * 需要清除的请求参数名称
     */
    private static final List<String> CLEAN_REQUEST_PARAM_NAME = new CopyOnWriteArrayList<>(
            List.of("operator")
    );

    /**
     * 添加需要清除的请求参数名称
     *
     * @param name 请求参数名称
     */
    @SuppressWarnings("ALL")
    private static void addCleanRequestParamName(String name) {
        CLEAN_REQUEST_PARAM_NAME.add(name);
    }

    /**
     * 移除需要清除的请求参数名称
     *
     * @param name 请求参数名称
     */
    @SuppressWarnings("ALL")
    private static void removeCleanRequestParamName(String name) {
        CLEAN_REQUEST_PARAM_NAME.remove(name);
    }

    /**
     * 运行之前的切点处理
     *
     * @param aspect 切面对象
     * @param data   参数对象
     * @return 结果对象
     */
    public Mono<Map<String, Object>> before(Aspect aspect, Map<String, Object> data) {
        if (data == null) {
            data = new HashMap<>(0);
        }
        final Map<String, Object> bData = data;
        for (final String name : CLEAN_REQUEST_PARAM_NAME) {
            data.remove(name);
        }
        return aspect.before(bData)
                .flatMap(b -> {
                    if (b) {
                        return Mono.just(bData);
                    } else {
                        return Mono.error(new AspectContactException(
                                this.getClass(),
                                "fun before() -> Action before intercept return false/error",
                                "Aspect handler before intercept return false/error"
                        ));
                    }
                });
    }

    /**
     * 运行之后的切点处理
     *
     * @param aspect 切面对象
     * @param data   参数对象
     * @param result 返回对象
     * @return 结果对象
     */
    public Mono<Map<String, Object>> after(Aspect aspect, Map<String, Object> data, Map<String, Object> result) {
        if (data == null) {
            data = new HashMap<>(0);
        }
        if (result == null) {
            result = new HashMap<>(0);
        }
        final Map<String, Object> aData = data;
        final Map<String, Object> aResult = result;
        return aspect.after(aData, aResult)
                .flatMap(b -> {
                    if (b) {
                        return Mono.just(aResult);
                    } else {
                        return Mono.error(new AspectContactException(
                                this.getClass(),
                                "fun after() -> Action after intercept return false/error",
                                "Aspect handler after intercept return false/error"
                        ));
                    }
                });
    }

}
