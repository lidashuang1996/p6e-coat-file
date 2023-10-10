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
 * 文件块上传模型
 *
 * @author lidashuang
 * @version 1.0
 */
@Data
@Table(UploadChunkModel.TABLE)
@Accessors(chain = true)
public class UploadChunkModel implements Serializable {

    public static final String TABLE = "p6e_file_upload_chunk";

    public static final String ID = "id";
    public static final String FID = "fid";
    public static final String NAME = "name";
    public static final String SIZE = "size";
    public static final String DATE = "date";
    public static final String OPERATOR = "operator";

    @Id
    private Integer id;
    private Integer fid;
    private String name;
    private Long size;
    private LocalDateTime date;
    private String operator;

    public Map<String, Object> toMap() {
        final Map<String, Object> map = new HashMap<>(6);
        map.put("id", id);
        map.put("fid", fid);
        map.put("name", name);
        map.put("size", size);
        map.put("date", date);
        map.put("operator", operator);
        return map;
    }

}
