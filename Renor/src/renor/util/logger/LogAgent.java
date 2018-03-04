package renor.util.logger;

import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogAgent implements ILogAgent {
	private final Logger logger;
	private final String loggerName;
	private final String loggerPrefix;
	private final String logFile;

	public LogAgent(String loggerName, String loggerPrefix, String logFile) {
		this.loggerName = loggerName;
		this.loggerPrefix = loggerPrefix;
		this.logFile = logFile;

		logger = Logger.getLogger(loggerName);
		setupLogger();
	}

	private void setupLogger() {
		logger.setUseParentHandlers(false);
		Handler[] handlers = logger.getHandlers();

		for (int i = 0; i < handlers.length; ++i) {
			Handler handler = handlers[i];
			logger.removeHandler(handler);
		}

		LogFormatter formatter = new LogFormatter(this);
		ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setFormatter(formatter);
		logger.addHandler(consoleHandler);

		try {
			FileHandler fileHandler = new FileHandler(logFile, true);
			fileHandler.setFormatter(formatter);
			logger.addHandler(fileHandler);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to log " + loggerName + " to " + logFile, e);
		}
	}

	public void logInfo(String message) {
		logger.log(Level.INFO, message);
	}

	public void logWarning(String message) {
		logger.log(Level.WARNING, message);
	}

	public void logWarningException(String message, Throwable throwable) {
		logger.log(Level.WARNING, message, throwable);
	}

	public void logSevere(String message) {
		logger.log(Level.SEVERE, message);
	}

	public void logSevereException(String message, Throwable throwable) {
		logger.log(Level.SEVERE, message, throwable);
	}

	public void logFine(String message) {
		logger.log(Level.FINE, message);
	}

	static String getLoggerPrefix(LogAgent logAgent) {
		return logAgent.loggerPrefix;
	}
}
