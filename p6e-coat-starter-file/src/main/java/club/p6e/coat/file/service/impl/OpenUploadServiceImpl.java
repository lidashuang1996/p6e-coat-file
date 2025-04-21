package club.p6e.coat.file.service.impl;

import club.p6e.coat.common.error.ParameterException;
import club.p6e.coat.file.context.OpenUploadContext;
import club.p6e.coat.file.model.UploadModel;
import club.p6e.coat.file.repository.UploadRepository;
import club.p6e.coat.file.service.OpenUploadService;
import club.p6e.coat.file.utils.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 分片上传服务
 * 步骤1: 打开分片上传操作
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = OpenUploadService.class,
        ignored = OpenUploadServiceImpl.class
)
public class OpenUploadServiceImpl implements OpenUploadService {

    /**
     * 源
     */
    private static final String SOURCE = "SLICE_UPLOAD";

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenUploadServiceImpl.class);

    /**
     * 上传存储库对象
     */
    private final UploadRepository repository;

    /**
     * 构造方法初始化
     *
     * @param repository 上传存储库对象
     */
    public OpenUploadServiceImpl(UploadRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Map<String, Object>> execute(OpenUploadContext context) {
        final UploadModel model = new UploadModel();
        final Object operator = context.get("$operator");
        if (operator instanceof final String content) {
            model.setOwner(content);
            model.setCreator(content);
            model.setModifier(content);
        }
        final String name = FileUtil.name(context.getName());
        if (name == null) {
            return Mono.error(new ParameterException(
                    this.getClass(),
                    "fun execute(OpenUploadContext context). ==> " +
                            "execute(...) request parameter <name> exception.",
                    "execute(...) request parameter <name> exception.")
            );
        }
        model.setName(name);
        model.setSource(SOURCE);
        LOGGER.info("OpenUploadServiceImpl >>>>>>>>> NAME >>>> {}", name);
        return repository.create(model).map(UploadModel::toMap);
    }

}
