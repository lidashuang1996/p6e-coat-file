package club.p6e.coat.file.repository;

import club.p6e.coat.file.error.DataBaseException;
import club.p6e.coat.file.model.UploadChunkModel;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Statement;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
public class UploadChunkRepository {

    /**
     * R2dbcEntityTemplate 对象
     */
    private final ConnectionFactory factory;

    @SuppressWarnings("ALL")
    private static final String CREATE_SQL = "" +
            "  INSERT INTO p6e_file_upload_chunk  " +
            "  (id, fid, name, size, date, operator)  " +
            "  VALUES($id, $fid, $name, $size, $date, $operator);";

    @SuppressWarnings("ALL")
    private static final String DELETE_SQL = "" +
            "DELETE FROM p6e_file_upload_chunk WHERE fid = $fid;";


    /**
     * 构造方法初始化
     *
     */
    public UploadChunkRepository(@Qualifier("club.p6e.coat.file.config.ConnectionFactory") ConnectionFactory factory) {
        this.factory = factory;
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
        model.setId(null);
        model.setDate(LocalDateTime.now());
        if (model.getOperator() == null) {
            model.setOperator("sys");
        }
        return Mono
                .from(this.factory.create())
                .flatMap(connection -> {
                    final Statement statement = connection.createStatement(CREATE_SQL);
                    statement.bindNull("$id", Integer.class);
                    statement.bind("$fid", model.getFid());
                    statement.bind("$name", model.getName());
                    statement.bind("$size", model.getSize());
                    statement.bind("$date", model.getDate());
                    statement.bind("$operator", model.getOperator());
                    return Mono.from(statement.execute());
                })
                .flatMap(r -> Mono.from(r.map((row, metadata) -> row.get("id", Integer.class))))
                .flatMap(id -> {
                    model.setId(id);
                    return Mono.just(model);
                });
    }

    /**
     * 根据 FID 删除数据
     *
     * @param fid FID
     * @return Mono<Long> 受影响的数据条数
     */
    public Mono<Long> deleteByFid(int fid) {
        return Mono
                .from(this.factory.create())
                .flatMap(connection -> Mono.from(connection.createStatement(DELETE_SQL).bind("$fid", fid).execute()))
                .flatMap(r -> Mono.from(r.getRowsUpdated()));
    }

}
