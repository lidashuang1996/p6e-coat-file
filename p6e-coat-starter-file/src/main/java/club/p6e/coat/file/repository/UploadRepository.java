package club.p6e.coat.file.repository;

import club.p6e.DatabaseConfig;
import club.p6e.coat.common.error.FileException;
import club.p6e.coat.common.error.DataBaseException;
import club.p6e.coat.file.model.UploadModel;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Statement;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
public class UploadRepository {

    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY_COUNT = 3;

    /**
     * 重试间隔时间
     */
    private static final int RETRY_INTERVAL_DATE = 1000;

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
            "       \"public\".\"" + DatabaseConfig.TABLE_PREFIX + "_file_upload\"    " +
            "  WHERE    " +
            "       id = $1    " +
            "    ;    ";

    @SuppressWarnings("ALL")
    private static final String CREATE_SQL = "" +
            "  INSERT INTO " +
            "       \"public\".\"" + DatabaseConfig.TABLE_PREFIX + "_file_upload\"    " +
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
            "        (    $1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12    )    " +
            "    RETURNING id    " +
            "    ;    ";

    @SuppressWarnings("ALL")
    private static final String DELETE_SQL = "" +
            "    DELETE FROM \"public\".\"" + DatabaseConfig.TABLE_PREFIX + "_file_upload\" WHERE id = $1;    ";

    @SuppressWarnings("ALL")
    private static final String ACQUIRE_LOCK_SQL = "" +
            "  UPDATE p6e_file_upload  " +
            "  SET lock = $1, version = $2, update_date = $3 " +
            "  WHERE id = $4 AND version = $5;";

    @SuppressWarnings("ALL")
    private static final String RELEASE_LOCK_SQL = "" +
            "  UPDATE p6e_file_upload  " +
            "  SET lock = $1, version = $2, update_date = $3 " +
            "  WHERE id = $4 AND version = $5;";

    @SuppressWarnings("ALL")
    private static final String CLOSE_LOCK_SQL = "" +
            "   UPDATE    " +
            "       \"public\".\"" + DatabaseConfig.TABLE_PREFIX + "_file_upload\"    " +
            "   SET    " +
            "       \"lock\" = $1,    " +
            "       \"version\" = $2,    " +
            "       \"modification_date_time\" = $3    " +
            "   WHERE " +
            "       \"id\" = $4    " +
            "       AND  \"version\" = $5    " +
            "    ;    ";

    /**
     * R2dbcEntityTemplate 对象
     */
    private final ConnectionFactory factory;

    /**
     * 构造方法初始化
     */
    public UploadRepository(ConnectionFactory factory) {
        this.factory = factory;
    }

    /**
     * 创建数据
     *
     * @param model 模型对象
     * @return Mono<UploadModel> 模型对象
     */
    public Mono<UploadModel> create(UploadModel model) {
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
        model.setId(null);
        model.setLock(0);
        model.setVersion(0);
        model.setCreationDateTime(LocalDateTime.now());
        model.setModificationDateTime(LocalDateTime.now());
        return Mono.usingWhen(
                this.factory.create(),
                connection -> {
                    final Statement statement = connection.createStatement(CREATE_SQL);
                    statement.bind("$1", model.getName());
                    statement.bind("$2", model.getSize());
                    statement.bind("$3", model.getSource());
                    statement.bind("$4", model.getOwner());
                    statement.bind("$5", model.getStorageType());
                    statement.bind("$6", model.getStorageLocation());
                    statement.bind("$7", model.getLock());
                    statement.bind("$8", model.getCreator());
                    statement.bind("$9", model.getModifier());
                    statement.bind("$10", model.getCreationDateTime());
                    statement.bind("$11", model.getModificationDateTime());
                    statement.bind("$12", model.getVersion());
                    return Mono
                            .from(statement.execute())
                            .flatMap(r -> Mono.from(r.map((row, metadata) -> row.get("id", Integer.class))))
                            .flatMap(this::findById);
                },
                Connection::close
        ).switchIfEmpty(Mono.error(new DataBaseException(
                this.getClass(),
                "fun create(UploadModel model). -> Create data error",
                "Create data error"
        )));
    }

