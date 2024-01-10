package api.response.apiResponse.Logger;

import org.jetbrains.annotations.NotNull;

public class Utils {
    public static void runLoggers(Logger @NotNull [] loggers, String message) {
        for (Logger logger : loggers) {
            logger.log(message);
        }
    }
}
