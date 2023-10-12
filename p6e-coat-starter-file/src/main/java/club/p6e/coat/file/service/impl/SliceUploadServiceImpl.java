package club.p6e.coat.file.service.impl;

import club.p6e.coat.file.FileSignatureService;
import club.p6e.coat.file.context.SliceUploadContext;
import club.p6e.coat.file.error.FileException;
import club.p6e.coat.file.Properties;
import club.p6e.coat.file.model.UploadChunkModel;
import club.p6e.coat.file.repository.UploadChunkRepository;
import club.p6e.coat.file.repository.UploadRepository;
import club.p6e.coat.file.service.SliceUploadService;
import club.p6e.coat.file.utils.FileUtil;
import club.p6e.coat.file.utils.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.Map;

/**
 * 分片上传服务
 * 步骤2: 分片上传操作
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = SliceUploadService.class,
        ignored = SliceUploadServiceImpl.class
)
public class SliceUploadServiceImpl implements SliceUploadService {

    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SliceUploadServiceImpl.class);

    /**
     * 配置文件对象
     */
    private final Properties properties;

    /**
     * 上传存储库对象
     */
    private final UploadRepository uploadRepository;

    /**
     * 上传块存储库对象
     */
    private final UploadChunkRepository uploadChunkRepository;

    /**
     * 文件签名服务
     */
    private final FileSignatureService fileSignatureService;

    /**
     * 构造方法初始化
     *
     * @param properties            配置文件对象
     * @param uploadRepository      上传存储库对象
     * @param uploadChunkRepository 上传块存储库对象
     */
    public SliceUploadServiceImpl(
            Properties properties,
            UploadRepository uploadRepository,
            UploadChunkRepository uploadChunkRepository,
            FileSignatureService fileSignatureService
    ) {
        this.properties = properties;
        this.uploadRepository = uploadRepository;
        this.uploadChunkRepository = uploadChunkRepository;
        this.fileSignatureService = fileSignatureService;
    }

    @Override
    public Mono<Map<String, Object>> execute(SliceUploadContext context) {
        final Integer id = context.getId();
        final Integer index = context.getIndex();
        final String signature = context.getSignature();
        // 读取并清除文件对象
        final FilePart filePart = context.getFilePart();
        context.setFilePart(null);
        return uploadRepository
                .findById(id)
                .flatMap(m -> Mono.just(m)
                        .flatMap(um -> {
                            final UploadRepository repository = SpringUtil.getBean(UploadRepository.class);
                            // 文件绝对路径
                            final String absolutePath = FileUtil.convertAbsolutePath(FileUtil.composePath(
                                    properties.getSliceUpload().getPath(), String.valueOf(um.getId())));
                            // 如果不存在文件夹就创建文件夹
                            if (!FileUtil.checkFolderExist(absolutePath)) {
                                FileUtil.createFolder(absolutePath);
                            }
                            final File absolutePathFile = new File(
                                    FileUtil.composePath(absolutePath, index + "_" + FileUtil.generateName()));
                            return repository
                                    // 获取锁
                                    .acquireLock(um.getId())
                                    // 写入文件数据
                                    .flatMap(file -> filePart
                                            .transferTo(absolutePathFile)
                                            .then(Mono.just(absolutePathFile)))
                                    // 释放锁
                                    .flatMap(file -> repository.releaseLock(um.getId()))
                                    // 转换为文件对象输出
                                    .map(l -> absolutePathFile);
                        })
                        // 验证文件数据
                        .flatMap(f -> {
                            final long size = properties.getSliceUpload().getMaxSize();
                            if (f.length() > size) {
                                FileUtil.deleteFile(f);
                                return Mono.error(new FileException(this.getClass(),
                                        "fun execute() -> File ("
                                                + f.getName() + ") upload exceeds the maximum length limit",
                                        "File (" + f.getName() + ") upload exceeds the maximum length limit")
                                );
                            }
                            return Mono.just(f);
                        })
                        .flatMap(f -> fileSignatureService
                                .execute(f)
                                .flatMap(s -> {
                                    if (!s.equals(signature)) {
                                        FileUtil.deleteFile(f);
                                        return Mono.error(new FileException(this.getClass(),
                                                "fun execute() -> File ("
                                                        + f.getName() + ") incorrect signature content",
                                                "File (" + f.getName() + ") incorrect signature content")
                                        );
                                    }
                                    return Mono.just(f);
                                }))
                        .flatMap(f -> {
                            final UploadChunkModel model = new UploadChunkModel();
                            model.setFid(m.getId());
                            model.setName(f.getName());
                            model.setSize(f.length());
                            final Object operator = context.get("operator");
                            if (operator instanceof final String o) {
                                model.setOperator(o);
                            } else if (m.getOperator() != null) {
                                model.setOperator(m.getOperator());
                            }
                            return uploadChunkRepository.create(model);
                        })
                )
                .map(UploadChunkModel::toMap);
    }

}
