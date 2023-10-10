package club.p6e.coat.file.aspect;

import club.p6e.coat.file.Properties;
import club.p6e.coat.file.utils.FileUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.Map;

/**
 * 切面（钩子）的默认实现
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = CloseUploadAspect.class,
        ignored = DefaultCloseUploadAspectImpl.class
)
public class DefaultCloseUploadAspectImpl implements CloseUploadAspect {

    /**
     * 配置文件对象
     */
    private final Properties properties;

    /**
     * 构造方法初始化
     *
     * @param properties 配置文件对象
     */
    public DefaultCloseUploadAspectImpl(Properties properties) {
        this.properties = properties;
    }

    @Override
    public Mono<Boolean> before(Map<String, Object> data) {
        return Mono.just(true);
    }

    @Override
    public Mono<Boolean> after(Map<String, Object> data, Map<String, Object> result) {
        if (result != null
                && result.get("id") != null
                && result.get("name") != null
                && result.get("name") instanceof final String name
                && result.get("storageLocation") != null
                && result.get("storageLocation") instanceof final String storageLocation
                && result.get("files") != null
                && result.get("files") instanceof final File[] files) {
            final Object id = result.get("id");
            final String filePath = FileUtil.composePath(storageLocation, name);
            final String absoluteFilePath = FileUtil.convertAbsolutePath(
                    FileUtil.composePath(properties.getSliceUpload().getPath(), filePath));
            result.clear();
            result.put("id", id);
            result.put("path", filePath);
            return FileUtil
                    .mergeFileSlice(files, absoluteFilePath)
                    .map(f -> true);
        } else {
            if (result != null) {
                result.clear();
            }
            return Mono.just(true);
        }
    }

}
