package club.p6e.coat.file.context;

import lombok.Getter;
import org.springframework.http.codec.multipart.FilePart;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 分片上传上下文对象
 *
 * @author lidashuang
 * @version 1.0
 */
@Getter
public class SliceUploadContext extends HashMap<String, Object> implements Serializable {

    /**
     * 上传编号
     */
    private Integer id;

    /**
     * 分片索引
     */
    private Integer index;

    /**
     * 分片签名
     */
    private String signature;

    /**
     * 文件对象
     */
    private FilePart filePart;

    /**
     * 无参数构造
     */
    public SliceUploadContext() {
    }

    /**
     * 构造函数初始化
     *
     * @param map 初始化对象
     */
    public SliceUploadContext(Map<String, Object> map) {
        this.putAll(map);
        if (map.get("id") != null && map.get("id") instanceof final Integer content) {
            this.setId(content);
        }
        if (map.get("index") != null && map.get("index") instanceof final Integer content) {
            this.setIndex(content);
        }
        if (map.get("filePart") != null && map.get("filePart") instanceof final FilePart fp) {
            this.setFilePart(fp);
        }
        if (map.get("signature") != null && map.get("signature") instanceof final String content) {
            this.setSignature(content);
        }
    }

    public void setId(Integer id) {
        this.id = id;
        if (id == null) {
            remove("id");
        } else {
            this.put("id", id);
        }
    }

    public void setIndex(Integer index) {
        this.index = index;
        if (index == null) {
            remove("index");
        } else {
            this.put("index", index);
        }
    }

    public void setSignature(String signature) {
        this.signature = signature;
        if (signature == null) {
            remove("signature");
        } else {
            this.put("signature", signature);
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
