package club.p6e.coat.file;

/**
 * 文件分片清除策略服务
 *
 * @author lidashuang
 * @version 1.0
 */
public interface FileSliceCleanStrategyService {

    /**
     * 执行文件清除
     */
    public void execute();

    /**
     * 执行文件清除时间策略
     */
    public boolean time();

}
