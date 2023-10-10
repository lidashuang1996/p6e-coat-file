package club.p6e.coat.file.repository;

import club.p6e.coat.file.error.DataBaseException;
import club.p6e.coat.file.model.UploadChunkModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * 文件块上传存储库
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = UploadChunkRepository.class,
        ignored = UploadChunkRepository.class
)
public class UploadChunkRepository extends BaseRepository {

    /**
     * R2dbcEntityTemplate 对象
     */
    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    /**
     * 构造方法初始化
     *
     * @param r2dbcEntityTemplate R2dbcEntityTemplate 对象
     */
    public UploadChunkRepository(R2dbcEntityTemplate r2dbcEntityTemplate) {
        this.r2dbcEntityTemplate = r2dbcEntityTemplate;
    }

    /**
     * 创建数据
     *
     * @param model 模型对象
     * @return Mono<UploadChunkModel> 模型对象
     */
    public Mono<UploadChunkModel> create(UploadChunkModel model) {
        if (model == null) {
            return Mono.error(new DataBaseException(
                    this.getClass(),
                    "fun create() -> " + UploadChunkModel.class + " is null",
                    "UploadChunkModel object data is null"
            ));
        }
        if (model.getOperator() == null) {
            model.setOperator("sys");
        }
        model.setDate(LocalDateTime.now());
        return r2dbcEntityTemplate.insert(model);
    }

    /**
     * 根据 FID 删除数据
     *
     * @param fid FID
     * @return Mono<Long> 受影响的数据条数
     */
    public Mono<Long> deleteByFid(int fid) {
        return r2dbcEntityTemplate.delete(Query.query(
                Criteria.where(UploadChunkModel.FID).is(fid)
        ), UploadChunkModel.class);
    }

}
