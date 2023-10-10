package club.p6e.coat.file;

import org.springframework.scheduling.annotation.Scheduled;

/**
 * 文件分片清除任务
 *
 * @author lidashuang
 * @version 1.0
 */
public class FileSliceCleanTask {

    /**
     * 文件分片清除策略对象
     */
    private final FileSliceCleanStrategyService strategy;

    /**
     * 构造方法初始化
     *
     * @param strategy 文件分片清除策略对象
     */
    public FileSliceCleanTask(FileSliceCleanStrategyService strategy) {
        this.strategy = strategy;
    }

    /**
     * 定时任务执行
     * 定时任务是初始化 5S 执行一次后，每次间隔 8 分钟执行一次
     */
    @Scheduled(initialDelay = 5_000, fixedDelay = 8 * 60_000)
    public void execute() {
        try {
            if (this.strategy.time()) {
                this.strategy.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
