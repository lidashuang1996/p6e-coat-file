package club.p6e.coat.file.context;

import lombok.Getter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 打开上传上下文对象
 *
 * @author lidashuang
 * @version 1.0
 */
@Getter
public class OpenUploadContext extends HashMap<String, Object> implements Serializable {

    /**
     * 上传文件名称
     */
    private String name;

    /**
     * 无参数构造
     */
    public OpenUploadContext() {
    }

    /**
     * map 初始化构造
     *
     * @param map 初始化对象
     */
    public OpenUploadContext(Map<String, Object> map) {
        this.putAll(map);
        if (map.get("name") != null && map.get("name") instanceof final String content) {
            this.setName(content);
        }
    }

    public void setName(String name) {
        this.name = name;
        this.put("name", name);
    }

    public Map<String, Object> toMap() {
        return this;
    }

}
