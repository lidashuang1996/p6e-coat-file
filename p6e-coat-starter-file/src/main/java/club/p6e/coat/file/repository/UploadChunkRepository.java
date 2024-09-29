package club.p6e.coat.file.repository;

import club.p6e.DatabaseConfig;
import club.p6e.coat.common.error.DataBaseException;
import club.p6e.coat.file.model.UploadChunkModel;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Statement;
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

    @SuppressWarnings("ALL")
    private static final String CREATE_SQL = "" +
            "    INSERT INTO    " +
            "        \"" + DatabaseConfig.TABLE_PREFIX + "file_upload_chunk\"    " +
            "        (    " +
            "            \"fid\",    " +
            "            \"name\",    " +
            "            \"size\",    " +
            "            \"creator\",    " +
            "            \"modifier\",    " +
            "            \"creation_date_time\",    " +
            "            \"modification_date_time\",    " +
            "            \"version\"    " +
            "        )    " +
            "    VALUES (    " +
            "            $1,    " +
            "            $2,    " +
            "            $3,    " +
            "            $4,    " +
            "            $5,    " +
            "            $6,    " +
            "            $7,    " +
            "            $8     " +
            "   ) RETURNING id      " +
            "   ;   ";

    @SuppressWarnings("ALL")
    private static final String FID_BY_DELETE_SQL = "" +
            "    DELETE FROM    " +
            "        \"" + DatabaseConfig.TABLE_PREFIX + "file_upload_chunk\"    " +
            "    WHERE    " +
            "        fid = $1        " +
            "    ;    ";

    @SuppressWarnings("ALL")
    private static final String EXPIRE_SELECT_SQL = "" +
            "    SELECT    " +
            "        \"id\",    " +
            "        \"fid\",    " +
            "        \"name\",    " +
            "        \"size\",    " +
            "        \"creator\",    " +
            "        \"modifier\",    " +
            "        \"creation_date_time\",    " +
            "        \"modification_date_time\",    " +
            "        \"version\"    " +
            "    FROM    " +
            "        \"" + DatabaseConfig.TABLE_PREFIX + "file_upload_chunk\"    " +
            "    WHERE    " +
            "        \"id\" > $1 AND \"creation_date_time\" < $2    " +
            "    ORDER BY    " +
            "        \"id\" AES    " +
            "    ;    ";


    /**
     * ConnectionFactory 对象
     */
    private final ConnectionFactory factory;

    /**
     * 构造方法初始化
     *
     * @param factory ConnectionFactory 对象
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
                    "fun create(UploadChunkModel model). ==> " +
                            "create(...) model<UploadChunkModel> object data is null.",
                    "create(...) UploadChunkModel object data is null."
            ));
        }
        if (model.getFid() == null
                || model.getName() == null
                || model.getSize() == null) {
            return Mono.error(new DataBaseException(
                    this.getClass(),
                    "fun create(UploadChunkModel model). ==> " +
                            "create(...) model<UploadChunkModel> object attribute [ fid/name/size ] data is null.",
                    "create(...) UploadChunkModel object attribute [ fid/name/size ] data is null."
            ));
        }
        final LocalDateTime now = LocalDateTime.now();
        model.setId(null);
        model.setVersion(0);
        model.setCreationDateTime(now);
        model.setModificationDateTime(now);
        if (model.getCreator() == null) {
            model.setCreator("sys");
        }
        if (model.getModifier() == null) {
            model.setModifier("sys");
        }
        return Mono.usingWhen(this.factory.create(), connection -> {
            final Statement statement = connection.createStatement(CREATE_SQL);
            statement.bind("$1", model.getFid());
            statement.bind("$2", model.getName());
            statement.bind("$3", model.getSize());
            statement.bind("$4", model.getCreator());
            statement.bind("$5", model.getModifier());
            statement.bind("$6", model.getCreationDateTime());
            statement.bind("$7", model.getModificationDateTime());
            statement.bind("$8", model.getVersion());
            return Mono.from(statement.execute()).flatMap(result -> Mono.from(result.map((row, metadata) -> row.get("id", Integer.class))));
        }, Connection::close).flatMap(id -> Mono.just(model.setId(id)));
    }

    /**
     * 根据 FID 删除数据
     *
     * @param fid FID
     * @return Mono<Long> 删除的数据条数
     */
    public Mono<Long> deleteByFid(int fid) {
        return Mono
                .from(this.factory.create())
                .flatMap(connection -> Mono.from(connection.createStatement(FID_BY_DELETE_SQL).bind("$1", fid).execute()))
                .flatMap(result -> Mono.from(result.getRowsUpdated()));
    }

    /**
     * 查询过期数据
     *
     * @param id            起始 ID
     * @param localDateTime 终止时间
     * @return Mono<UploadChunkModel> 模型对象
     */
    public Mono<UploadChunkModel> selectExpireData(Integer id, LocalDateTime localDateTime) {
        return Mono
                .from(this.factory.create())
                .flatMap(connection -> {
                    final Statement statement = connection.createStatement(EXPIRE_SELECT_SQL);
                    statement.bind("$1", id);
                    statement.bind("$2", localDateTime);
                    return Mono.from(statement.execute());
                })
                .flatMap(r -> Mono.from(r.map((row, metadata) -> {
                    final UploadChunkModel model = new UploadChunkModel();
                    model.setId(row.get("id", Integer.class));
                    model.setFid(row.get("fid", Integer.class));
                    model.setName(row.get("name", String.class));
                    model.setSize(row.get("size", Long.class));
                    model.setCreator(row.get("creator", String.class));
                    model.setCreationDateTime(row.get("creation_date_time", LocalDateTime.class));
                    model.setModifier(row.get("modifier", String.class));
                    model.setModificationDateTime(row.get("modification_date_time", LocalDateTime.class));
                    model.setVersion(row.get("version", Integer.class));
                    return model;
                })));
    }

}
