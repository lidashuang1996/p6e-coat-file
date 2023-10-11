package club.p6e.coat.file.utils;

import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lidashuang
 * @version 1.0
 */
@SuppressWarnings("ALL")
public final class SpringUtil {

    /**
     * 定义类
     */
    public interface Definition {

        /**
         * 初始化 Spring Boot 的上下文对象
         *
         * @param application Spring Boot 的上下文对象
         */
        public void init(ApplicationContext application);

        /**
         * 通过 Spring Boot 的上下文对象判断 Bean 是否存在
         *
         * @param tClass Bean 的类型
         * @return boolean 是否存在 Bean
         */
        public boolean exist(Class<?> tClass);

        /**
         * 通过 Spring Boot 的上下文对象获取 Bean 对象
         *
         * @param tClass Bean 的类型
         * @param <T>    Bean 的类型泛型
         * @return Bean 对象
         */
        public <T> T getBean(Class<T> tClass);

        /**
         * 通过 Spring Boot 的上下文对象获取 Bean 对象集合
         *
         * @param tClass Bean 的类型
         * @param <T>    Bean 的类型泛型
         * @return Bean 对象集合
         */
        public <T> Map<String, T> getBeans(Class<T> tClass);

    }

    /**
     * 实现类
     */
    public static class Implementation implements Definition {

        /**
         * 全局的 Spring Boot 的上下文对象
         */
        private ApplicationContext application = null;

        @Override
        public void init(ApplicationContext application) {
            this.application = application;
        }

        @Override
        public boolean exist(Class<?> tClass) {
            try {
                this.application.getBean(tClass);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public <T> T getBean(Class<T> tClass) {
            return application == null ? null : application.getBean(tClass);
        }

        @Override
        public <T> Map<String, T> getBeans(Class<T> tClass) {
            return application == null ? new HashMap<>(0) : application.getBeansOfType(tClass);
        }
    }

    /**
     * 默认的 SPRING 上下文实现类
     */
    private static Definition DEFINITION = new Implementation();

    /**
     * 设置 SPRING 上下文实现类
     *
     * @param implementation SPRING 上下文实现类
     */
    public static void set(Definition implementation) {
        DEFINITION = implementation;
    }

    /**
     * 初始化 Spring Boot 的上下文对象
     *
     * @param application Spring Boot 的上下文对象
     */
    public static void init(ApplicationContext application) {
        DEFINITION.init(application);
    }

    /**
     * 通过 Spring Boot 的上下文对象判断 Bean 是否存在
     *
     * @param tClass Bean 的类型
     * @return boolean 是否存在 Bean
     */
    public static boolean exist(Class<?> tClass) {
        return DEFINITION.exist(tClass);
    }

    /**
     * 通过 Spring Boot 的上下文对象获取 Bean 对象
     *
     * @param tClass Bean 的类型
     * @param <T>    Bean 的类型泛型
     * @return Bean 对象
     */
    public static <T> T getBean(Class<T> tClass) {
        return DEFINITION.getBean(tClass);
    }

    /**
     * 通过 Spring Boot 的上下文对象获取 Bean 对象集合
     *
     * @param tClass Bean 的类型
     * @param <T>    Bean 的类型泛型
     * @return Bean 对象集合
     */
    public static <T> Map<String, T> getBeans(Class<T> tClass) {
        return DEFINITION.getBeans(tClass);
    }

}
