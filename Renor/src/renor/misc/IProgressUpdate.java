package renor.misc;

public abstract interface IProgressUpdate {
	public abstract void displayProgressMessage(String message);

	public abstract void resetProgressAndMessage(String message);

	public abstract void resetProgressAndWorkingMessage(String message);

	public abstract void setLoadingProgress(int progress);

	public abstract void onNoMoreProgress();
}
