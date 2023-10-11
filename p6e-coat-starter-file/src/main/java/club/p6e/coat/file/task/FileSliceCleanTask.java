package club.p6e.coat.file.task;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

/**
 * 文件分片清除任务
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = FileSliceCleanTask.class,
        ignored = FileSliceCleanTask.class
)
public class FileSliceCleanTask {

    /**
     * 构造方法初始化
     *
     * @param taskScheduler                 任务调度器对象
     * @param fileSliceCleanStrategyService 文件分片清除策略对象
     */
    public FileSliceCleanTask(TaskScheduler taskScheduler, FileSliceCleanTaskStrategyService fileSliceCleanStrategyService) {
        taskScheduler.schedule(fileSliceCleanStrategyService::execute, new CronTrigger(fileSliceCleanStrategyService.cron()));
    }

}
