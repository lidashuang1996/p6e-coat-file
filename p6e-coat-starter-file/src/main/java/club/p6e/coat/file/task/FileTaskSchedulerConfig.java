package club.p6e.coat.file.task;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

/**
 * @author lidashuang
 * @version 1.0
 */
@Component
@Configuration
public class FileTaskSchedulerConfig {

    @Bean("club.p6e.coat.file.task.TaskScheduler")
    public TaskScheduler injectTaskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setThreadNamePrefix("P6E-COAT-TASK-SCHEDULER-");
        return taskScheduler;
    }

}
