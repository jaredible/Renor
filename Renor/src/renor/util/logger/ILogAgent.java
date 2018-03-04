package renor.util.logger;

public abstract interface ILogAgent {
	public abstract void logInfo(String message);

	public abstract void logWarning(String message);

	public abstract void logWarningException(String message, Throwable throwable);

	public abstract void logSevere(String message);

	public abstract void logSevereException(String message, Throwable throwable);

	public abstract void logFine(String message);
}
