package club.p6e.coat.file.context;

import lombok.Getter;
import org.springframework.http.codec.multipart.FilePart;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 简单（小文件）上传上下文对象
 *
 * @author lidashuang
 * @version 1.0
 */
@Getter
public class SimpleUploadContext extends HashMap<String, Object> implements Serializable {

    /**
     * 节点
     */
    private String node;

    /**
     * 文件对象
     */
    private FilePart filePart;

    /**
     * 无参数构造
     */
    public SimpleUploadContext() {
    }

    /**
     * 构造函数初始化
     *
     * @param map 初始化对象
     */
    public SimpleUploadContext(Map<String, Object> map) {
        this.putAll(map);
        if (map.get("node") != null && map.get("node") instanceof final String content) {
            this.setNode(content);
        }
        if (map.get("filePart") != null && map.get("filePart") instanceof final FilePart fp) {
            this.setFilePart(fp);
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

    public void setFilePart(FilePart filePart) {
        this.filePart = filePart;
        if (filePart == null) {
            remove("filePart");
        } else {
            this.put("filePart", filePart);
        }
    }

}