    /**
     * 修改数据--锁增加 1
     *
     * @param id ID
     * @return Mono<Long> 受影响的数据条数
     */
    public Mono<Long> acquireLock(int id) {
        return acquireLock(id, 0);
    }

    /**
     * 修改数据--锁增加 1
     *
     * @param id    ID
     * @param retry 重试次数
     * @return Mono<Long> 受影响的数据条数
     */
    private Mono<Long> acquireLock(int id, int retry) {
        if (retry == 0) {
            return acquireLock0(id).flatMap(c -> c > 0 ? Mono.just(c) : acquireLock(id, (retry + 1)));
        } else if (retry <= MAX_RETRY_COUNT) {
            final long interval = RETRY_INTERVAL_DATE * ThreadLocalRandom.current().nextInt(100) / 100;
            return Mono.delay(Duration.of(interval, ChronoUnit.MILLIS))
                    .flatMap(r -> acquireLock0(id))
                    .flatMap(c -> c > 0 ? Mono.just(c) : acquireLock(id, (retry + 1)));
        } else {
            return Mono.error(new DataBaseException(
                    this.getClass(),
                    "fun acquireLock(int id, int retry). -> [ acquireLock(...) exceptions exceeding the latest retry count. ]",
                    "acquireLock(...) exceptions exceeding the latest retry count."
            ));
        }
    }

    /**
     * 修改数据--锁增加 1
     *
     * @param id ID
     * @return Mono<Long> 受影响的数据条数
     */
    private Mono<Long> acquireLock0(int id) {
        return this.findById(id)
                .switchIfEmpty(Mono.error(new DataBaseException(
                        this.getClass(),
                        "fun acquireLock0(int id). -> [ acquireLock0(...) get data does not exist exception. ]",
                        "acquireLock0(...) get data does not exist exception."
                )))
                .flatMap(m -> {
                    m.setModificationDateTime(LocalDateTime.now());
                    if (m.getLock() >= 0) {
                        return Mono.usingWhen(
                                this.factory.create(),
                                connection -> {
                                    final Statement statement = connection.createStatement(ACQUIRE_LOCK_SQL);
                                    statement.bind("$4", m.getId());
                                    statement.bind("$5", m.getVersion());
                                    statement.bind("$1", (m.getLock() + 1));
                                    statement.bind("$2", (m.getVersion() + 1));
                                    statement.bind("$3", m.getModificationDateTime());
                                    return Mono.from(statement.execute()).flatMap(result -> Mono.from(result.getRowsUpdated()));
                                }, Connection::close
                        ).switchIfEmpty(Mono.error(new DataBaseException(
                                this.getClass(),
                                "fun acquireLock0(int id). -> [ releaseLock0(...) data base exception. ]",
                                "releaseLock0(...) data base exception."
                        )));
                    } else {
                        return Mono.error(new FileException(
                                this.getClass(),
                                "fun acquireLock0(int id). [ releaseLock0(...) incomplete sharding exceptions. ]",
                                "releaseLock0(...) incomplete sharding exceptions."
                        ));
                    }
                });
    }

    /**
     * 修改数据--锁减少 1
     *
     * @param id ID
     * @return Mono<Long> 受影响的数据条数
     */
    public Mono<Long> releaseLock(int id) {
        return releaseLock(id, 0);
    }

