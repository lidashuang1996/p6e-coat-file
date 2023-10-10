package club.p6e.coat.file.service;

import club.p6e.coat.file.context.SliceUploadContext;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 分片上传
 * 步骤2: 分片上传操作
 *
 * @author lidashuang
 * @version 1.0
 */
public interface SliceUploadService {

    /**
     * 执行分片上传操作
     *
     * @param context 分片上传上下文对象
     * @return 结果对象
     */
    public Mono<Map<String, Object>> execute(SliceUploadContext context);

}
