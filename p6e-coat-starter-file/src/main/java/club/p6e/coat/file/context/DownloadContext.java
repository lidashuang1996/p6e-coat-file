package club.p6e.coat.file.context;

import lombok.Getter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 下载上下文对象
 *
 * @author lidashuang
 * @version 1.0
 */
@Getter
public class DownloadContext extends HashMap<String, Object> implements Serializable {

    /**
     * 下载的文件节点
     */
    private String node;

    /**
     * 下载的文件路径
     */
    private String path;

    /**
     * 无参数构造
     */
    public DownloadContext() {
    }

    /**
     * 构造函数初始化
     *
     * @param map 初始化对象
     */
    public DownloadContext(Map<String, Object> map) {
        this.putAll(map);
        if (map.get("node") != null && map.get("node") instanceof final String content) {
            this.setNode(content);
        }
        if (map.get("path") != null && map.get("path") instanceof final String content) {
            this.setPath(content);
        }
    }

    public void setNode(String node) {
        this.node = node;
        if (node == null) {
            remove("node");
        } else {
            this.put("node", node);
        }
    }

    public void setPath(String path) {
        this.path = path;
        if (path == null) {
            remove("path");
        } else {
            this.put("path", path);
        }
    }

}
