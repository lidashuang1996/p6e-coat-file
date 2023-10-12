package club.p6e.coat.file;

import club.p6e.coat.file.error.FileException;
import club.p6e.coat.file.utils.FileUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.File;
import java.security.MessageDigest;

/**
 * 文件签名服务的默认实现
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = FileSignatureService.class,
        ignored = FileSignatureServiceImpl.class
)
public class FileSignatureServiceImpl implements FileSignatureService {

    /**
     * HEX_CHARS
     */
    private static final char[] HEX_CHARS = new char[]
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    @Override
    public Mono<String> execute(File file) {
        final FileSignatureService.DigestAlgorithm digestAlgorithm = new DefaultDigestAlgorithm();
        return Mono.just(file)
                .flatMap(f -> FileUtil.checkFileExist(f) ? Mono.just(f) : Mono.error(new FileException(
                        FileUtil.class,
                        "fun execute(File file). -> The content read by the signature is not a file.",
                        "The content read by the signature is not a file"
                )))
                .flatMap(f -> FileUtil
                        .readFile(file)
                        .flatMap(buffer -> {
                            try {
                                final int count = buffer.readableByteCount();
                                final byte[] bytes = new byte[count];
                                buffer.read(bytes);
                                digestAlgorithm.input(bytes);
                                return Mono.just(count);
                            } finally {
                                DataBufferUtils.release(buffer);
                            }
                        }).count())
                .map(l -> digestAlgorithmBytesToHexString(digestAlgorithm.output()));
    }

    /**
     * 摘要算法转换为 HEX 格式的字符串
     *
     * @param bytes 等待转换的内容
     * @return 转换结果
     */
    private String digestAlgorithmBytesToHexString(byte[] bytes) {
        final char[] chars = new char[32];
        for (int i = 0; i < chars.length; i += 2) {
            final byte b = bytes[i / 2];
            chars[i] = HEX_CHARS[b >>> 4 & 15];
            chars[i + 1] = HEX_CHARS[b & 15];
        }
        return new String(chars);
    }

    /**
     * 默认的摘要算法实现
     */
    private static class DefaultDigestAlgorithm implements FileSignatureService.DigestAlgorithm {

        /**
         * 摘要算法对象
         */
        private final MessageDigest md;

        /**
         * 构造方法初始化
         */
        public DefaultDigestAlgorithm() {
            try {
                this.md = MessageDigest.getInstance("MD5");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void input(byte[] bytes) {
            md.update(bytes);
        }

        @Override
        public byte[] output() {
            return md.digest();
        }

    }

}
