package renor.util.crashreport;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import renor.util.callable.CallableJVMFlags;
import renor.util.callable.CallableJavaInfo;
import renor.util.callable.CallableMemoryInfo;
import renor.util.callable.CallableOSInfo;
import renor.util.logger.ILogAgent;
import renor.util.throwable.ReportedException;

public class CrashReport {
	private final String description;
	private final Throwable cause;
	private File crashReportFile;
	private StackTraceElement[] stackTrace = new StackTraceElement[0];
	private final CrashReportCategory crashReportCategory = new CrashReportCategory(this, "System Details");

	public CrashReport(String description, Throwable cause) {
		this.description = description;
		this.cause = cause;

		populateEnvironment();
	}

	private void populateEnvironment() {
		crashReportCategory.addCrashSectionCallable("Operating System", new CallableOSInfo());
		crashReportCategory.addCrashSectionCallable("Java Version", new CallableJavaInfo());
		crashReportCategory.addCrashSectionCallable("Memory", new CallableMemoryInfo());
		crashReportCategory.addCrashSectionCallable("JVM Flags", new CallableJVMFlags());
	}

	public String getDescription() {
		return description;
	}

	public Throwable getCause() {
		return cause;
	}

	public File getFile() {
		return crashReportFile;
	}

	public String getCompleteReport() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("---- Renor Crash Report ----");
		stringBuilder.append("// ");
		stringBuilder.append(getWittyComment());
		stringBuilder.append("\n\n");
		stringBuilder.append("Time: ");
		stringBuilder.append(new SimpleDateFormat().format(new Date()));
		stringBuilder.append("\n");
		stringBuilder.append("Description: ");
		stringBuilder.append(description);
		stringBuilder.append("\n\n");
		stringBuilder.append(getCauseStackTraceOrString());
		stringBuilder.append("\n\nA detailed walkthrough of the error, its code path and all known details is as follows: \n");

		for (int i = 0; i < 87; ++i)
			stringBuilder.append("-");

		stringBuilder.append("\n\n");
		getSectionsForStringBuilder(stringBuilder);
		return stringBuilder.toString();
	}

	public String getCauseStackTraceOrString() {
		StringWriter stringWriter = null;
		PrintWriter printWriter = null;
		String cause = this.cause.toString();

		try {
			stringWriter = new StringWriter();
			printWriter = new PrintWriter(stringWriter);
			this.cause.printStackTrace();
			cause = stringWriter.toString();
		} finally {
			try {
				if (stringWriter != null) stringWriter.close();

				if (printWriter != null) printWriter.close();
			} catch (IOException e) {
			}
		}

		return cause;
	}

	public void getSectionsForStringBuilder(StringBuilder stringBuilder) {
		if (stackTrace != null && stackTrace.length > 0) {
		}
	}

	public boolean saveToFile(File saveFile, ILogAgent logAgent) {
		if (crashReportFile != null) return false;
		else {
			if (saveFile.getParentFile() != null) saveFile.getParentFile().mkdirs();

			try {
				FileWriter fileWriter = new FileWriter(saveFile);
				fileWriter.write(getCompleteReport());
				fileWriter.close();
				crashReportFile = saveFile;
				return true;
			} catch (Throwable e) {
				logAgent.logSevereException("Could not save crash report to " + saveFile, e);
				return false;
			}
		}
	}

	public static CrashReport makeCrashReport(Throwable throwable, String description) {
		CrashReport crashReport;

		if (throwable instanceof ReportedException) crashReport = ((ReportedException) throwable).getCrashReport();
		else crashReport = new CrashReport(description, throwable);

		return crashReport;
	}

	private static String getWittyComment() {
		return ":(";
	}
}
