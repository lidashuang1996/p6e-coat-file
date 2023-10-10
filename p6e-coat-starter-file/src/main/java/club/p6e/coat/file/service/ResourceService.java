package club.p6e.coat.file.service;

import club.p6e.coat.file.FileReadWriteService;
import club.p6e.coat.file.context.ResourceContext;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 资源查看服务
 *
 * @author lidashuang
 * @version 1.0
 */
public interface ResourceService {

    /**
     * 执行资源查看操作
     *
     * @param context 资源上下文对象
     * @return 结果对象
     */
    public Mono<FileReadWriteService.FileReadActuator> execute(ResourceContext context);

}
