package hsbc.hw.transaction.system.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI transactionSystemAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("交易管理系统 API")
                        .description("提供交易记录的创建、查询、更新和删除功能")
                        .version("1.0"));
    }
}