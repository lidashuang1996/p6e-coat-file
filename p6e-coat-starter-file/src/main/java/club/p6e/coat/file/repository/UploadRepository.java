package club.p6e.coat.file.repository;

import club.p6e.coat.file.error.DataBaseException;
import club.p6e.coat.file.error.FileException;
import club.p6e.coat.file.model.UploadModel;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Statement;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
     * R2dbcEntityTemplate 对象
     */
    private final ConnectionFactory factory;

    /**
     * 构造方法初始化
     */
    public UploadRepository(ConnectionFactory factory) {
        this.factory = factory;
    }

    @SuppressWarnings("ALL")
    private static final String CREATE_SQL = "" +
            "  INSERT INTO p6e_file_upload_chunk  " +
            "  (id, name, size, source, storage_type, storage_location, owner, create_date, update_date, rubbish, operator, lock, version)  " +
            "  VALUES($id, $name, $size, $source, $storage_type, $storage_location, $owner, $create_date, $update_date, $rubbish, $operator, $lock, $version);";

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
                    "fun create() -> " + UploadModel.class + " is null",
                    "UploadModel object data is null"
            ));
        }
        if (model.getSize() == null) {
            model.setSize(0L);
        }
        if (model.getOwner() == null) {
            model.setOwner("sys");
        }
        if (model.getOperator() == null) {
            model.setOperator("sys");
        }
        model.setId(null);
        model.setLock(0);
        model.setVersion(0);
        model.setRubbish(0);
        model.setCreateDate(LocalDateTime.now());
        model.setUpdateDate(LocalDateTime.now());
        return Mono
                .from(this.factory.create())
                .flatMap(connection -> {
                    final Statement statement = connection.createStatement(CREATE_SQL);
                    statement.bindNull("$id", Integer.class);
                    statement.bind("$name", model.getName());
                    statement.bind("$size", model.getSize());
                    statement.bind("$source", model.getSource());
                    statement.bind("$storage_type", model.getStorageType());
                    statement.bind("$storage_location", model.getStorageLocation());
                    statement.bind("$owner", model.getOwner());
                    statement.bind("$create_date", model.getCreateDate());
                    statement.bind("$update_date", model.getUpdateDate());
                    statement.bind("$rubbish", model.getRubbish());
                    statement.bind("$operator", model.getOperator());
                    statement.bind("$lock", model.getLock());
                    statement.bind("$version", model.getVersion());
                    return Mono.from(statement.execute());
                })
                .flatMap(r -> Mono.from(r.map((row, metadata) -> row.get("id", Integer.class))))
                .flatMap(id -> {
                    model.setId(id);
                    return Mono.just(model);
                });
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

    private static final int MAX_RETRY_COUNT = 3;
    private static final int RETRY_INTERVAL_DATE = 1500;

    /**
     * 修改数据--锁增加 1
     *
     * @param id    ID
     * @param retry 重试次数
     * @return Mono<Long> 受影响的数据条数
     */
    private Mono<Long> acquireLock(int id, int retry) {
        if (retry == 0) {
            return acquireLock0(id)
                    .flatMap(c -> c > 0 ? Mono.just(c) : acquireLock(id, (retry + 1)));
        } else if (retry <= MAX_RETRY_COUNT) {
            final long interval = RETRY_INTERVAL_DATE * ThreadLocalRandom.current().nextInt(100) / 100;
            return Mono.delay(Duration.of(interval, ChronoUnit.MILLIS))
                    .flatMap(r -> acquireLock0(id))
                    .flatMap(c -> c > 0 ? Mono.just(c) : acquireLock(id, (retry + 1)));
        } else {
            return Mono.error(new DataBaseException(
                    this.getClass(),
                    "fun acquireLock(int id, int retry). -> Exceeding maximum retry count error",
                    "Exceeding maximum retry count error"
            ));
        }
    }


    @SuppressWarnings("ALL")
    private static final String ACQUIRE_LOCK_SQL = "" +
            "  UPDATE p6e_file_upload_chunk  " +
            "  SET lock = $lock, version = $version1, update_date = $update_date " +
            "  WHERE id = $id AND version = $version2;";

    /**
     * 修改数据--锁增加 1
     *
     * @param id ID
     * @return Mono<Long> 受影响的数据条数
     */
    private Mono<Long> acquireLock0(int id) {
        return this.findById(id)
                .flatMap(m -> {
                    if (m.getLock() >= 0) {
                        return Mono
                                .from(this.factory.create())
                                .flatMap(connection -> {
                                    final Statement statement = connection.createStatement(ACQUIRE_LOCK_SQL);
                                    statement.bind("$id", id);
                                    statement.bind("$lock", m.getLock() + 1);
                                    statement.bind("$version1", m.getVersion() + 1);
                                    statement.bind("$version2", m.getVersion());
                                    statement.bind("$update_date", m.getUpdateDate());
                                    return Mono.from(statement.execute());
                                })
                                .flatMap(r -> Mono.from(r.getRowsUpdated()));
                    } else {
                        return Mono.error(new FileException(
                                this.getClass(),
                                "fun acquireLock0(int id). The file sharding request has been closed.",
                                "The file sharding request has been closed"
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
            return releaseLock0(id)
                    .flatMap(c -> c > 0 ? Mono.just(c) : releaseLock(id, (retry + 1)));
        } else if (retry <= MAX_RETRY_COUNT) {
            final long interval = RETRY_INTERVAL_DATE * ThreadLocalRandom.current().nextInt(100) / 100;
            return Mono.delay(Duration.of(interval, ChronoUnit.MILLIS))
                    .flatMap(r -> releaseLock0(id))
                    .flatMap(c -> c > 0 ? Mono.just(c) : releaseLock(id, (retry + 1)));
        } else {
            return Mono.error(new DataBaseException(
                    this.getClass(),
                    "fun releaseLock(int id, int retry). -> Exceeding maximum retry count error",
                    "Exceeding maximum retry count error"
            ));
        }
    }

    @SuppressWarnings("ALL")
    private static final String RELEASE_LOCK_SQL = "" +
            "  UPDATE p6e_file_upload_chunk  " +
            "  SET lock = $lock, version = $version1, update_date = $update_date " +
            "  WHERE id = $id AND version = $version2;";

    /**
     * 修改数据--锁减少 1
     *
     * @param id ID
     * @return Mono<Long> 受影响的数据条数
     */
    private Mono<Long> releaseLock0(int id) {
        return this.findById(id)
                .flatMap(m -> {
                    if (m.getLock() >= 0) {
                        return Mono
                                .from(this.factory.create())
                                .flatMap(connection -> {
                                    final Statement statement = connection.createStatement(RELEASE_LOCK_SQL);
                                    statement.bind("$id", id);
                                    statement.bind("$lock", m.getLock() - 1);
                                    statement.bind("$version1", m.getVersion() + 1);
                                    statement.bind("$version2", m.getVersion());
                                    statement.bind("$update_date", m.getUpdateDate());
                                    return Mono.from(statement.execute());
                                })
                                .flatMap(r -> Mono.from(r.getRowsUpdated()));
                    } else {
                        return Mono.error(new FileException(
                                this.getClass(),
                                "fun acquireLock0(int id). The file sharding request has been closed.",
                                "The file sharding request has been closed"
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
            return closeLock0(id)
                    .flatMap(c -> c > 0 ? Mono.just(c) : closeLock(id, (retry + 1)));
        } else if (retry <= MAX_RETRY_COUNT) {
            final long interval = RETRY_INTERVAL_DATE * ThreadLocalRandom.current().nextInt(100) / 100;
            return Mono.delay(Duration.of(interval, ChronoUnit.MILLIS))
                    .flatMap(t -> closeLock0(id))
                    .flatMap(c -> c > 0 ? Mono.just(c) : closeLock(id, (retry + 1)));
        } else {
            return Mono.error(new DataBaseException(
                    this.getClass(),
                    "fun closeLock(int id, int retry). -> Exceeding maximum retry count error",
                    "Exceeding maximum retry count error"
            ));
        }
    }

    @SuppressWarnings("ALL")
    private static final String CLOSE_LOCK_SQL = "" +
            "  UPDATE p6e_file_upload_chunk  " +
            "  SET lock = $lock, version = $version1, update_date = $update_date " +
            "  WHERE id = $id AND version = $version2;";

    /**
     * 关闭锁
     *
     * @param id 模型 ID
     * @return Mono<UploadModel> 受影响的数据条数
     */
    private Mono<Long> closeLock0(int id) {
        return this.findById(id)
                .flatMap(m -> {
                    if (m.getLock() == -1) {
                        return Mono.error(new FileException(
                                this.getClass(),
                                "fun closeLock0(int id). -> It is already in a closed state and cannot be closed again.",
                                "It is already in a closed state and cannot be closed again"
                        ));
                    } else if (m.getLock() > 0) {
                        return Mono.error(new FileException(
                                this.getClass(),
                                "fun closeLock0(int id). -> There are upload sharding requests and cannot be closed.",
                                "There are upload sharding requests and cannot be closed"
                        ));
                    } else if (m.getLock() == 0) {
                        return Mono
                                .from(this.factory.create())
                                .flatMap(connection -> {
                                    final Statement statement = connection.createStatement(CLOSE_LOCK_SQL);
                                    statement.bind("$id", id);
                                    statement.bind("$lock", -1);
                                    statement.bind("$version1", m.getVersion() + 1);
                                    statement.bind("$version2", m.getVersion());
                                    statement.bind("$update_date", m.getUpdateDate());
                                    return Mono.from(statement.execute());
                                })
                                .flatMap(r -> Mono.from(r.getRowsUpdated()));
                    } else {
                        return Mono.just(0L);
                    }
                });
    }

    @SuppressWarnings("all")
    private static final String SELECT_SQL = "" +
            "  SELECT id, name, size, source, storage_type, storage_location, owner, create_date, update_date, rubbish, operator, lock, version  " +
            "  FROM p6e_file_upload_chunk  " +
            "  WHERE id = $id;";

    /**
     * 根据 ID 查询数据
     *
     * @param id 模型 ID
     * @return Mono<UploadModel> 模型对象
     */
    public Mono<UploadModel> findById(int id) {
        return Mono
                .from(this.factory.create())
                .flatMap(connection -> Mono.from(connection.createStatement(SELECT_SQL).bind("$id", id).execute()))
                .flatMap(r -> Mono.from(r.map((row, metadata) -> {
                    final UploadModel model = new UploadModel();
                    model.setId(row.get("id", Integer.class));
                    model.setName(row.get("name", String.class));
                    model.setSize(row.get("size", Long.class));
                    model.setSource(row.get("source", String.class));
                    model.setStorageType(row.get("storage_type", String.class));
                    model.setStorageLocation(row.get("storage_location", String.class));
                    model.setOwner(row.get("owner", String.class));
                    model.setCreateDate(row.get("create_date", LocalDateTime.class));
                    model.setUpdateDate(row.get("update_date", LocalDateTime.class));
                    model.setRubbish(row.get("rubbish", Integer.class));
                    model.setOperator(row.get("operator", String.class));
                    model.setLock(row.get("lock", Integer.class));
                    model.setVersion(row.get("version", Integer.class));
                    return model;
                })));
    }

    @SuppressWarnings("ALL")
    private static final String SELELCT_SQL2 = "" +
            "  SELECT id, name, size, source, storage_type, storage_location, owner, create_date, update_date, rubbish, operator, lock, version  " +
            "  FROM p6e_file_upload_chunk  " +
            "  WHERE id > $id AND create_date >= $localDateTime" +
            "  ORDER BY id AES ;";

    /**
     * 根据 ID 或者创建时间范围查询数据
     *
     * @param id        模型 ID
     * @return Mono<UploadModel> 模型对象
     */
    public Mono<UploadModel> findByIdAndCreateDateOne(int id, LocalDateTime localDateTime) {
        return Mono
                .from(this.factory.create())
                .flatMap(connection -> {
                    final Statement statement = connection.createStatement(SELELCT_SQL2);
                    statement.bind("$id", id);
                    statement.bind("$localDateTime", localDateTime);
                    return Mono.from(statement.execute());
                })
                .flatMap(r -> Mono.from(r.map((row, metadata) -> {
                    final UploadModel model = new UploadModel();
                    model.setLock(row.get("lock", Integer.class));
                    model.setVersion(row.get("version", Integer.class));
                    model.setId(row.get("id", Integer.class));
                    model.setOwner(row.get("owner", String.class));
                    model.setCreateDate(row.get("create_date", LocalDateTime.class));
                    model.setUpdateDate(row.get("update_date", LocalDateTime.class));
                    model.setRubbish(row.get("rubbish", Integer.class));
                    model.setOperator(row.get("operator", String.class));
                    model.setName(row.get("name", String.class));
                    model.setSize(row.get("size", Long.class));
                    model.setSource(row.get("source", String.class));
                    model.setStorageType(row.get("storage_type", String.class));
                    model.setStorageLocation(row.get("storage_location", String.class));
                    return model;
                })));
    }

    @SuppressWarnings("ALL")
    private static final String UPDATE_SQL = "" +
            "  SELECT id, name, size, source, storage_type, storage_location, owner, create_date, update_date, rubbish, operator, lock, version  " +
            "  FROM p6e_file_upload_chunk  " +
            "  WHERE id > $id AND create_date >= $localDateTime" +
            "  ORDER BY id AES ;";

    /**
     * 修改数据
     *
     * @param model 模型对象
     * @return Mono<UploadModel> 受影响的数据条数
     */
    public Mono<Long> update(UploadModel model) {
        return this.findById(model.getId())
                .flatMap(m -> {
                    return Mono
                            .from(this.factory.create())
                            .flatMap(connection -> {
                                final Statement statement = connection.createStatement(UPDATE_SQL);
                                statement.bind("$id", model.getId());
                                statement.bind("$name", model.getName());
                                statement.bind("$size", model.getSize());
                                statement.bind("$source", model.getSource());
                                statement.bind("$storage_type", model.getStorageType());
                                statement.bind("$storage_location", model.getStorageLocation());
                                statement.bind("$owner", model.getOwner());
                                statement.bind("$create_date", model.getCreateDate());
                                statement.bind("$update_date", model.getUpdateDate());
                                statement.bind("$rubbish", model.getRubbish());
                                statement.bind("$operator", model.getOperator());
                                statement.bind("$lock", model.getLock());
                                statement.bind("$version", model.getVersion() + 1);
                                return Mono.from(statement.execute());
                            })
                            .flatMap(r -> Mono.from(r.getRowsUpdated()));
                });

    }

    @SuppressWarnings("ALL")
    private static final String DELETE_SQL = "" +
            "DELETE FROM p6e_file_upload WHERE id = $id;";

    /**
     * 根据 ID 删除数据
     *
     * @param id ID
     * @return Mono<Long> 受影响的数据条数
     */
    public Mono<Long> deleteById(int id) {
        return Mono
                .from(this.factory.create())
                .flatMap(connection -> Mono.from(connection.createStatement(DELETE_SQL).bind("$id", id).execute()))
                .flatMap(r -> Mono.from(r.getRowsUpdated()));
    }
}
