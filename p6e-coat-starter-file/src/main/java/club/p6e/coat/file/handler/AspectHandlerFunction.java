package club.p6e.coat.file.handler;

import club.p6e.coat.common.error.AspectContactException;
import club.p6e.coat.file.aspect.Aspect;
import lombok.Data;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.Comparator;
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
     * 需要清除的请求参数名称
     */
    private static final List<String> CLEAN_REQUEST_PARAM_NAME = new CopyOnWriteArrayList<>(
            List.of("$id", "$node", "$operator")
    );
    private static final Logger LOGGER = LoggerFactory.getLogger(AspectHandlerFunction.class);

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
     * @param aspects 切面列表对象
     * @param data    参数对象
     * @return 结果对象
     */
    public Mono<Map<String, Object>> before(List<? extends Aspect> aspects, Map<String, Object> data) {
        aspects.sort(Comparator.comparingInt(Aspect::order));
        if (data == null) {
            data = new HashMap<>(0);
        }
        for (final String name : CLEAN_REQUEST_PARAM_NAME) {
            data.remove(name);
        }
        return before(aspects, data, 0);
    }

    /**
     * 运行之前的切点处理
     *
     * @param aspects 切面列表对象
     * @param data    参数对象
     * @param index   执行的当前序号
     * @return 结果对象
     */
    private Mono<Map<String, Object>> before(List<? extends Aspect> aspects, Map<String, Object> data, int index) {
        if (index < aspects.size()) {
            LOGGER.info("AspectHandlerFunction before >>>> {} :: {} :: {} ", aspects, data, index);
            return aspects
                    .get(index)
                    .before(data)
                    .flatMap(b -> {
                        if (b) {
                            return before(aspects, data, index + 1);
                        } else {
                            return Mono.error(new AspectContactException(
                                    this.getClass(),
                                    "fun before(List<? extends Aspect> aspects, Map<String, Object> data, int index). " +
                                            "==> before(...) action before intercept return false exception.",
                                    "before(...) action before intercept return false exception."
                            ));
                        }
                    });
        } else {
            return Mono.just(data);
        }
    }

    /**
     * 运行之后的切点处理
     *
     * @param aspects 切面列表对象
     * @param data    参数对象
     * @param result  返回对象
     * @return 结果对象
     */
    public Mono<Map<String, Object>> after(List<? extends Aspect> aspects, Map<String, Object> data, Map<String, Object> result) {
        aspects.sort(Comparator.comparingInt(Aspect::order));
        if (data == null) {
            data = new HashMap<>(0);
        }
        if (result == null) {
            result = new HashMap<>(0);
        }
        return after(aspects, data, result, 0);
    }

    /**
     * 运行之后的切点处理
     *
     * @param aspects 切面列表对象
     * @param data    参数对象
     * @param result  返回对象
     * @param index   执行的当前序号
     * @return 结果对象
     */
    public Mono<Map<String, Object>> after(List<? extends Aspect> aspects, Map<String, Object> data, Map<String, Object> result, int index) {
        if (index < aspects.size()) {
            LOGGER.info("AspectHandlerFunction after >>>> {} :: {} :: {} :: {}", aspects, data, result, index);
            return aspects
                    .get(index)
                    .after(data, result)
                    .flatMap(b -> {
                        if (b) {
                            return after(aspects, data, result, index + 1);
                        } else {
                            return Mono.error(new AspectContactException(
                                    this.getClass(),
                                    "fun after(List<? extends Aspect> aspects, Map<String, Object> data, Map<String, Object> result, int index). " +
                                            "==> after(...) action after intercept return false exception.",
                                    "after(...) action after intercept return false exception."
                            ));
                        }
                    });
        } else {
            return Mono.just(result);
        }
    }

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
    }
}
