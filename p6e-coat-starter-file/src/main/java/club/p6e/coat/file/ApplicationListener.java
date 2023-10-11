package club.p6e.coat.file;

import club.p6e.coat.file.task.FileSliceCleanTaskStrategyService;
import club.p6e.coat.file.task.FileSliceCleanTask;
import club.p6e.coat.file.utils.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 事件监听
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
public class ApplicationListener implements
        org.springframework.context.ApplicationListener<ApplicationReadyEvent> {

    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationListener.class);

    /**
     * Spring Boot 初始化完成事件监听
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // 初始化上下文对象
        SpringUtil.init(event.getApplicationContext());
        // 打印配置文件的信息
        final Properties properties = SpringUtil.getBean(Properties.class);
        LOGGER.info("=============================================================");
        LOGGER.info("--------- " + Properties.class + " ---------");
        final Map<String, Properties.Resource> resources = properties.getResources();
        LOGGER.info("resources:");
        for (final String key : resources.keySet()) {
            LOGGER.info("     " + key + ":");
            final Properties.Resource resource = resources.get(key);
            LOGGER.info("          path:" + resource.getPath());
            LOGGER.info("          suffixes:" + resource.getSuffixes());
        }
        final Map<String, Properties.Download> downloads = properties.getDownloads();
        LOGGER.info("downloads:");
        for (final String key : downloads.keySet()) {
            LOGGER.info("     " + key + ":");
            final Properties.Download download = downloads.get(key);
            LOGGER.info("          path:" + download.getPath());
        }
        final Properties.SliceUpload sliceUpload = properties.getSliceUpload();
        LOGGER.info("sliceUpload:");
        LOGGER.info("     path:" + sliceUpload.getPath());
        LOGGER.info("     maxSize:" + sliceUpload.getMaxSize());
        LOGGER.info("simpleUpload:");
        LOGGER.info("--------- " + Properties.class + " ---------");
        LOGGER.info("=============================================================");
        LOGGER.info("=============================================================");
        final FileSliceCleanTaskStrategyService strategy = SpringUtil.getBean(FileSliceCleanTaskStrategyService.class);
        LOGGER.info("File slice cleaning task enabled.");
        LOGGER.info("File slice cleaning task strategy is [ " + strategy.getClass() + " ].");
        LOGGER.info("=============================================================");
        LOGGER.info("Initialization completed.");
    }

}