    /**
     * 修改数据--锁减少 1
     *
     * @param id    ID
     * @param retry 重试次数
     * @return Mono<Long> 受影响的数据条数
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
                    "fun releaseLock(int id, int retry). -> [ releaseLock(...) exceptions exceeding the latest retry count. ]",
                    "exceptions exceeding the latest retry count."
            ));
        }
    }

    /**
     * 修改数据--锁减少 1
     *
     * @param id ID
     * @return Mono<Long> 受影响的数据条数
     */
    private Mono<Long> releaseLock0(int id) {
        return this.findById(id)
                .switchIfEmpty(Mono.error(new DataBaseException(
                        this.getClass(),
                        "fun releaseLock0(int id). -> [ releaseLock0(...) get data does not exist exception. ]",
                        "releaseLock0(...) get data does not exist exception."
                )))
                .flatMap(m -> {
                    m.setModificationDateTime(LocalDateTime.now());
                    if (m.getLock() >= 0) {
                        return Mono.usingWhen(
                                this.factory.create(),
                                connection -> {
                                    final Statement statement = connection.createStatement(RELEASE_LOCK_SQL);
                                    statement.bind("$4", id);
                                    statement.bind("$5", m.getVersion());
                                    statement.bind("$1", (m.getLock() - 1));
                                    statement.bind("$2", (m.getVersion() + 1));
                                    statement.bind("$3", m.getModificationDateTime());
                                    return Mono.from(statement.execute()).flatMap(r -> Mono.from(r.getRowsUpdated()));
                                }, Connection::close
                        ).switchIfEmpty(Mono.error(new DataBaseException(
                                this.getClass(),
                                "fun releaseLock0(int id). -> [ releaseLock0(...) data base exception. ]",
                                "releaseLock0(...) data base exception."
                        )));
                    } else {
                        return Mono.error(new FileException(
                                this.getClass(),
                                "fun releaseLock0(int id). -> [ releaseLock0(...) incomplete sharding exceptions. ]",
                                "releaseLock0(...) incomplete sharding exceptions."
                        ));
                    }
                });
    }

    /**
     * 关闭锁
     *
     * @param id 模型 ID
     * @return Mono<UploadModel> 受影响的数据条数
     */
    public Mono<Long> closeLock(int id) {
        return closeLock(id, 0);
    }

