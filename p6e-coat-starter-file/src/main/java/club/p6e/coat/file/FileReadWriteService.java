package club.p6e.coat.file;

import lombok.Data;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Map;

/**
 * @author lidashuang
 * @version 1.0
 */
public interface FileReadWriteService {

    @Data
    public static class FileModel implements Serializable {
        private String name;
        private String path;
        private String type;
        private long length;
    }

    public interface FileWriteActuator {
        Mono<File> execute(File file);
    }

    public interface FileReadActuator {
        String name();

        MediaType mediaType();
        long length();
        Flux<DataBuffer> execute();

        Flux<DataBuffer> execute(long position, long size);
    }

    public Mono<FileModel> write(String name, Map<String, Object> extend, FileWriteActuator fileWriteActuator);

    public Mono<FileReadActuator> read(String type, String base, String path, MediaType mediaType, Map<String, Object> extend);


}
