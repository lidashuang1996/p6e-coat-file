package club.p6e.coat.file.actuator;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;

/**
 * @author lidashuang
 * @version 1.0
 */
public interface FileReadActuator {

    /**
     * 获取文件媒体类型对象
     *
     * @return 媒体类型
     */
    public MediaType mediaType();

    /**
     * 获取文件模型对象
     *
     * @return 文件模型对象
     */
    public FileActuatorModel model();

    /**
     * 执行文件读取操作
     *
     * @return 字节码流
     */
    public Flux<DataBuffer> execute();

    /**
     * 执行文件读取操作
     *
     * @param position 读取的文件开始位置
     * @param size     读取的文件长度
     * @return 字节码流
     */
    public Flux<DataBuffer> execute(long position, long size);

}
