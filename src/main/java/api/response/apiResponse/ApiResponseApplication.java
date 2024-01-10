package api.response.apiResponse;

import api.response.apiResponse.Logger.DatabaseLogger;
import api.response.apiResponse.Logger.Logger;
import api.response.apiResponse.Logger.RedisLogger;
import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class ApiResponseApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiResponseApplication.class, args);
    }

    @Bean
    public ModelMapper getModelMapper() {
        return new ModelMapper();
    }

    Logger[] loggers = {new DatabaseLogger(), new RedisLogger()};

    @Bean
    public Logger[] logger() {
        return loggers;
    }
}
