package club.p6e.coat.file.error;

/**
 * 自定义异常
 * 下载节点异常
 *
 * @author lidashuang
 * @version 1.0
 */
public class DownloadNodeException extends CustomException {

    /**
     * 默认的代码
     */
    public static final int DEFAULT_CODE = 11000;

    /**
     * 默认的简述
     */
    private static final String DEFAULT_SKETCH = "DOWNLOAD_NODE_EXCEPTION";

    /**
     * 下载节点异常
     *
     * @param sc      源 class
     * @param content 异常内容
     */
    public DownloadNodeException(Class<?> sc, String error, String content) {
        super(sc, DownloadNodeException.class, error, DEFAULT_CODE, DEFAULT_SKETCH, content);
    }

    /**
     * 下载节点异常
     *
     * @param sc        源 class
     * @param throwable 异常对象
     */
    public DownloadNodeException(Class<?> sc, Throwable throwable, String content) {
        super(sc, DownloadNodeException.class, throwable, DEFAULT_CODE, DEFAULT_SKETCH, content);
    }

    /**
     * 下载节点异常
     *
     * @param sc      源 class
     * @param content 异常内容
     * @param code    代码
     * @param sketch  简述
     */
    public DownloadNodeException(Class<?> sc, String error, int code, String sketch, String content) {
        super(sc, DownloadNodeException.class, error, code, sketch, content);
    }

    /**
     * 下载节点异常
     *
     * @param sc        源 class
     * @param throwable 异常对象
     * @param code      代码
     * @param sketch    简述
     */
    public DownloadNodeException(Class<?> sc, Throwable throwable, int code, String sketch, String content) {
        super(sc, DownloadNodeException.class, throwable, code, sketch, content);
    }

}
