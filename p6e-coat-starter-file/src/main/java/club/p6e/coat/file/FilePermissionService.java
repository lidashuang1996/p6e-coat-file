package club.p6e.coat.file;

import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * @author lidashuang
 * @version 1.0
 */
public interface FilePermissionService {

    /**
     * 权限验证的执行
     *
     * @param type    权限类型
     * @param context 权限参数
     * @return 是否具备权限
     */
    public Mono<Boolean> execute(String type, Map<String, Object> context);

}
