package club.p6e.coat.file;

import club.p6e.coat.file.task.FileSliceCleanTaskStrategyService;
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
        LOGGER.info("=============================================================");
        LOGGER.info("--------- " + Properties.class + " ---------");
        final Properties.SliceUpload slice = properties.getSliceUpload();
        LOGGER.info("SliceUpload:");
        LOGGER.info("        path: " + slice.getPath());
        LOGGER.info("        max-size: " + slice.getMaxSize());
        final Map<String, Properties.Upload> uploads = properties.getUploads();
        LOGGER.info("Upload:");
        for (final String key : uploads.keySet()) {
            LOGGER.info("    " + key + ": ");
            final Properties.Upload upload = uploads.get(key);
            LOGGER.info("        type: " + upload.getType());
            LOGGER.info("        path: " + upload.getPath());
            LOGGER.info("        extend: ");
            for (final String eKey : upload.getExtend().keySet()) {
                LOGGER.info("              " + eKey + ": " + upload.getExtend().get(eKey));
            }
        }
        final Map<String, Properties.Resource> resources = properties.getResources();
        LOGGER.info("Resources:");
        for (final String key : resources.keySet()) {
            LOGGER.info("    " + key + ": ");
            final Properties.Resource resource = resources.get(key);
            LOGGER.info("        type: " + resource.getType());
            LOGGER.info("        path: " + resource.getPath());
            LOGGER.info("        suffixes: ");
            for (final String sKey : resource.getSuffixes().keySet()) {
                LOGGER.info("              " + sKey + ": " + resource.getSuffixes().get(sKey));
            }
            LOGGER.info("        extend: ");
            for (final String eKey : resource.getExtend().keySet()) {
                LOGGER.info("              " + eKey + ": " + resource.getExtend().get(eKey));
            }
        }
        final Map<String, Properties.Download> downloads = properties.getDownloads();
        LOGGER.info("Downloads:");
        for (final String key : downloads.keySet()) {
            LOGGER.info("    " + key + ": ");
            final Properties.Download download = downloads.get(key);
            LOGGER.info("        path: " + download.getPath());
            LOGGER.info("        type: " + download.getType());
            LOGGER.info("        extend: ");
            for (final String eKey : download.getExtend().keySet()) {
                LOGGER.info("              " + eKey + ": " + download.getExtend().get(eKey));
            }
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
