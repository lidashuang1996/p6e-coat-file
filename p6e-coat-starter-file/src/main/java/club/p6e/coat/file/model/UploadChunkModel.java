package club.p6e.coat.file.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件块上传模型
 *
 * @author lidashuang
 * @version 1.0
 */
@Data
@Accessors(chain = true)
public class UploadChunkModel implements Serializable {

    private Integer id;
    private Integer fid;
    private String name;
    private Long size;
    private String creator;
    private String modifier;
    private LocalDateTime creationDateTime;
    private LocalDateTime modificationDateTime;
    private Integer version;

    public Map<String, Object> toMap() {
        final Map<String, Object> map = new HashMap<>(6);
        map.put("id", id);
        map.put("fid", fid);
        map.put("name", name);
        map.put("size", size);
        map.put("creator", creator);
        map.put("modifier", modifier);
        map.put("creationDateTime", creationDateTime);
        map.put("modificationDateTime", modificationDateTime);
        map.put("version", version);
        return map;
    }

}
