package club.p6e.coat.file;

import club.p6e.coat.file.error.DataBaseException;
import club.p6e.coat.file.repository.UploadChunkRepository;
import club.p6e.coat.file.repository.UploadRepository;
import club.p6e.coat.file.utils.FileUtil;
import club.p6e.coat.file.utils.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 实现文件分片清除策略服务
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = FileSliceCleanStrategyService.class,
        ignored = FileSliceCleanStrategyServiceImpl.class
)
public class FileSliceCleanStrategyServiceImpl implements FileSliceCleanStrategyService {

    /**
     * 分片 数据源
     */
    private static final String SLICE_SOURCE = "SLICE_UPLOAD";

    /**
     * 简单（小文件） 数据源
     */
    private static final String SIMPLE_UPLOAD = "SIMPLE_UPLOAD";

    /**
     * 日志系统
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FileSliceCleanStrategyServiceImpl.class);

    /**
     * "{{ht}" 小时删除
     */
    private final int ht;

    /**
     * "{mt}x" 分钟删除
     */
    private final int mt;

    /**
     * 构造方法初始化
     */
    public FileSliceCleanStrategyServiceImpl() {
        this.ht = ThreadLocalRandom.current().nextInt(6);
        this.mt = ThreadLocalRandom.current().nextInt(6);
        LOGGER.info("[ FILE CLEAN ] INIT ==> HT: " + this.ht + " MT: "
                + this.mt + " >>> XXXX-XX-XX 0" + this.ht + ":" + this.mt + "X:XX");
    }

    @Override
    public void execute() {
        final Properties properties = SpringUtil.getBean(Properties.class);
        final Properties.SliceUpload sliceUpload = properties.getSliceUpload();
        final Properties.SimpleUpload simpleUpload = properties.getSimpleUpload();
        final UploadRepository uploadRepository = SpringUtil.getBean(UploadRepository.class);
        final UploadChunkRepository uploadChunkRepository = SpringUtil.getBean(UploadChunkRepository.class);
        // 查询需要处理的数据
        final AtomicInteger id = new AtomicInteger(0);
        final AtomicBoolean status = new AtomicBoolean(true);
        // 删除 7 天以前的数据
        final LocalDateTime localDateTime = LocalDateTime.now().minusDays(7);
        LOGGER.info("[ TASK ] Start executing file purge scheduled task. localDateTime => " + localDateTime);
        while (status.get()) {
            uploadRepository
                    .findByIdAndCreateDateOne(id.get(), null, localDateTime)
                    .map(m -> {
                        id.set(m.getId());
                        return m;
                    })
                    .flatMap(m -> uploadRepository
                            .update(m.setRubbish(1))
                            .flatMap(c -> {
                                if (c > 0) {
                                    String fp = null;
                                    // 清除磁盘文件
                                    if (SLICE_SOURCE.equals(m.getSource())) {
                                        fp = FileUtil.composePath(sliceUpload.getPath(), m.getStorageLocation());
                                        FileUtil.deleteFolder(fp);
                                    }
                                    if (SIMPLE_UPLOAD.equals(m.getSource())) {
                                        fp = FileUtil.composePath(simpleUpload.getPath(), m.getStorageLocation());
                                        FileUtil.deleteFolder(fp);
                                    }
                                    LOGGER.info("[ TASK ] delete folder => " + fp);
                                    return Mono.just(m);
                                } else {
                                    return Mono.error(new DataBaseException(
                                            this.getClass(),
                                            "fun execute(). -> Database mark rubbish data [" + m.getId() + "] error",
                                            "Database mark rubbish data [" + m.getId() + "] error"
                                    ));
                                }
                            }))
                    .flatMap(m -> uploadRepository
                            // 清除数据库的缓存数据
                            .deleteById(m.getId())
                            .flatMap(c -> {
                                LOGGER.info("[ TASK ] db file delete result ( " + m.getId() + " ) => " + c);
                                if (c > 0) {
                                    return uploadChunkRepository
                                            .deleteByFid(m.getId())
                                            .map(cc -> {
                                                LOGGER.info("[ TASK ] db file chunk delete result ( " + m.getId() + " ) => " + cc);
                                                return cc;
                                            });
                                } else {
                                    return Mono.error(new DataBaseException(
                                            this.getClass(),
                                            "fun execute(). -> Database file data [" + m.getId() + "] delete error",
                                            "Database file data [" + m.getId() + "] delete error"
                                    ));
                                }
                            }))
                    .switchIfEmpty(Mono
                            .just(0L)
                            .map(c -> {
                                status.set(false);
                                return c;
                            }))
                    .onErrorResume(throwable -> {
                        status.set(false);
                        throwable.printStackTrace();
                        return Mono.just(0L);
                    })
                    .block();
        }
        LOGGER.info("[ TASK ] Complete executing file purge scheduled task.");
    }

    @Override
    public boolean time() {
        final int hour = LocalDateTime.now().getHour();
        final int minute = LocalDateTime.now().getMinute();
        return this.ht == hour && this.mt == ((int) Math.floor(minute / 10F));
    }

}
