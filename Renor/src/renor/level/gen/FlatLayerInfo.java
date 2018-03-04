package renor.level.gen;

public class FlatLayerInfo {
	private int layerCount;
	private int layerFillBlock;
	private int layerFillBlockMeta;
	private int layerMinimumY;

	public FlatLayerInfo(int layerCount, int layerFillBlock) {
		this.layerCount = layerCount;
		this.layerFillBlock = layerFillBlock;

		layerFillBlockMeta = 0;
		layerMinimumY = 0;
	}
}
