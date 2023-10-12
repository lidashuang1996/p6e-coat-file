package club.p6e.coat.file.error;

import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义异常
 *
 * @author lidashuang
 * @version 1.0
 */
@Getter
public abstract class CustomException extends RuntimeException {

    /**
     * 转型器缓存
     */
    private static final Map<Class<? extends Exception>,
            CustomException> TRANSFORMER_CACHE = new ConcurrentHashMap<>();

    /**
     * 代码
     */
    private final int code;

    /**
     * 标记
     */
    private final String sketch;

    /**
     * 内容
     */
    private final String content;

    /**
     * 转换
     *
     * @param exception 异常对象
     * @return 异常对象
     */
    public static Throwable transformation(Throwable exception) {
        final CustomException customException = TRANSFORMER_CACHE.get(exception.getClass());
        return customException == null ? exception : customException;
    }

    /**
     * 转换器
     *
     * @param ec              异常对象 class
     * @param customException 自定义异常对象
     */
    public synchronized static void registerTransformer(Class<? extends Exception> ec, CustomException customException) {
        TRANSFORMER_CACHE.put(ec, customException);
    }

    /**
     * 模版输出
     *
     * @param sc      源 class
     * @param ec      异常 class
     * @param content 错误的描述
     * @param code    代码
     * @param sketch  简述
     * @return 模版的输出内容
     */
    private static String template(Class<?> sc, Class<? extends CustomException> ec,
                                   String error, int code, String sketch, String content) {
        return "{ " + sketch + " <" + code + "> [" + content + "] } ::: [ " + sc + " ] => (" + ec + ") ===> " + error;
    }

    /**
     * 构造方法
     *
     * @param sc      源 class
     * @param ec      异常 class
     * @param error   错误的描述
     * @param code    代码
     * @param sketch  简述
     * @param content 内容
     */
    public CustomException(Class<?> sc, Class<? extends CustomException> ec,
                           String error, int code, String sketch, String content) {
        super(template(sc, ec, error, code, sketch, content));
        this.code = code;
        this.sketch = sketch;
        this.content = content;
    }

    /**
     * 构造方法
     *
     * @param sc        源 class
     * @param ec        异常 class
     * @param throwable 异常对象
     * @param code      代码
     * @param sketch    简述
     * @param content   内容
     */
    public CustomException(Class<?> sc, Class<? extends CustomException> ec,
                           Throwable throwable, int code, String sketch, String content) {
        super(template(sc, ec, throwable.getMessage(), code, sketch, content), throwable);
        this.code = code;
        this.sketch = sketch;
        this.content = content;
    }

}
