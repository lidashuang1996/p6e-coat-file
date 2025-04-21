package club.p6e.coat.file.repository;

import club.p6e.DatabaseConfig;
import club.p6e.coat.common.error.DataBaseException;
import club.p6e.coat.common.utils.CopyUtil;
import club.p6e.coat.common.utils.TransformationUtil;
import club.p6e.coat.file.model.UploadModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 文件上传存储库
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = UploadRepository.class,
        ignored = UploadRepository.class
)
@SuppressWarnings("ALL")
public class UploadRepository {

    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY_COUNT = 3;

    /**
     * 重试间隔时间
     */
    private static final int RETRY_INTERVAL_DATE = 1000;

    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadRepository.class);

    @SuppressWarnings("ALL")
    private static final String SELECT_SQL = "" +
            "  SELECT " +
            "       \"id\",    " +
            "       \"name\",    " +
            "       \"size\",    " +
            "       \"source\",    " +
            "       \"owner\",    " +
            "       \"storage_type\",    " +
            "       \"storage_location\",    " +
            "       \"lock\",    " +
            "       \"creator\",    " +
            "       \"modifier\",    " +
            "       \"creation_date_time\",    " +
            "       \"modification_date_time\",    " +
            "       \"version\"    " +
            "  FROM    " +
            "       \"" + DatabaseConfig.TABLE_PREFIX + "file_upload\"    " +
            "  WHERE    " +
            "       \"id\" = :ID    " +
            "    ;    ";

    @SuppressWarnings("ALL")
    private static final String CREATE_SQL = "" +
            "    INSERT INTO    " +
            "        \"" + DatabaseConfig.TABLE_PREFIX + "file_upload\"    " +
            "    (    " +
            "       \"name\",    " +
            "       \"size\",    " +
            "       \"source\",    " +
            "       \"owner\",    " +
            "       \"storage_type\",    " +
            "       \"storage_location\",    " +
            "       \"lock\",    " +
            "       \"creator\",    " +
            "       \"modifier\",    " +
            "       \"creation_date_time\",    " +
            "       \"modification_date_time\",    " +
            "       \"version\"    " +
            "    )    " +
            "    VALUES    " +
            "        (    :NAME, :SIZE, :SOURCE, :OWNER, :STORAGE_TYPE, :STORAGE_LOCATION, :LOCK, :CREATOR, :MODIFIER, :CREATION_DATE_TIME, :MODIFICATION_DATE_TIME, :VERSION    )    " +
            "    RETURNING id    " +
            "    ;    ";

    @SuppressWarnings("ALL")
    private static final String DELETE_SQL = "" +
            "    DELETE FROM    " +
            "        \"" + DatabaseConfig.TABLE_PREFIX + "file_upload\"    " +
            "    WHERE    " +
            "        \"id\" = :ID    " +
            "    ;    ";

    @SuppressWarnings("ALL")
    private static final String ACQUIRE_LOCK_SQL = "" +
            "    UPDATE    " +
            "        \"" + DatabaseConfig.TABLE_PREFIX + "file_upload\"    " +
            "    SET " +
            "        \"lock\" = :LOCK,    " +
            "        \"version\" = :NEW_VERSION,    " +
            "        \"modification_date_time\" = :MODIFICATION_DATE_TIME    " +
            "    WHERE    " +
            "        \"id\" = :ID    " +
            "        AND \"version\" = :OLD_VERSION    " +
            "    ;    ";

    @SuppressWarnings("ALL")
    private static final String RELEASE_LOCK_SQL = "" +
            "    UPDATE    " +
            "        \"" + DatabaseConfig.TABLE_PREFIX + "file_upload\"    " +
            "    SET " +
            "        \"lock\" = :LOCK,    " +
            "        \"version\" = :NEW_VERSION,    " +
            "        \"modification_date_time\" = :MODIFICATION_DATE_TIME    " +
            "    WHERE    " +
            "        \"id\" = :ID    " +
            "        AND \"version\" = :OLD_VERSION    " +
            "    ;    ";

    @SuppressWarnings("ALL")
    private static final String CLOSE_LOCK_SQL = "" +
            "    UPDATE    " +
            "        \"" + DatabaseConfig.TABLE_PREFIX + "file_upload\"    " +
            "    SET    " +
            "        \"lock\" = :LOCK,    " +
            "        \"version\" = :NEW_VERSION,    " +
            "        \"modification_date_time\" = :MODIFICATION_DATE_TIME    " +
            "    WHERE " +
            "        \"id\" = :ID    " +
            "        AND \"version\" = :OLD_VERSION    " +
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
    public UploadRepository(DatabaseClient client) {
        this.client = client;
    }

    /**
     * 创建数据
     *
     * @param model 模型对象
     * @return Mono<UploadModel> 模型对象
     */
    public Mono<UploadModel> create(UploadModel model) {
        if (model == null) {
            return Mono.error(new DataBaseException(
                    this.getClass(),
                    "fun create(UploadModel model). ==> create(...) model<UploadModel> object data is null.",
                    "create(...) UploadModel object data is null."
            ));
        }
        if (model.getName() == null || model.getSource() == null) {
            return Mono.error(new DataBaseException(
                    this.getClass(),
                    "fun create(UploadModel model). ==> " +
                            "create(...) model<UploadModel> object attribute [ name/source ] data is null.",
                    "create(...) UploadModel object attribute [ name/source ] data is null."
            ));
        }
        if (model.getSize() == null) {
            model.setSize(0L);
        }
        if (model.getOwner() == null) {
            model.setOwner("sys");
        }
        if (model.getCreator() == null) {
            model.setCreator("sys");
        }
        if (model.getModifier() == null) {
            model.setModifier("sys");
        }
        if (model.getStorageType() == null) {
            model.setStorageType("");
        }
        if (model.getStorageLocation() == null) {
            model.setStorageLocation("");
        }
        final LocalDateTime now = LocalDateTime.now();
        model.setId(null);
        model.setLock(0);
        model.setVersion(0);
        model.setCreationDateTime(now);
        model.setModificationDateTime(now);
        return client
                .sql(CREATE_SQL)
                .bind("NAME", model.getName())
                .bind("SIZE", model.getSize())
                .bind("SOURCE", model.getSource())
                .bind("OWNER", model.getOwner())
                .bind("STORAGE_TYPE", model.getStorageType())
                .bind("STORAGE_LOCATION", model.getStorageLocation())
                .bind("LOCK", model.getLock())
                .bind("CREATOR", model.getCreator())
                .bind("MODIFIER", model.getModifier())
                .bind("CREATION_DATE_TIME", model.getCreationDateTime())
                .bind("MODIFICATION_DATE_TIME", model.getModificationDateTime())
                .bind("VERSION", model.getVersion())
                .fetch()
                .first()
                .map(row -> model.setId(TransformationUtil.objectToInteger(row.get("id"))))
                .switchIfEmpty(Mono.error(new DataBaseException(
                        this.getClass(),
                        "fun create(UploadModel model). ==> create(...) create data is null.",
                        "create(...) create data is null."
                )));
    }

    /**
     * 修改数据--锁增加 1
     *
     * @param id ID
     * @return Mono<Long> 修改的数据条数
     */
    public Mono<Long> acquireLock(int id) {
        return acquireLock(id, 0);
    }

    /**
     * 修改数据--锁增加 1
     *
     * @param id    ID
     * @param retry 重试次数
     * @return Mono<Long> 修改的数据条数
     */
    private Mono<Long> acquireLock(int id, int retry) {
        if (retry == 0) {
            return acquireLock0(id).flatMap(c -> c > 0 ? Mono.just(c) : acquireLock(id, (retry + 1)));
        } else if (retry <= MAX_RETRY_COUNT) {
            final long interval = RETRY_INTERVAL_DATE * ThreadLocalRandom.current().nextInt(100) / 100;
            return Mono
                    .delay(Duration.of(interval, ChronoUnit.MILLIS))
                    .flatMap(r -> acquireLock0(id))
                    .flatMap(c -> c > 0 ? Mono.just(c) : acquireLock(id, (retry + 1)));
        } else {
            return Mono.error(new DataBaseException(
                    this.getClass(),
                    "fun acquireLock(int id, int retry). ==> " +
                            "acquireLock(...) exceptions exceeding the latest retry count.",
                    "acquireLock(...) exceptions exceeding the latest retry count."
            ));
        }
    }

    /**
     * 修改数据--锁增加 1
     *
     * @param id ID
     * @return Mono<Long> 修改的数据条数
     */
    private Mono<Long> acquireLock0(int id) {
        return this.findById(id)
                .switchIfEmpty(Mono.error(new DataBaseException(
                        this.getClass(),
                        "fun acquireLock0(int id). ==> acquireLock0(...) get data does not exist exception.",
                        "acquireLock0(...) get data does not exist exception."
                )))
                .flatMap(m -> {
                    m.setModificationDateTime(LocalDateTime.now());
                    if (m.getLock() >= 0) {
                        return client
                                .sql(ACQUIRE_LOCK_SQL)
                                .bind("ID", m.getId())
                                .bind("OLD_VERSION", m.getVersion())
                                .bind("LOCK", (m.getLock() + 1))
                                .bind("NEW_VERSION", (m.getVersion() + 1))
                                .bind("MODIFICATION_DATE_TIME", m.getModificationDateTime())
                                .fetch()
                                .rowsUpdated()
                                .switchIfEmpty(Mono.error(new DataBaseException(
                                        this.getClass(),
                                        "fun acquireLock0(int id). acquireLock0(...) update data exception.",
                                        "acquireLock0(...) update data exception."
                                )));
                    } else {
                        return Mono.error(new DataBaseException(
                                this.getClass(),
                                "fun acquireLock0(int id). acquireLock0(...) incomplete sharding exceptions.",
                                "acquireLock0(...) incomplete sharding exceptions."
                        ));
                    }
                });
    }

    /**
     * 修改数据--锁减少 1
     *
     * @param id ID
     * @return Mono<Long> 修改的数据条数
     */
    public Mono<Long> releaseLock(int id) {
        return releaseLock(id, 0);
    }

    /**
     * 修改数据--锁减少 1
     *
     * @param id    ID
     * @param retry 重试次数
     * @return Mono<Long> 修改的数据条数
     */
    private Mono<Long> releaseLock(int id, int retry) {
        if (retry == 0) {
            return releaseLock0(id).flatMap(c -> c > 0 ? Mono.just(c) : releaseLock(id, (retry + 1)));
        } else if (retry <= MAX_RETRY_COUNT) {
            final long interval = RETRY_INTERVAL_DATE * ThreadLocalRandom.current().nextInt(100) / 100;
            return Mono
                    .delay(Duration.of(interval, ChronoUnit.MILLIS))
                    .flatMap(r -> releaseLock0(id))
                    .flatMap(c -> c > 0 ? Mono.just(c) : releaseLock(id, (retry + 1)));
        } else {
            return Mono.error(new DataBaseException(
                    this.getClass(),
                    "fun releaseLock(int id, int retry). ==> releaseLock(...) exceptions exceeding the latest retry count.",
                    "releaseLock(...) exceptions exceeding the latest retry count."
            ));
        }
    }

    /**
     * 修改数据--锁减少 1
     *
     * @param id ID
     * @return Mono<Long> 修改的数据条数
     */
    private Mono<Long> releaseLock0(int id) {
        return this.findById(id)
                .switchIfEmpty(Mono.error(new DataBaseException(
                        this.getClass(),
                        "fun releaseLock0(int id). ==> releaseLock0(...) get data does not exist exception.",
                        "releaseLock0(...) get data does not exist exception."
                )))
                .flatMap(m -> {
                    m.setModificationDateTime(LocalDateTime.now());
                    if (m.getLock() >= 0) {
                        return client
                                .sql(RELEASE_LOCK_SQL)
                                .bind("ID", m.getId())
                                .bind("OLD_VERSION", m.getVersion())
                                .bind("LOCK", (m.getLock() + 1))
                                .bind("NEW_VERSION", (m.getVersion() + 1))
                                .bind("MODIFICATION_DATE_TIME", m.getModificationDateTime())
                                .fetch()
                                .rowsUpdated()
                                .switchIfEmpty(Mono.error(new DataBaseException(
                                        this.getClass(),
                                        "fun releaseLock0(int id). ==> releaseLock0(...) update data exception.",
                                        "releaseLock0(...) update data exception."
                                )));
                    } else {
                        return Mono.error(new DataBaseException(
                                this.getClass(),
                                "fun releaseLock0(int id). ==> releaseLock0(...) incomplete sharding exceptions.",
                                "releaseLock0(...) incomplete sharding exceptions."
                        ));
                    }
                });
    }

    /**
     * 关闭锁
     *
     * @param id 模型 ID
     * @return Mono<UploadModel> 修改的数据条数
     */
    public Mono<Long> closeLock(int id) {
        return closeLock(id, 0);
    }

    /**
     * 关闭锁
     *
     * @param id    模型 ID
     * @param retry 重试次数
     * @return Mono<UploadModel> 修改的数据条数
     */
    private Mono<Long> closeLock(int id, int retry) {
        if (retry == 0) {
            return closeLock0(id).flatMap(c -> c > 0 ? Mono.just(c) : closeLock(id, (retry + 1)));
        } else if (retry <= MAX_RETRY_COUNT) {
            final long interval = RETRY_INTERVAL_DATE * ThreadLocalRandom.current().nextInt(100) / 100;
            return Mono
                    .delay(Duration.of(interval, ChronoUnit.MILLIS))
                    .flatMap(t -> closeLock0(id))
                    .flatMap(c -> c > 0 ? Mono.just(c) : closeLock(id, (retry + 1)));
        } else {
            return Mono.error(new DataBaseException(
                    this.getClass(),
                    "fun closeLock(int id, int retry). ==> closeLock(...) exceptions exceeding the latest retry count.",
                    "closeLock(...) exceptions exceeding the latest retry count."
            ));
        }
    }

    /**
     * 关闭锁
     *
     * @param id 模型 ID
     * @return Mono<UploadModel> 修改的数据条数
     */
    private Mono<Long> closeLock0(int id) {
        return this.findById(id)
                .switchIfEmpty(Mono.error(new DataBaseException(
                        this.getClass(),
                        "fun closeLock0(int id). ==> closeLock0(...) get data does not exist exception.",
                        "closeLock0(...) get data does not exist exception."
                )))
                .flatMap(m -> {
                    m.setModificationDateTime(LocalDateTime.now());
                    if (m.getLock() == -1) {
                        return Mono.error(new DataBaseException(
                                this.getClass(),
                                "fun closeLock0(int id). ==> closeLock0(...) " +
                                        "it is already in a closed state and cannot be closed again.",
                                "closeLock0(...) it is already in a closed state and cannot be closed again."
                        ));
                    } else if (m.getLock() > 0) {
                        return Mono.error(new DataBaseException(
                                this.getClass(),
                                "fun closeLock0(int id). ==> closeLock0(...) " +
                                        "there are upload sharding requests and cannot be closed.",
                                "closeLock0(...) there are upload sharding requests and cannot be closed."
                        ));
                    } else if (m.getLock() == 0) {
                        return client
                                .sql(CLOSE_LOCK_SQL)
                                .bind("LOCK", -1)
                                .bind("ID", id)
                                .bind("OLD_VERSION", m.getVersion())
                                .bind("NEW_VERSION", (m.getVersion() + 1))
                                .bind("MODIFICATION_DATE_TIME", m.getModificationDateTime())
                                .fetch()
                                .rowsUpdated()
                                .switchIfEmpty(Mono.error(new DataBaseException(
                                        this.getClass(),
                                        "fun closeLock0(int id). ==> closeLock0(...) update data exception.",
                                        "closeLock0(...) update data exception."
                                )));
                    } else {
                        return Mono.just(0L);
                    }
                });
    }


    /**
     * 根据 ID 查询数据
     *
     * @param id 模型 ID
     * @return Mono<UploadModel> 模型对象
     */
    public Mono<UploadModel> findById(int id) {
        return client
                .sql(SELECT_SQL)
                .bind("ID", id)
                .fetch()
                .first()
                .map(row -> {
                    final UploadModel model = new UploadModel();
                    model.setId(TransformationUtil.objectToInteger(row.get("id")));
                    model.setName(TransformationUtil.objectToString(row.get("name")));
                    model.setSize(TransformationUtil.objectToLong(row.get("size")));
                    model.setSource(TransformationUtil.objectToString(row.get("source")));
                    model.setOwner(TransformationUtil.objectToString(row.get("owner")));
                    model.setStorageType(TransformationUtil.objectToString(row.get("storage_type")));
                    model.setStorageLocation(TransformationUtil.objectToString(row.get("storage_location")));
                    model.setLock(TransformationUtil.objectToInteger(row.get("lock")));
                    model.setCreator(TransformationUtil.objectToString(row.get("creator")));
                    model.setModifier(TransformationUtil.objectToString(row.get("modifier")));
                    model.setCreationDateTime(TransformationUtil.objectToLocalDateTime(row.get("creation_date_time")));
                    model.setModificationDateTime(TransformationUtil.objectToLocalDateTime(row.get("modification_date_time")));
                    model.setVersion(TransformationUtil.objectToInteger(row.get("version")));
                    return model;
                })
                .switchIfEmpty(Mono.error(new DataBaseException(
                        this.getClass(),
                        "fun findById(int id). ==> findById(...) find id data is null.",
                        "findById(...) find id data is null."
                )));
    }

    /**
     * 修改数据
     *
     * @param model 模型对象
     * @return Mono<UploadModel> 修改的数据条数
     */
    @SuppressWarnings("ALL")
    public Mono<UploadModel> update(UploadModel model) {
        if (model == null) {
            return Mono.error(new DataBaseException(
                    this.getClass(),
                    "fun update(UploadModel model) ==> update(...) model<UploadModel> object data is null.",
                    "update(...) UploadModel object data is null."
            ));
        }
        if (model.getId() == null) {
            return Mono.error(new DataBaseException(
                    this.getClass(),
                    "fun update(UploadModel model) ==> update(...) model<UploadModel> object attribute [ id ] data is null.",
                    "update(...) UploadModel object attribute [ id ] data is null."
            ));
        }
        return this.findById(model.getId())
                .switchIfEmpty(Mono.error(new DataBaseException(
                        this.getClass(),
                        "fun update(UploadModel model). ==> update(...) get data does not exist exception.",
                        "update(...) get data does not exist exception."
                )))
                .flatMap(m -> {
                    final List<Map<String, Object>> list = new ArrayList<>();
                    model.setVersion(m.getVersion());
                    model.setModificationDateTime(LocalDateTime.now());
                    list.add(new HashMap<>() {{
                        put("modification_date_time", model.getModificationDateTime());
                    }});
                    if (model.getName() != null) {
                        list.add(new HashMap<>() {{
                            put("name", model.getName());
                        }});
                    }
                    if (model.getSize() != null) {
                        list.add(new HashMap<>() {{
                            put("size", model.getSize());
                        }});
                    }
                    if (model.getSource() != null) {
                        list.add(new HashMap<>() {{
                            put("source", model.getSource());
                        }});
                    }
                    if (model.getStorageType() != null) {
                        list.add(new HashMap<>() {{
                            put("storage_type", model.getStorageType());
                        }});
                    }
                    if (model.getStorageLocation() != null) {
                        list.add(new HashMap<>() {{
                            put("storage_location", model.getStorageLocation());
                        }});
                    }
                    if (model.getOwner() != null) {
                        list.add(new HashMap<>() {{
                            put("owner", model.getOwner());
                        }});
                    }
                    if (model.getModifier() != null) {
                        list.add(new HashMap<>() {{
                            put("modifier", model.getModifier());
                        }});
                    }
                    final StringBuffer sql = new StringBuffer();
                    sql.append("    UPDATE    ")
                            .append("\"")
                            .append(DatabaseConfig.TABLE_PREFIX)
                            .append("file_upload")
                            .append("\"")
                            .append("    SET    ");
                    for (final Map<String, Object> item : list) {
                        for (final String key : item.keySet()) {
                            sql.append("    ")
                                    .append("\"")
                                    .append(key)
                                    .append("\"")
                                    .append("    =    ")
                                    .append(":")
                                    .append(key.toUpperCase())
                                    .append(",");
                        }
                    }
                    sql.append("    \"version\" = :NEW_VERSION    ")
                            .append("    WHERE \"id\" = :ID    ")
                            .append("        AND \"version\" = :OLD_VERSION    ").append(";");
                    LOGGER.info("[ SQL ] >>> {}", sql);
                    LOGGER.info("[ LIST ] >>> {}", list);
                    LOGGER.info("[ MODEL ] >>> {}", model);
                    DatabaseClient.GenericExecuteSpec spec = client.sql(sql.toString());
                    for (int i = 0; i < list.size(); i++) {
                        final Map<String, Object> item = list.get(i);
                        for (final String key : item.keySet()) {
                            spec = spec.bind(key.toUpperCase(), item.get(key));
                        }
                    }
                    spec = spec.bind("ID", model.getId());
                    spec = spec.bind("OLD_VERSION", model.getVersion());
                    spec = spec.bind("NEW_VERSION", model.getVersion() + 1);
                    spec.bind("MODIFICATION_DATE_TIME", model.getModificationDateTime());
                    return spec
                            .fetch()
                            .rowsUpdated()
                            .map(row -> CopyUtil.run(model, m).setVersion(model.getVersion() + 1))
                            .switchIfEmpty((Mono.error(new DataBaseException(
                                    this.getClass(),
                                    "fun update(UploadModel model) ==> update(...) model<UploadModel> data is null.",
                                    "update(...) UploadModel data is null."
                            ))));
                });
    }

    /**
     * 根据 ID 删除数据
     *
     * @param id ID
     * @return Mono<Long> 删除的数据条数
     */
    public Mono<Long> delete(Integer id) {
        return client
                .sql(DELETE_SQL)
                .bind("ID", id)
                .fetch()
                .rowsUpdated()
                .switchIfEmpty(Mono.error(new DataBaseException(
                        this.getClass(),
                        "fun delete(int id). ==> delete(...) find id data is null.",
                        "delete(...) find id data is null."
                )));
    }

}
