package club.p6e.coat.file.context;

import org.springframework.http.codec.multipart.FilePart;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 简单（小文件）上传上下文对象
 * @author lidashuang
 * @version 1.0
 */
public class SimpleUploadContext extends HashMap<String, Object> implements Serializable {

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
     * map 初始化构造
     *
     * @param map 初始化对象
     */
    public SimpleUploadContext(Map<String, Object> map) {
        this.putAll(map);
        if (map.get("filePart") != null && map.get("filePart") instanceof final FilePart fp) {
            this.setFilePart(fp);
        }
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
