package club.p6e.coat.file.utils;

import org.springframework.context.ApplicationContext;

import java.util.Map;

/**
 * Spring 上下文对象帮助类
 *
 * @author lidashuang
 * @version 1.0
 */
public final class SpringUtil {


    /**
     * 全局的 Spring Boot 的上下文对象
     */
    private static ApplicationContext APPLICATION;

    /**
     * 初始化 Spring Boot 的上下文对象
     *
     * @param application Spring Boot 的上下文对象
     */
    @SuppressWarnings("ALL")
    public static void init(ApplicationContext application) {
        APPLICATION = application;
    }

    /**
     * 通过 Spring Boot 的上下文对象判断 Bean 是否存在
     *
     * @param tClass Bean 的类型
     * @return boolean 是否存在 Bean
     */
    @SuppressWarnings("ALL")
    public static boolean existBean(Class<?> tClass) {
        try {
            APPLICATION.getBean(tClass);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 通过 Spring Boot 的上下文对象获取 Bean 对象
     *
     * @param tClass Bean 的类型
     * @param <T>    Bean 的类型泛型
     * @return Bean 对象
     */
    @SuppressWarnings("ALL")
    public static <T> T getBean(Class<T> tClass) {
        return APPLICATION.getBean(tClass);
    }

    /**
     * 通过 Spring Boot 的上下文对象获取 Bean 对象集合
     *
     * @param tClass Bean 的类型
     * @param <T>    Bean 的类型泛型
     * @return Bean 对象集合
     */
    @SuppressWarnings("ALL")
    public static <T> Map<String, T> getBeans(Class<T> tClass) {
        return APPLICATION.getBeansOfType(tClass);
    }

}