    /**
     * 关闭锁
     *
     * @param id    模型 ID
     * @param retry 重试次数
     * @return Mono<UploadModel> 受影响的数据条数
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
                    "fun closeLock(int id, int retry). -> [ closeLock0(...) exceptions exceeding the latest retry count. ]",
                    "exceptions exceeding the latest retry count."
            ));
        }
    }

    /**
     * 关闭锁
     *
     * @param id 模型 ID
     * @return Mono<UploadModel> 受影响的数据条数
     */
    private Mono<Long> closeLock0(int id) {
        return this.findById(id)
                .switchIfEmpty(Mono.error(new DataBaseException(
                        this.getClass(),
                        "fun releaseLock0(int id). -> [ closeLock0(...) get data does not exist exception. ]",
                        "closeLock0(...) get data does not exist exception."
                )))
                .flatMap(m -> {
                    m.setModificationDateTime(LocalDateTime.now());
                    if (m.getLock() == -1) {
                        return Mono.error(new FileException(
                                this.getClass(),
                                "fun closeLock0(int id). -> [ closeLock0(...) It is already in a closed state and cannot be closed again. ]",
                                "closeLock0(...) it is already in a closed state and cannot be closed again."
                        ));
                    } else if (m.getLock() > 0) {
                        return Mono.error(new FileException(
                                this.getClass(),
                                "fun closeLock0(int id). -> [ loseLock0(...) there are upload sharding requests and cannot be closed. ]",
                                "closeLock0(...) there are upload sharding requests and cannot be closed."
                        ));
                    } else if (m.getLock() == 0) {
                        return Mono.usingWhen(
                                this.factory.create(),
                                connection -> {
                                    final Statement statement = connection.createStatement(CLOSE_LOCK_SQL);
                                    statement.bind("$1", -1);
                                    statement.bind("$4", id);
                                    statement.bind("$5", m.getVersion());
                                    statement.bind("$2", (m.getVersion() + 1));
                                    statement.bind("$3", m.getModificationDateTime());
                                    return Mono.from(statement.execute()).flatMap(r -> Mono.from(r.getRowsUpdated()));
                                }, Connection::close
                        ).switchIfEmpty(Mono.error(new DataBaseException(
                                this.getClass(),
                                "fun closeLock0(int id). -> [ closeLock0(...) data base exception. ]",
                                "closeLock0(...) data base exception."
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
        return Mono.usingWhen(
                this.factory.create(),
                connection -> Mono.from(connection.createStatement(SELECT_SQL).bind("$1", id).execute())
                        .flatMap(r -> Mono.from(r.map((row, metadata) -> {
                            final UploadModel model = new UploadModel();
                            model.setId(row.get("id", Integer.class));
                            model.setName(row.get("name", String.class));
                            model.setSize(row.get("size", Long.class));
                            model.setSource(row.get("source", String.class));
                            model.setOwner(row.get("owner", String.class));
                            model.setStorageType(row.get("storage_type", String.class));
                            model.setStorageLocation(row.get("storage_location", String.class));
                            model.setLock(row.get("lock", Integer.class));
                            model.setCreator(row.get("creator", String.class));
                            model.setModifier(row.get("modifier", String.class));
                            model.setCreationDateTime(row.get("creation_date_time", LocalDateTime.class));
                            model.setModificationDateTime(row.get("modification_date_time", LocalDateTime.class));
                            model.setVersion(row.get("version", Integer.class));
                            return model;
                        }))), Connection::close);
    }

    /**
     * 修改数据
     *
     * @param model 模型对象
     * @return Mono<UploadModel> 受影响的数据条数
     */
    @SuppressWarnings("ALL")
    public Mono<Long> update(UploadModel model) {
        return this.findById(model.getId())
                .switchIfEmpty(Mono.error(new DataBaseException(
                        this.getClass(),
                        "fun update(UploadModel model). -> [ update(...) get data does not exist exception. ]",
                        "update(...) get data does not exist exception."
                )))
                .flatMap(m -> {
                    model.setId(m.getId());
                    model.setVersion(m.getVersion());
                    model.setModificationDateTime(LocalDateTime.now());
                    return Mono.usingWhen(
                            this.factory.create(),
                            connection -> {
                                final List<Map<String, Object>> list = new ArrayList<>();
                                list.add(new HashMap<>() {{
                                    put("update_date", model.getUpdateDate());
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
                                if (model.getOperator() != null) {
                                    list.add(new HashMap<>() {{
                                        put("operator", model.getOperator());
                                    }});
                                }
                                final StringBuffer sql = new StringBuffer();
                                sql.append("  UPDATE p6e_file_upload  SET ");
                                for (int i = 0; i < list.size(); i++) {
                                    final Map<String, Object> item = list.get(i);
                                    for (final String key : item.keySet()) {
                                        sql.append(" ").append(key).append(" = ").append("$").append(i + 1).append(",");
                                    }
                                }
                                sql.append(" version = $").append(list.size() + 1)
                                        .append(" WHERE id = $").append(list.size() + 2)
                                        .append(" AND version = $").append(list.size() + 3).append(";");
                                final Statement statement = connection.createStatement(sql.toString());
                                for (int i = 0; i < list.size(); i++) {
                                    final Map<String, Object> item = list.get(i);
                                    for (final String key : item.keySet()) {
                                        statement.bind("$" + (i + 1), item.get(key));
                                    }
                                }
                                statement.bind("$" + (list.size() + 1), model.getVersion() + 1);
                                statement.bind("$" + (list.size() + 2), model.getId());
                                statement.bind("$" + (list.size() + 3), model.getVersion());
                                return Mono.from(statement.execute())
                                        .flatMap(r -> Mono.from(r.getRowsUpdated()));
                            },
                            Connection::close
                    ).switchIfEmpty(Mono.error(new DataBaseException(
                            this.getClass(),
                            "fun update(int id). -> [ closeLock0(...) data base exception. ]",
                            "closeLock0(...) data base exception."
                    )));
                });
    }

    /**
     * 根据 ID 删除数据
     *
     * @param id ID
     * @return Mono<Long> 受影响的数据条数
     */
    public Mono<Long> deleteById(int id) {
        return Mono.usingWhen(
                this.factory.create(),
                connection -> Mono.from(
                        connection.createStatement(DELETE_SQL).bind("$1", id).execute()
                ).flatMap(r -> Mono.from(r.getRowsUpdated())),
                Connection::close
        );
    }
}
