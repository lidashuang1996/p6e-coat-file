package club.p6e.coat.file.repository;

import club.p6e.DatabaseConfig;
import club.p6e.coat.common.error.DataBaseException;
import club.p6e.coat.common.utils.TransformationUtil;
import club.p6e.coat.file.model.UploadChunkModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.r2dbc.core.DatabaseClient;
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
@SuppressWarnings("ALL")
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
            "            :FID,    " +
            "            :NAME,    " +
            "            :SIZE,    " +
            "            :CREATOR,    " +
            "            :MODIFIER,    " +
            "            :CREATOR_DATE_TIME,    " +
            "            :MODIFICATION_DATE_TIME,    " +
            "            :VERSION     " +
            "   ) RETURNING id      " +
            "   ;   ";

    @SuppressWarnings("ALL")
    private static final String FID_BY_DELETE_SQL = "" +
            "    DELETE FROM    " +
            "        \"" + DatabaseConfig.TABLE_PREFIX + "file_upload_chunk\"    " +
            "    WHERE    " +
            "        fid = :FID        " +
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
            "        \"id\" > :ID AND \"creation_date_time\" < :CREATION_DATE_TIME    " +
            "    ORDER BY    " +
            "        \"id\" AES    " +
            "    ;    ";


    /**
     * DatabaseClient 对象
     */
    private final DatabaseClient client;

    /**
     * 构造方法初始化
     *
     * @param client DatabaseClient 对象
     */
    public UploadChunkRepository(DatabaseClient client) {
        this.client = client;
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
        return client
                .sql(CREATE_SQL)
                .bind("FID", model.getFid())
                .bind("NAME", model.getName())
                .bind("SIZE", model.getSize())
                .bind("CREATOR", model.getCreator())
                .bind("MODIFIER", model.getModifier())
                .bind("CREATOR_DATE_TIME", model.getCreationDateTime())
                .bind("MODIFICATION_DATE_TIME", model.getModificationDateTime())
                .bind("VERSION", model.getVersion())
                .fetch()
                .first()
                .map(row -> model.setId(TransformationUtil.objectToInteger(row.get("id"))))
                .switchIfEmpty(Mono.error(new DataBaseException(
                        this.getClass(),
                        "fun create(UploadChunkModel model). ==> create(...) create data is null.",
                        "create(...) create data is null."
                )));
    }

    /**
     * 根据 FID 删除数据
     *
     * @param fid FID
     * @return Mono<Long> 删除的数据条数
     */
    public Mono<Long> deleteByFid(Integer fid) {
        return client.sql(FID_BY_DELETE_SQL).bind("FID", fid).fetch().rowsUpdated();
    }

    /**
     * 查询过期数据
     *
     * @param id            起始 ID
     * @param localDateTime 终止时间
     * @return Mono<UploadChunkModel> 模型对象
     */
    public Mono<UploadChunkModel> selectExpireData(Integer id, LocalDateTime localDateTime) {
        return client
                .sql(EXPIRE_SELECT_SQL)
                .bind("ID", id)
                .bind("CREATION_DATE_TIME", localDateTime)
                .fetch()
                .first()
                .map(row -> {
                    final UploadChunkModel model = new UploadChunkModel();
                    model.setId(TransformationUtil.objectToInteger(row.get("id")));
                    model.setFid(TransformationUtil.objectToInteger(row.get("fid")));
                    model.setName(TransformationUtil.objectToString(row.get("name")));
                    model.setSize(TransformationUtil.objectToLong(row.get("size")));
                    model.setCreator(TransformationUtil.objectToString(row.get("creator")));
                    model.setCreationDateTime(TransformationUtil.objectToLocalDateTime(row.get("creation_date_time")));
                    model.setModifier(TransformationUtil.objectToString(row.get("modifier")));
                    model.setModificationDateTime(TransformationUtil.objectToLocalDateTime(row.get("modification_date_time")));
                    model.setVersion(TransformationUtil.objectToInteger(row.get("version")));
                    return model;
                });
    }

}
