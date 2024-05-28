package logger;

import exceptions.ApiException;
import exceptions.NotAuthorizedException;
import io.javalin.http.Context;
import io.javalin.http.Handler;

import java.io.IOException;
import java.util.logging.*;

public class CustomLogger<T> {
    private static final Logger logger = Logger.getLogger(CustomLogger.class.getName());
    private static final String logFilePath = "errors.txt";

    static {
        try {
            FileHandler fileHandler = new FileHandler(logFilePath);
            fileHandler.setFormatter(new SimpleFormatter() {
                @Override
                public String format(LogRecord record) {
                    return record.getMessage() + "\n";
                }
            });
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false);
        } catch (IOException e) {
            throw new RuntimeException("Error creating log file: " + logFilePath, e);
        }
    }

    public Handler handleExceptions(Handler handler) {
        return ctx -> {
            try {
                handler.handle(ctx);
            } catch (ApiException e) {
                logException(ctx, e);
                ctx.json("{\"status\": \"" + e.getStatusCode() + "\", \"message\": \"" + e.getMessage() + "\", \"timestamp\": \"" + e.getTimeStamp() + "\"}");
            } catch (NotAuthorizedException e) {
                logException(ctx, e);
                ctx.json("{\"status\": \"" + e.getStatusCode() + "\", \"message\": \"" + e.getMessage() + "\", \"timestamp\": \"" + e.getTimeStamp() + "\"}");
            }
        };
    }

    private void logException(Context ctx, Exception e) {
        String logMessage = "Method: " + ctx.method() + ", ";
        if (e instanceof ApiException) {
            logMessage += "Status: " + ((ApiException) e).getStatusCode() + ", ";
            logMessage += "Timestamp: " + ((ApiException) e).getTimeStamp() + ", ";
        } else if (e instanceof NotAuthorizedException) {
            logMessage += "Status: " + ((NotAuthorizedException) e).getStatusCode() + ", ";
            logMessage += "Timestamp: " + ((NotAuthorizedException) e).getTimeStamp() + ", ";
        }
        logMessage += "Message: " + e.getMessage() + ", ";
        logMessage += "IP Address: " + ctx.ip();
        logger.log(Level.SEVERE, logMessage);
    }
}
