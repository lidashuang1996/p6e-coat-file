package club.p6e.coat.file;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 配置文件
 *
 * @author lidashuang
 * @version 1.0
 */
@Data
@Component
@Accessors(chain = true)
@ConfigurationProperties(prefix = "p6e.coat.file")
public class Properties implements Serializable {

    /**
     * HOST 配置
     */
    private Host host = new Host();

    /**
     * REFERER 配置
     */
    private Referer referer = new Referer();

    /**
     * 跨域配置
     */
    private CrossDomain crossDomain = new CrossDomain();

    /**
     * 分片上传配置
     */
    private SliceUpload sliceUpload = new SliceUpload();

    /**
     * 上传配置
     */
    private Map<String, Upload> uploads = new HashMap<>();

    /**
     * 资源配置
     */
    private Map<String, Resource> resources = new HashMap<>();

    /**
     * 下载配置
     */
    private Map<String, Download> downloads = new HashMap<>();

    /**
     * 跨域配置
     */
    @Data
    @Accessors(chain = true)
    public static class CrossDomain implements Serializable {

        /**
         * 是否启用
         */
        private boolean enable = false;

    }

    /**
     * HOST 配置
     */
    @Data
    @Accessors(chain = true)
    public static class Host implements Serializable {

        /**
         * 是否启用
         */
        private boolean enable = false;

        /**
         * HOST 配置
         */
        private List<String> list = new ArrayList<>();

    }

    /**
     * REFERER 配置
     */
    @Data
    @Accessors(chain = true)
    public static class Referer implements Serializable {

        /**
         * 是否启用
         */
        private boolean enable = false;

        /**
         * REFERER 配置
         */
        private List<String> list = new ArrayList<>();

    }

    /**
     * 下载
     */
    @Data
    @Accessors(chain = true)
    public static class Upload implements Serializable {

        /**
         * 资源类型
         */
        private String type;

        /**
         * 基础的文件路径
         */
        private String path;

        /**
         * 扩展参数
         */
        private Map<String, String> extend = new HashMap<>();

    }

    /**
     * 资源
     */
    @Data
    @Accessors(chain = true)
    public static class Resource implements Serializable {

        /**
         * 资源类型
         */
        private String type;

        /**
         * 基础的文件路径
         */
        private String path;

        /**
         * 扩展参数
         */
        private Map<String, String> extend = new HashMap<>();

        /**
         * 允许的文件后缀以及对应的媒体类型
         */
        private Map<String, MediaType> suffixes = new HashMap<>();

    }

    @Data
    @Accessors(chain = true)
    public static class Download implements Serializable {

        /**
         * 资源类型
         */
        private String type;

        /**
         * 基础的文件路径
         */
        private String path;

        /**
         * 扩展参数
         */
        private Map<String, String> extend = new HashMap<>();

    }

    /**
     * 分片上传
     */
    @Data
    @Accessors(chain = true)
    public static class SliceUpload implements Serializable {

        /**
         * 基础的文件路径
         */
        private String path = "/opt/data/slice";

        /**
         * 允许上传的文件大小的最大值
         */
        private long maxSize = 1024 * 1024 * 20;

    }

}