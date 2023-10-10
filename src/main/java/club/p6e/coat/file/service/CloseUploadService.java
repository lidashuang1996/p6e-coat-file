package club.p6e.coat.file.service;

import club.p6e.coat.file.context.CloseUploadContext;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 分片上传
 * 步骤3: 关闭上传操作
 *
 * @author lidashuang
 * @version 1.0
 */
public interface CloseUploadService {

    /**
     * 执行关闭上传操作
     *
     * @param context 关闭上传上下文对象
     * @return 结果对象
     */
    public Mono<Map<String, Object>> execute(CloseUploadContext context);

}
