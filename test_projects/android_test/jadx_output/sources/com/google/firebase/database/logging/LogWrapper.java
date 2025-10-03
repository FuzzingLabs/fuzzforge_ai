package com.google.firebase.database.logging;

import com.google.firebase.database.logging.Logger;
import java.io.PrintWriter;
import java.io.StringWriter;

/* loaded from: classes.dex */
public class LogWrapper {
    private final String component;
    private final Logger logger;
    private final String prefix;

    private static String exceptionStacktrace(Throwable e) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);
        return writer.toString();
    }

    public LogWrapper(Logger logger, String component) {
        this(logger, component, null);
    }

    public LogWrapper(Logger logger, String component, String prefix) {
        this.logger = logger;
        this.component = component;
        this.prefix = prefix;
    }

    public void error(String message, Throwable e) {
        String logMsg = toLog(message, new Object[0]) + "\n" + exceptionStacktrace(e);
        this.logger.onLogMessage(Logger.Level.ERROR, this.component, logMsg, now());
    }

    public void warn(String message) {
        warn(message, null);
    }

    public void warn(String message, Throwable e) {
        String logMsg = toLog(message, new Object[0]);
        if (e != null) {
            logMsg = logMsg + "\n" + exceptionStacktrace(e);
        }
        this.logger.onLogMessage(Logger.Level.WARN, this.component, logMsg, now());
    }

    public void info(String message) {
        this.logger.onLogMessage(Logger.Level.INFO, this.component, toLog(message, new Object[0]), now());
    }

    public void debug(String message, Object... args) {
        debug(message, null, args);
    }

    public boolean logsDebug() {
        return this.logger.getLogLevel().ordinal() <= Logger.Level.DEBUG.ordinal();
    }

    public void debug(String message, Throwable e, Object... args) {
        if (logsDebug()) {
            String logMsg = toLog(message, args);
            if (e != null) {
                logMsg = logMsg + "\n" + exceptionStacktrace(e);
            }
            this.logger.onLogMessage(Logger.Level.DEBUG, this.component, logMsg, now());
        }
    }

    private long now() {
        return System.currentTimeMillis();
    }

    private String toLog(String message, Object... args) {
        String formatted = args.length > 0 ? String.format(message, args) : message;
        if (this.prefix == null) {
            return formatted;
        }
        return this.prefix + " - " + formatted;
    }
}
