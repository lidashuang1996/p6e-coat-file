package club.p6e.coat.file.utils;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author lidashuang
 * @version 1.0
 */
@SuppressWarnings("ALL")
public final class GeneratorUtil {

    /**
     * 定义类
     */
    public interface Definition {

        /**
         * 生成 UUID 数据
         *
         * @return UUID
         */
        public String uuid();

        /**
         * 生成随机数据
         *
         * @param len      数据长度
         * @param isLetter 是否包含字母
         * @param isCase   是否包含大小写
         */
        public String random(int len, boolean isLetter, boolean isCase);

    }

    /**
     * 实现类
     */
    private static class Implementation implements Definition {

        /**
         * 基础的字符模型
         */
        private static final String[] BASE_DATA = new String[]{
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
                "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
                "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
                "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
                "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
        };

        @Override
        public String uuid() {
            return UUID.randomUUID().toString().replaceAll("-", "");
        }

        @Override
        public String random(int len, boolean isLetter, boolean isCase) {
            final StringBuilder sb = new StringBuilder();
            final int base = isLetter ? (isCase ? 62 : 36) : 10;
            for (int i = 0; i < len; i++) {
                sb.append(BASE_DATA[ThreadLocalRandom.current().nextInt(base)]);
            }
            return sb.toString();
        }

    }

    /**
     * 默认的随机数实现的对象
     */
    private static Definition DEFINITION = new Implementation();

    /**
     * 设置随机数实现的对象
     *
     * @param implementation 随机数实现的对象
     */
    public static void set(Definition implementation) {
        DEFINITION = implementation;
    }

    /**
     * 生成 UUID 数据
     *
     * @return UUID
     */
    public static String uuid() {
        return DEFINITION.uuid();
    }

    /**
     * 生成长度为 6 位数字格式的随机数
     */
    public static String random() {
        return DEFINITION.random(6, false, false);
    }

    /**
     * 生成随机数据
     *
     * @param len      数据长度
     * @param isLetter 是否包含字母
     * @param isCase   是否包含大小写
     */
    public static String random(int len, boolean isLetter, boolean isCase) {
        return DEFINITION.random(len, isLetter, isCase);
    }

}
