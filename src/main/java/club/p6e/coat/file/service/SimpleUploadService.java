package club.p6e.coat.file.service;

import club.p6e.coat.file.context.SimpleUploadContext;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 简单（小文件）上传
 *
 * @author lidashuang
 * @version 1.0
 */
public interface SimpleUploadService {

    /**
     * 执行简单（小文件）上传操作
     *
     * @param context 简单（小文件）上传上下文对象
     * @return 结果对象
     */
    public Mono<Map<String, Object>> execute(SimpleUploadContext context);

}
