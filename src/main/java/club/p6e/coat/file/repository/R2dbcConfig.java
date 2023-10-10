package club.p6e.coat.file.repository;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;

/**
 * R2DBC 配置
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@Configuration
public class R2dbcConfig {

    /**
     * DatabaseClient 服务
     */
    private final ConnectionFactory connectionFactory;

    /**
     * 构造初始化
     *
     * @param connectionFactory ConnectionFactory 服务
     */
    public R2dbcConfig(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Bean
    public R2dbcEntityTemplate r2dbcEntityTemplate() {
        return new R2dbcEntityTemplate(connectionFactory);
    }

}
