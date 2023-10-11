package club.p6e.coat.file;

import club.p6e.coat.file.actuator.FileActuatorModel;
import club.p6e.coat.file.actuator.FileReadActuator;
import club.p6e.coat.file.actuator.FileWriteActuator;
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
     * 文件配置对象
     */
    private final Properties properties;

    /**
     * 文件存储位置的路径服务
     */
    private final FolderStorageLocationPathService folderStorageLocationPathService;

    /**
     * 构造方法初始化
     *
     * @param properties                       文件配置对象
     * @param folderStorageLocationPathService 文件存储位置的路径服务
     */
    public FileReadWriteServiceImpl(
            Properties properties,
            FolderStorageLocationPathService folderStorageLocationPathService) {
        this.properties = properties;
        this.folderStorageLocationPathService = folderStorageLocationPathService;
    }

    @Override
    public Mono<FileActuatorModel> write(String name, Map<String, Object> extend, FileWriteActuator fileWriteActuator) {
        System.out.println(extend);
        final Object node = extend.get("node");
        if (node instanceof final String content) {
            final Properties.Upload upload = properties.getUploads().get(content);
            final String path = folderStorageLocationPathService.path();
            final String relativePath = FileUtil.composePath(path, name);
            final String absolutePath = FileUtil.convertAbsolutePath(FileUtil.composePath(upload.getPath(), relativePath));
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
                        fam.setType("DISK");
                        fam.setLength(f.length());
                        System.out.println("family: " + fam);
                        return Mono.just(fam);
                    });
        } else {
            return Mono.empty();
        }
    }

    @Override
    public Mono<FileReadActuator> read(
            String type, String base, String path, MediaType mediaType, Map<String, Object> extend) {
        System.out.println(type);
        System.out.println(base);
        System.out.println(path);
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
