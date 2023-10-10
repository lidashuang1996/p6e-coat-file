package club.p6e.coat.file.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传模型
 *
 * @author lidashuang
 * @version 1.0
 */
@Data
@Table(UploadModel.TABLE)
@Accessors(chain = true)
public class UploadModel implements Serializable {

    public static final String TABLE = "p6e_file_upload";

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String SIZE = "size";
    public static final String SOURCE = "source";
    public static final String OWNER = "owner";
    public static final String STORAGE_LOCATION = "storageLocation";
    public static final String CREATE_DATE = "createDate";
    public static final String UPDATE_DATE = "updateDate";
    public static final String OPERATOR = "operator";
    public static final String LOCK = "lock";
    public static final String VERSION = "version";
    public static final String RUBBISH = "rubbish";

    @Id
    private Integer id;
    private String name;
    private Long size;
    private String source;
    private String storageLocation;
    private String owner;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
    private String operator;
    private Integer lock;
    private Integer version;
    private Integer rubbish;

    public Map<String, Object> toMap() {
        final Map<String, Object> map = new HashMap<>(10);
        map.put("id", id);
        map.put("name", name);
        map.put("size", size);
        map.put("source", source);
        map.put("storageLocation", storageLocation);
        map.put("owner", owner);
        map.put("createDate", createDate);
        map.put("updateDate", updateDate);
        map.put("operator", operator);
        map.put("lock", lock);
        map.put("version", version);
        map.put("rubbish", rubbish);
        return map;
    }

}
