package club.p6e.coat.file.actuator;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 文件执行器模型
 *
 * @author lidashuang
 * @version 1.0
 */
@Data
@Accessors(chain = true)
public class FileActuatorModel implements Serializable {

    /**
     * 资源类型
     */
    private String type;

    /**
     * 文件名称
     */
    private String name;

    /**
     * 文件路径
     */
    private String path;

    /**
     * 文件长度
     */
    private long length;

}
