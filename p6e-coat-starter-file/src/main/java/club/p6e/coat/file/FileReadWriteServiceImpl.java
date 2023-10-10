package club.p6e.coat.file;

import club.p6e.coat.file.utils.FileUtil;
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
public class FileReadWriteServiceImpl implements FileReadWriteService {

    private Properties properties;
    private FolderStorageLocationPathService folderStorageLocationPathService;

    public FileReadWriteServiceImpl(Properties properties, FolderStorageLocationPathService folderStorageLocationPathService) {
        this.properties = properties;
        this.folderStorageLocationPathService = folderStorageLocationPathService;
    }

    @Override
    public Mono<FileModel> write(String name, Map<String, Object> extend, FileWriteActuator fileWriteActuator) {
        final Object node = extend.get("node");
        if (node instanceof final String content) {
            final Properties.Upload upload = properties.getUpload().get(content);
            final String path = FileUtil.composePath(folderStorageLocationPathService.path(), name);
            final String absolutePath = FileUtil.convertAbsolutePath(FileUtil.composePath(upload.getPath(), path));
            return fileWriteActuator
                    .execute(new File(absolutePath))
                    .flatMap(file -> {
                        final FileModel fileModel = new FileModel();
                        fileModel.setName(name);
                        fileModel.setPath(path);
                        fileModel.setType("DISK");
                        fileModel.setLength(file.length());
                        return Mono.just(fileModel);
                    });
        }
        return Mono.empty();
    }

    @Override
    public Mono<FileReadActuator> read(String type, String base, String path, MediaType mediaType, Map<String, Object> extend) {
        final File file = new File(FileUtil.composePath(base, path));
        return Mono.just(new FileReadActuator() {
            @Override
            public String name() {
                return FileUtil.name(path);
            }

            @Override
            public MediaType mediaType() {
                return mediaType;
            }

            @Override
            public long length() {
                return file.length();
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
