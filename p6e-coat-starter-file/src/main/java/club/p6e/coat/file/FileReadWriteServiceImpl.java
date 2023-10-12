package club.p6e.coat.file;

import club.p6e.coat.file.actuator.FileActuatorModel;
import club.p6e.coat.file.actuator.FileReadActuator;
import club.p6e.coat.file.actuator.FileWriteActuator;
import club.p6e.coat.file.error.ResourceNodeException;
import club.p6e.coat.file.utils.FileUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.Map;

/**
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = FileReadWriteService.class,
        ignored = FileReadWriteServiceImpl.class
)
public class FileReadWriteServiceImpl implements FileReadWriteService {

    /**
     * DISK 资源类型
     */
    private static final String DISK_RESOURCE_TYPE = "DISK";

    /**
     * 文件存储位置的路径服务
     */
    private final FolderStorageLocationPathService folderStorageLocationPathService;

    /**
     * 构造方法初始化
     *
     * @param folderStorageLocationPathService 文件存储位置的路径服务
     */
    public FileReadWriteServiceImpl(FolderStorageLocationPathService folderStorageLocationPathService) {
        this.folderStorageLocationPathService = folderStorageLocationPathService;
    }

    @Override
    public Mono<FileActuatorModel> write(String name, Map<String, Object> extend, FileWriteActuator fileWriteActuator) {
        final String type = fileWriteActuator.type();
        if (DISK_RESOURCE_TYPE.equalsIgnoreCase(type)) {
            final String path = folderStorageLocationPathService.path();
            final String relativePath = FileUtil.composePath(path, name);
            final String absolutePath = FileUtil.convertAbsolutePath(
                    FileUtil.composePath(fileWriteActuator.path(), relativePath));
            final File file = new File(absolutePath);
            final File folder = file.getParentFile();
            if (!FileUtil.checkFolderExist(folder)) {
                FileUtil.createFolder(folder);
            }
            return fileWriteActuator
                    .execute(file)
                    .flatMap(f -> {
                        final FileActuatorModel fam = new FileActuatorModel();
                        fam.setName(name);
                        fam.setPath(path);
                        fam.setLength(f.length());
                        fam.setType(DISK_RESOURCE_TYPE);
                        return Mono.just(fam);
                    });
        } else {
            return Mono.error(new ResourceNodeException(
                    this.getClass(),
                    "fun write(String name, Map<String, Object> extend, FileWriteActuator fileWriteActuator). " +
                            "-> Unable to process current [" + type + "]resource type data.",
                    "Unable to process current[" + type + "] resource type data")
            );
        }
    }

    @Override
    public Mono<FileReadActuator> read(
            String type, String base, String path, MediaType mediaType, Map<String, Object> extend
    ) {
        final File file = new File(FileUtil.composePath(base, path));
        return Mono.just(new FileReadActuator() {

            @Override
            public MediaType mediaType() {
                return mediaType;
            }

            @Override
            public FileActuatorModel model() {
                return new FileActuatorModel()
                        .setType(type)
                        .setPath(path)
                        .setName(file.getName())
                        .setLength(file.length());
            }

            @Override
            public Flux<DataBuffer> execute() {
                return FileUtil.readFile(file);
            }

            @Override
            public Flux<DataBuffer> execute(long position, long size) {
                return FileUtil.readFile(file, position, size);
            }
        });
    }

}
