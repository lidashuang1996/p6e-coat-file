package club.p6e.coat.file.context;

import lombok.Getter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 关闭上传上下文对象
 *
 * @author lidashuang
 * @version 1.0
 */
@Getter
public class CloseUploadContext extends HashMap<String, Object> implements Serializable {

    /**
     * 上传编号
     */
    private Integer id;

    /**
     * 无参数构造
     */
    public CloseUploadContext() {
    }

    /**
     * 构造函数初始化
     *
     * @param map 初始化对象
     */
    public CloseUploadContext(Map<String, Object> map) {
        this.putAll(map);
        if (map.get("id") != null && map.get("id") instanceof final Integer content) {
            this.setId(content);
        }
    }

    public void setId(Integer id) {
        this.id = id;
        this.put("id", id);
    }

    public Map<String, Object> toMap() {
        return this;
    }

}
