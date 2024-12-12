package sustech.java2finalproject.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${openapi.dev-url}")
    private String devUrl;

    @Value("${openapi.prod-url}")
    private String prodUrl;


    @Bean
    public OpenAPI openAPI() {

        Server devServer = new Server();
        devServer.setUrl(devUrl);
        devServer.setDescription("Server URL in Development environment");

        Server prodServer = new Server();
        prodServer.setUrl(prodUrl);
        prodServer.setDescription("Server URL in Production environment");

        return new OpenAPI()
                .info(new Info()
                        .title("CS209A-Final Project")
                        .description("Data Analysis of Stack Overflow Questions")
                        .version("1.0")
                        .contact(new Contact()
                                .name("12113053-THA SREYNY, 12112351-Fitria Zusni Farida")
                                .email("12113053@mail.sustech.edu.cn, 12112351@mail.sustech.edu.cn")
                        )
                        .license(new License().name("SUSTech License")
                                .url("https://github.com/sreyny-dev/java2-final-project")
                        )
                )
                .servers(List.of(devServer, prodServer));
    }


}
