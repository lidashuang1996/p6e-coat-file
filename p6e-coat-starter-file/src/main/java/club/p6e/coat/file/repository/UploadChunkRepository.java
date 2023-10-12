package club.p6e.coat.file.repository;

import club.p6e.coat.file.error.DataBaseException;
import club.p6e.coat.file.model.UploadChunkModel;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Statement;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

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
            "  (fid, name, size, date, operator)  " +
            "  VALUES( $1, $2, $3, $4, $5) RETURNING id;";

    @SuppressWarnings("ALL")
    private static final String DELETE_SQL = "" +
            "DELETE FROM p6e_file_upload_chunk WHERE fid = $1;";


    /**
     * 构造方法初始化
     */
    public UploadChunkRepository(ConnectionFactory factory) {
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
        return Mono.usingWhen(
                        this.factory.create(),
                        connection -> {
                            final Statement statement = connection.createStatement(CREATE_SQL);
                            statement.bind("$1", model.getFid());
                            statement.bind("$2", model.getName());
                            statement.bind("$3", model.getSize());
                            statement.bind("$4", model.getDate());
                            statement.bind("$5", model.getOperator());
                            return Mono.from(statement.execute())
                                    .flatMap(r -> Mono.from(r.map((row, metadata) -> row.get("id", Integer.class))));
                        },
                        Connection::close
                )
                .flatMap(id -> Mono.just(model.setId(id)));
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

    @SuppressWarnings("ALL")
    private static final String SELECT_SQL = "" +
            "  SELECT  " +
            "    id, fid, name, size, date, operator  " +
            "  FROM p6e_file_upload_chunk  " +
            "  WHERE  " +
            "  id > $id date < $localDateTime" +
            "  ORDER BY id AES;";

    public Mono<UploadChunkModel> select(Integer id, LocalDateTime localDateTime) {
        return Mono
                .from(this.factory.create())
                .flatMap(connection -> {
                    final Statement statement = connection.createStatement(SELECT_SQL);
                    statement.bind("$id", id);
                    statement.bind("$localDateTime", localDateTime);
                    return Mono.from(statement.execute());
                })
                .flatMap(r -> Mono.from(r.map((row, metadata) -> {
                    final UploadChunkModel model = new UploadChunkModel();
                    model.setId(row.get("id", Integer.class));
                    model.setFid(row.get("fid", Integer.class));
                    model.setName(row.get("name", String.class));
                    model.setSize(row.get("size", Long.class));
                    model.setDate(row.get("date", LocalDateTime.class));
                    model.setOperator(row.get("operator", String.class));
                    return model;
                })));
    }
}
