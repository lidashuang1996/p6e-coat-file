package club.p6e.coat.file;

import club.p6e.coat.file.task.FileSliceCleanTaskStrategyService;
import club.p6e.coat.file.utils.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.stereotype.Component;

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
        SpringUtil.init(event.getApplicationContext());
        final Properties properties = SpringUtil.getBean(Properties.class);
        LOGGER.info("=============================================================");
        LOGGER.info("=============================================================");
        LOGGER.info("--------- " + Properties.class + " ---------");
        LOGGER.info("host.list: " + properties.getHost().getList());
        LOGGER.info("host.enable: " + properties.getHost().isEnable());
        LOGGER.info("referer.list: " + properties.getReferer().getList());
        LOGGER.info("referer.enable: " + properties.getReferer().isEnable());
        LOGGER.info("cross-domain.enable: " + properties.getCrossDomain().isEnable());
        LOGGER.info("slice-upload.path: " + properties.getSliceUpload().getPath());
        LOGGER.info("slice-upload.max-size: " + properties.getSliceUpload().getMaxSize());
        for (final String key : properties.getUploads().keySet()) {
            LOGGER.info("uploads." + key + ": " + properties.getUploads().get(key));
        }
        for (final String key : properties.getDownloads().keySet()) {
            LOGGER.info("downloads." + key + ": " + properties.getDownloads().get(key));
        }
        for (final String key : properties.getResources().keySet()) {
            LOGGER.info("resources." + key + ": " + properties.getResources().get(key));
        }
        LOGGER.info("=============================================================");
        LOGGER.info("=============================================================");
        final FileSliceCleanTaskStrategyService strategy = SpringUtil.getBean(FileSliceCleanTaskStrategyService.class);
        LOGGER.info("File slice cleaning task enabled.");
        LOGGER.info("File slice cleaning task strategy is [ " + strategy.getClass() + " ].");
        LOGGER.info("=============================================================");
        LOGGER.info("=============================================================");
        LOGGER.info("Initialization completed.");
    }

}
