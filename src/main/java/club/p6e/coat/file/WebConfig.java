//package club.p6e.coat.file.file;
//
//import club.p6e.coat.file.file.UploadFileCleanStrategyService;
//import club.p6e.coat.file.file.UploadFileCleanTask;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.stereotype.Component;
//import org.springframework.web.reactive.config.CorsRegistry;
//import org.springframework.web.reactive.config.EnableWebFlux;
//import org.springframework.web.reactive.config.WebFluxConfigurer;
//
///**
// * @author lidashuang
// * @version 1.0
// */
//@Component
//@Configuration
//@EnableWebFlux
//public class WebConfig implements WebFluxConfigurer {
//
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")
//                .allowedOrigins("*")
//                .allowedMethods("*")
//                .allowedHeaders("*");
//    }
//
//    @Bean
//    public UploadFileCleanTask task(UploadFileCleanStrategyService service) {
//        return new UploadFileCleanTask(service);
//    }
//
//}
