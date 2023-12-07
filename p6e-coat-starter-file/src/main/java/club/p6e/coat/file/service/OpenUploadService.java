package club.p6e.coat.file.service;

import club.p6e.coat.file.context.OpenUploadContext;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 分片上传服务
 * 步骤1: 打开分片上传操作
 *
 * @author lidashuang
 * @version 1.0
 */
public interface OpenUploadService {

    /**
     * 执行打开上传操作
     *
     * @param context 打开分片上传上下文对象
     * @return 结果对象
     */
    public Mono<Map<String, Object>> execute(OpenUploadContext context);

}
