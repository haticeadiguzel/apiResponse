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
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class ApiResponseApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiResponseApplication.class, args);
    }

    int THREADS_COUNT = 2;

    @Bean
    public ModelMapper getModelMapper() {
        return new ModelMapper();
    }

    //For multiple Scheduled task. THREADS_COUNT is task number.
    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(THREADS_COUNT);
        return threadPoolTaskScheduler;
    }

    Logger[] loggers = {new DatabaseLogger(), new RedisLogger()};

    @Bean
    public Logger[] logger() {
        return loggers;
    }
}
