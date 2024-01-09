package api.response.apiResponse.Logger;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DatabaseLogger implements Logger{

    @Override
    public void log(String message) {
        log.info("Saved to database: " + message);
    }
}
