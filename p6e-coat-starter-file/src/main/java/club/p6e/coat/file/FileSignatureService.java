package club.p6e.coat.file;

import reactor.core.publisher.Mono;

import java.io.File;

/**
 * 文件签名服务
 *
 * @author lidashuang
 * @version 1.0
 */
public interface FileSignatureService {

    /**
     * 文件签名服务的执行
     *
     * @param file 文件对象
     * @return 签名的内容
     */
    public Mono<String> execute(File file);

    /**
     * 摘要算法对象
     */
    public interface DigestAlgorithm {

        /**
         * 输入签名内容
         *
         * @param bytes 签名内容
         */
        public void input(byte[] bytes);

        /**
         * 获取签名结果
         *
         * @return 签名结果
         */
        public byte[] output();

    }

}
