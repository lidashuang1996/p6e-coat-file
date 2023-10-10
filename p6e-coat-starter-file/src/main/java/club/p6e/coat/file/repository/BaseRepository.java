package club.p6e.coat.file.repository;

/**
 * 基础的存储库
 *
 * @author lidashuang
 * @version 1.0
 */
public class BaseRepository {

    /**
     * 最大重试次数
     */
    public static final int MAX_RETRY_COUNT = 3;

    /**
     * 重试间隔时间
     */
    public static final long RETRY_INTERVAL_DATE = 5000;

    /**
     * 大写字母转为下划线分割
     *
     * @param text 文本内容
     * @return 转换内容
     */
    public static String textCapitalToUnderline(String text) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) >= 'A' && text.charAt(i) <= 'Z') {
                sb.append("_");
            }
            sb.append(String.valueOf(text.charAt(i)).toLowerCase());
        }
        return sb.toString();
    }

}
