package api.response.apiResponse.Logger;

public class Utils {
    public static void runLoggers(Logger[] loggers, String message) {
        for (Logger logger : loggers) {
            logger.log(message);
        }
    }
}
