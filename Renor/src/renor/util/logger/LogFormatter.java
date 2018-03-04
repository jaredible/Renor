package renor.util.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {
	private SimpleDateFormat timestamp;
	final LogAgent logAgent;

	public LogFormatter(LogAgent logAgent) {
		this.logAgent = logAgent;

		timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}

	public String format(LogRecord logRecord) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(timestamp.format(Long.valueOf(logRecord.getMillis())));

		if (LogAgent.getLoggerPrefix(logAgent) != null) stringBuilder.append(LogAgent.getLoggerPrefix(logAgent));

		stringBuilder.append(" [").append(logRecord.getLevel().getName()).append("] ");
		stringBuilder.append(formatMessage(logRecord));
		stringBuilder.append('\n');
		Throwable throwable = logRecord.getThrown();

		if (throwable != null) {
			StringWriter stringWriter = new StringWriter();
			throwable.printStackTrace(new PrintWriter(stringWriter));
			stringBuilder.append(stringWriter.toString());
		}

		return stringBuilder.toString();
	}
}
