package club.p6e.coat.file.error;

/**
 * 自定义异常
 * 资源节点异常
 *
 * @author lidashuang
 * @version 1.0
 */
public class ResourceNodeException extends CustomException {

    /**
     * 默认的代码
     */
    public static final int DEFAULT_CODE = 12000;

    /**
     * 默认的简述
     */
    private static final String DEFAULT_SKETCH = "RESOURCE_NODE_EXCEPTION";

    /**
     * 资源节点异常
     *
     * @param sc      源 class
     * @param content 异常内容
     */
    public ResourceNodeException(Class<?> sc, String error, String content) {
        super(sc, ResourceNodeException.class, error, DEFAULT_CODE, DEFAULT_SKETCH, content);
    }

    /**
     * 资源节点异常
     *
     * @param sc        源 class
     * @param throwable 异常对象
     */
    public ResourceNodeException(Class<?> sc, Throwable throwable, String content) {
        super(sc, ResourceNodeException.class, throwable, DEFAULT_CODE, DEFAULT_SKETCH, content);
    }

    /**
     * 资源节点异常
     *
     * @param sc      源 class
     * @param content 异常内容
     * @param code    代码
     * @param sketch  简述
     */
    public ResourceNodeException(Class<?> sc, String error, int code, String sketch, String content) {
        super(sc, ResourceNodeException.class, error, code, sketch, content);
    }

    /**
     * 资源节点异常
     *
     * @param sc        源 class
     * @param throwable 异常对象
     * @param code      代码
     * @param sketch    简述
     */
    public ResourceNodeException(Class<?> sc, Throwable throwable, int code, String sketch, String content) {
        super(sc, ResourceNodeException.class, throwable, code, sketch, content);
    }
}
