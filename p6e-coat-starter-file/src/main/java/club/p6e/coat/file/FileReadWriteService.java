package club.p6e.coat.file;

import club.p6e.coat.file.actuator.FileActuatorModel;
import club.p6e.coat.file.actuator.FileReadActuator;
import club.p6e.coat.file.actuator.FileWriteActuator;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * @author lidashuang
 * @version 1.0
 */
public interface FileReadWriteService {

    /**
     * 写入文件
     *
     * @param name              文件名称
     * @param extend            扩展参数
     * @param fileWriteActuator 文件写入执行器对象
     * @return 文件模型对象
     */
    public Mono<FileActuatorModel> write(
            String name, Map<String, Object> extend, FileWriteActuator fileWriteActuator);

    /**
     * 读取文件
     *
     * @param type      资源类型
     * @param base      基础路径
     * @param path      文件路径
     * @param mediaType 媒体类型
     * @param extend    扩展参数
     * @return 文件读取执行器对象
     */
    public Mono<FileReadActuator> read(
            String type, String base, String path, MediaType mediaType, Map<String, Object> extend);

}
