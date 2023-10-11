package club.p6e.coat.file.repository;

import club.p6e.coat.file.error.DataBaseException;
import club.p6e.coat.file.error.FileException;
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
            "  INSERT INTO p6e_file_upload  " +
            "  (name, size, source, storage_type, storage_location, owner, create_date, update_date, operator, lock, version)  " +
            "  VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11) RETURNING id;";

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
        if (model.getStorageType() == null) {
            model.setStorageType("");
        }
        if (model.getStorageLocation() == null) {
            model.setStorageLocation("");
        }
        model.setId(null);
        model.setLock(0);
        model.setVersion(0);
        model.setCreateDate(LocalDateTime.now());
        model.setUpdateDate(LocalDateTime.now());
        return Mono
                .from(this.factory.create())
                .flatMap(connection -> {
                    final Statement statement = connection.createStatement(CREATE_SQL);
                    statement.bind("$1", model.getName());
                    statement.bind("$2", model.getSize());
                    statement.bind("$3", model.getSource());
                    statement.bind("$4", model.getStorageType());
                    statement.bind("$5", model.getStorageLocation());
                    statement.bind("$6", model.getOwner());
                    statement.bind("$7", model.getCreateDate());
                    statement.bind("$8", model.getUpdateDate());
                    statement.bind("$9", model.getOperator());
                    statement.bind("$10", model.getLock());
                    statement.bind("$11", model.getVersion());
                    return Mono.from(statement.execute());
                })
                .flatMap(r -> Mono.from(r.map((row, metadata) -> row.get("id", Integer.class))))
                .flatMap(this::findById);
    }

    /**
     * 修改数据--锁增加 1
     *
     * @param id ID
     * @return Mono<Long> 受影响的数据条数
     */
    public Mono<Long> acquireLock(int id) {
        System.out.println("acquireLock");
        return acquireLock(id, 0);
    }

    private static final int MAX_RETRY_COUNT = 3;
    private static final int RETRY_INTERVAL_DATE = 1000;

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
            "  UPDATE p6e_file_upload  " +
            "  SET lock = $1, version = $2, update_date = $3 " +
            "  WHERE id = $4 AND version = $5;";

    /**
     * 修改数据--锁增加 1
     *
     * @param id ID
     * @return Mono<Long> 受影响的数据条数
     */
    private Mono<Long> acquireLock0(int id) {
        return this.findById(id)
                .flatMap(m -> {
                    System.out.println("ACQ ACQ   " + m);
                    m.setUpdateDate(LocalDateTime.now());
                    if (m.getLock() >= 0) {
                        return Mono.usingWhen(
                                this.factory.create(),
                                connection -> {
                                    final Statement statement = connection.createStatement(ACQUIRE_LOCK_SQL);
                                    statement.bind("$1", m.getLock() + 1);
                                    statement.bind("$2", m.getVersion() + 1);
                                    statement.bind("$3", m.getUpdateDate());
                                    statement.bind("$4", m.getId());
                                    statement.bind("$5", m.getVersion());
                                    return Mono
                                            .from(statement.execute())
                                            .flatMap(result -> Mono.from(result.getRowsUpdated()));
                                },
                                Connection::close
                        );
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
        System.out.println("releaseLock");
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
            "  UPDATE p6e_file_upload  " +
            "  SET lock = $1, version = $2, update_date = $3 " +
            "  WHERE id = $4 AND version = $5;";

    /**
     * 修改数据--锁减少 1
     *
     * @param id ID
     * @return Mono<Long> 受影响的数据条数
     */
    private Mono<Long> releaseLock0(int id) {
        return this.findById(id)
                .flatMap(m -> {
                    m.setUpdateDate(LocalDateTime.now());
                    System.out.println("RES RES   " + m);
                    if (m.getLock() >= 0) {
                        return Mono.usingWhen(
                                this.factory.create(),
                                connection -> {
                                    final Statement statement = connection.createStatement(RELEASE_LOCK_SQL);
                                    statement.bind("$1", m.getLock() - 1);
                                    statement.bind("$2", m.getVersion() + 1);
                                    statement.bind("$3", m.getUpdateDate());
                                    statement.bind("$4", id);
                                    statement.bind("$5", m.getVersion());
                                    return Mono.from(statement.execute())
                                            .flatMap(r -> Mono.from(r.getRowsUpdated()));
                                },
                                Connection::close
                        );
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
        System.out.println(id);
        return closeLock(id, 0).flatMap(r -> findById(id).map(m -> {
            System.out.println(m);
            return 1L;
        }));
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
            "  UPDATE p6e_file_upload  " +
            "  SET lock = $1, version = $2, update_date = $3 " +
            "  WHERE id = $4 AND version = $5;";

    /**
     * 关闭锁
     *
     * @param id 模型 ID
     * @return Mono<UploadModel> 受影响的数据条数
     */
    private Mono<Long> closeLock0(int id) {
        return this.findById(id)
                .flatMap(m -> {
                    m.setUpdateDate(LocalDateTime.now());
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
                        return Mono.usingWhen(
                                this.factory.create(),
                                connection -> {
                                    final Statement statement = connection.createStatement(CLOSE_LOCK_SQL);
                                    statement.bind("$1", -1);
                                    statement.bind("$2", m.getVersion() + 1);
                                    statement.bind("$3", m.getUpdateDate());
                                    statement.bind("$4", id);
                                    statement.bind("$5", m.getVersion());
                                    return Mono.from(statement.execute())
                                            .flatMap(r -> Mono.from(r.getRowsUpdated()));
                                },
                                Connection::close
                        );
                    } else {
                        return Mono.just(0L);
                    }
                });
    }

    @SuppressWarnings("all")
    private static final String SELECT_SQL = "" +
            "  SELECT id, name, size, source, storage_type, storage_location, owner, create_date, update_date, operator, lock, version  " +
            "  FROM p6e_file_upload  " +
            "  WHERE id = $1;";

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
                            model.setLock(row.get("lock", Integer.class));
                            model.setVersion(row.get("version", Integer.class));
                            model.setId(row.get("id", Integer.class));
                            model.setOwner(row.get("owner", String.class));
                            model.setCreateDate(row.get("create_date", LocalDateTime.class));
                            model.setUpdateDate(row.get("update_date", LocalDateTime.class));
                            model.setOperator(row.get("operator", String.class));
                            model.setName(row.get("name", String.class));
                            model.setSize(row.get("size", Long.class));
                            model.setSource(row.get("source", String.class));
                            model.setStorageType(row.get("storage_type", String.class));
                            model.setStorageLocation(row.get("storage_location", String.class));
                            return model;
                        })))
                , Connection::close
        );
    }

    @SuppressWarnings("ALL")
    private static final String SELELCT_SQL2 = "" +
            "  SELECT id, name, size, source, storage_type, storage_location, owner, create_date, update_date, rubbish, operator, lock, version  " +
            "  FROM p6e_file_upload  " +
            "  WHERE id > $id AND create_date >= $localDateTime" +
            "  ORDER BY id AES ;";

    /**
     * 根据 ID 或者创建时间范围查询数据
     *
     * @param id 模型 ID
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
            "  UPDATE p6e_file_upload  " +
            "  SET  aaa " +
            "  WHERE id = $1 AND version = $2;";

    /**
     * 修改数据
     *
     * @param model 模型对象
     * @return Mono<UploadModel> 受影响的数据条数
     */
    public Mono<Long> update(UploadModel model) {
        return this.findById(model.getId())
                .flatMap(m -> {
                    model.setVersion(m.getVersion());
                    model.setUpdateDate(LocalDateTime.now());
                    System.out.println("xxx update   " + model);
                    return Mono.usingWhen(
                            this.factory.create(),
                            connection -> {
                                String sql = UPDATE_SQL;
                                String a = "";
                                if (model.getVersion() != null) {
                                    a += ", version = $3 ";
                                }
                                if (model.getUpdateDate() != null) {
                                    a += ", update_date = $4 ";
                                }
                                if (model.getSize() != null) {
                                    a += ", size = $5 ";
                                }
                                if (model.getStorageType() != null) {
                                    a += ", storage_type = $6 ";
                                }
                                if (model.getStorageLocation() != null) {
                                    a += ", storage_location = $7 ";
                                }
                                System.out.println(a);
                                System.out.println(a.substring(1));
                                System.out.println(sql);
                                sql = sql.replace("aaa", a.substring(1));
                                System.out.println(sql);
                                final Statement statement = connection.createStatement(sql);
                                statement.bind("$4", model.getUpdateDate());
                                statement.bind("$5", model.getSize());
                                statement.bind("$6", model.getStorageType());
                                statement.bind("$7", model.getStorageLocation());
                                statement.bind("$1", model.getId());
                                statement.bind("$2", model.getVersion());
                                statement.bind("$3", model.getVersion() + 1);
                                return Mono.from(statement.execute())
                                        .flatMap(r -> Mono.from(r.getRowsUpdated()));
                            },
                            Connection::close
                    );
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
