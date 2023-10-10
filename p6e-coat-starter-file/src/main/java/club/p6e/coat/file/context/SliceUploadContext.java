package club.p6e.coat.file.context;

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
     * map 初始化构造
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
        this.put("id", id);
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
        this.put("index", index);
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
        this.put("signature", signature);
    }

    public FilePart getFilePart() {
        return filePart;
    }

    public void setFilePart(FilePart filePart) {
        this.filePart = filePart;
        this.put("filePart", filePart);
    }

    public Map<String, Object> toMap() {
        return this;
    }

}
