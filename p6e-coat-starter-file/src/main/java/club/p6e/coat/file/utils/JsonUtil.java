package club.p6e.coat.file.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.Map;

/**
 * JSON 序列化和反序列化帮助类
 *
 * @author lidashuang
 * @version 1.0
 */
@SuppressWarnings("ALL")
public final class JsonUtil {

    /**
     * 定义类
     */
    public interface Definition {

        /**
         * 序列化对象
         *
         * @param o 对象
         * @return 序列化内容
         */
        public String toJson(Object o);

        /**
         * 反序列化 JSON 到对象
         *
         * @param json   json 内容
         * @param tClass 对象类型
         * @param <T>    类型
         * @return 对象
         */
        public <T> T fromJson(String json, Class<T> tClass);

        /**
         * 反序列化 JSON 到对象
         *
         * @param inputStream json 内容流
         * @param tClass      对象类型
         * @param <T>         类型
         * @return 对象
         */
        public <T> T fromJson(InputStream inputStream, Class<T> tClass);

        /**
         * 反序列化 JSON 到对象
         *
         * @param json   json 内容
         * @param kClass key 对象类型
         * @param vClass value 对象类型
         * @param <K>    key 对象类型
         * @param <V>    value 对象类型
         * @return 对象
         */
        public <K, V> Map<K, V> fromJsonToMap(String json, Class<K> kClass, Class<V> vClass);

    }

    /**
     * 实现类
     */
    private static class Implementation implements Definition {

        /**
         * OBJECT_MAPPER 对象
         */
        private final ObjectMapper om;

        /**
         * 构造方法初始化
         */
        public Implementation() {
            om = new ObjectMapper();
            om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }

        @Override
        public String toJson(Object o) {
            try {
                return om.writeValueAsString(o);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public <T> T fromJson(String json, Class<T> tClass) {
            try {
                return om.readValue(json, tClass);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public <T> T fromJson(InputStream inputStream, Class<T> tClass) {
            try {
                return om.readValue(inputStream, tClass);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public <K, V> Map<K, V> fromJsonToMap(String json, Class<K> kClass, Class<V> vClass) {
            try {
                return om.readValue(json, om.getTypeFactory().constructParametricType(Map.class, kClass, vClass));
            } catch (Exception e) {
                return null;
            }
        }
    }


    /**
     * 默认的 JSON 实现的对象
     */
    private static Definition DEFINITION = new Implementation();

    /**
     * 设置 JSON 实现的对象
     *
     * @param implementation JSON 实现的对象
     */
    public static void set(Definition implementation) {
        DEFINITION = implementation;
    }

    /**
     * 序列化对象
     *
     * @param o 对象
     * @return 序列化内容
     */
    public static String toJson(Object o) {
        return DEFINITION.toJson(o);
    }

    /**
     * 反序列化 JSON 到对象
     *
     * @param json   json 内容
     * @param tClass 对象类型
     * @param <T>    类型
     * @return 对象
     */
    public static <T> T fromJson(String json, Class<T> tClass) {
        return DEFINITION.fromJson(json, tClass);
    }

    /**
     * 反序列化 JSON 到对象
     *
     * @param inputStream json 内容流
     * @param tClass      对象类型
     * @param <T>         类型
     * @return 对象
     */
    public static <T> T fromJson(InputStream inputStream, Class<T> tClass) {
        return DEFINITION.fromJson(inputStream, tClass);
    }

    /**
     * 反序列化 JSON 到对象
     *
     * @param json   json 内容
     * @param kClass key 对象类型
     * @param vClass value 对象类型
     * @param <K>    key 对象类型
     * @param <V>    value 对象类型
     * @return 对象
     */
    public static <K, V> Map<K, V> fromJsonToMap(String json, Class<K> kClass, Class<V> vClass) {
        return DEFINITION.fromJsonToMap(json, kClass, vClass);
    }

}
