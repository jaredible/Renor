package renor.util.throwable;

import renor.util.crashreport.CrashReport;

public class ReportedException extends RuntimeException {
	private final CrashReport theReportedExceptionCrashReport;

	public ReportedException(CrashReport crashReport) {
		theReportedExceptionCrashReport = crashReport;
	}

	public CrashReport getCrashReport() {
		return theReportedExceptionCrashReport;
	}

	public String getMessage() {
		return theReportedExceptionCrashReport.getDescription();
	}
}
