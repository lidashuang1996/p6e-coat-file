package club.p6e.coat.file;

import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.lang.annotation.*;

/**
 * @author lidashuang
 * @version 1.0
 */
@Documented
@EnableScheduling
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(AutoConfigureImportSelector.class)
public @interface EnableP6eFile {
}
