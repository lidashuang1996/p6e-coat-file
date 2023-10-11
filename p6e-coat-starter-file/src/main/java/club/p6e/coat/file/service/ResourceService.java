package club.p6e.coat.file.service;

import club.p6e.coat.file.actuator.FileReadActuator;
import club.p6e.coat.file.context.ResourceContext;
import reactor.core.publisher.Mono;

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
    public Mono<FileReadActuator> execute(ResourceContext context);

}
