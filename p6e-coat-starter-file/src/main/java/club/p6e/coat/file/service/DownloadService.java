package club.p6e.coat.file.service;

import club.p6e.coat.file.actuator.FileReadActuator;
import club.p6e.coat.file.context.DownloadContext;
import reactor.core.publisher.Mono;

/**
 * 下载
 *
 * @author lidashuang
 * @version 1.0
 */
public interface DownloadService {

    /**
     * 执行下载操作
     *
     * @param context 下载上下文对象
     * @return 结果对象
     */
    public Mono<FileReadActuator> execute(DownloadContext context);

}
