package renor.util.texture;

public abstract interface Icon {
	public abstract int getOriginX();

	public abstract int getOriginY();

	public abstract float getMinU();

	public abstract float getMaxU();

	public abstract float getInterpolatedU(double n);

	public abstract float getMinV();

	public abstract float getMaxV();

	public abstract float getInterpolatedV(double n);

	public abstract String getIconName();

	public abstract int getSheetWidth();

	public abstract int getSheetHeight();
}
