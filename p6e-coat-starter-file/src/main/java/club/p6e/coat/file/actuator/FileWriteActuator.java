package club.p6e.coat.file.actuator;

import reactor.core.publisher.Mono;

import java.io.File;

/**
 * 文件写入执行器
 *
 * @author lidashuang
 * @version 1.0
 */
public interface FileWriteActuator {

    public String type();


    public String path();

    /**
     * 执行写入操作
     *
     * @param file 被写入的文件对象
     * @return 写入后的文件对象
     */
    public Mono<File> execute(File file);

}
