package club.p6e.coat.file;

import club.p6e.coat.file.task.FileSliceCleanTaskStrategyService;
import club.p6e.coat.file.utils.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 事件监听
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
public class ApplicationListener implements CommandLineRunner {

    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationListener.class);

    /**
     * 应用上下文对象
     */
    private final ApplicationContext context;

    /**
     * 构造方法初始化
     *
     * @param context 应用上下文对象
     */
    public ApplicationListener(ApplicationContext context) {
        this.context = context;
    }

    /**
     * Spring Boot 初始化完成事件监听
     */
    @Override
    public void run(String... args) {
        SpringUtil.init(context);
        final Properties properties = SpringUtil.getBean(Properties.class);
        LOGGER.info("=============================================================");
        LOGGER.info("=============================================================");
        LOGGER.info("--------- " + Properties.class + " ---------");
        LOGGER.info("version: " + properties.getVersion());
        LOGGER.info("referer.enable: " + properties.getReferer().isEnable());
        LOGGER.info("referer.white-list: " + properties.getReferer().getWhiteList());
        LOGGER.info("cross-domain.enable: " + properties.getCrossDomain().isEnable());
        LOGGER.info("cross-domain.white-list: " + properties.getCrossDomain().getWhiteList());
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
