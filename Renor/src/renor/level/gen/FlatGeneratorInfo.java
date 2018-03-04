package renor.level.gen;

import java.util.ArrayList;
import java.util.List;

public class FlatGeneratorInfo {
	private final List<FlatLayerInfo> flatLayers = new ArrayList<FlatLayerInfo>();

	public List<FlatLayerInfo> getFlatLayers() {
		return flatLayers;
	}

	public static FlatGeneratorInfo createFlatGeneratorFromString(String options) {
		if (options == null) return getDefaultFlatGenerator();
		else {
			// do other stuff in here
			return getDefaultFlatGenerator();
		}
	}

	public static FlatGeneratorInfo getDefaultFlatGenerator() {
		// flatGeneratorInfo
		FlatGeneratorInfo info = new FlatGeneratorInfo();
		// info.getFlatLayers().add(new FlatLayerInfo(1, Block.bedrock.blockId));
		// info.getFlatLayers().add(new FlatLayerInfo(2, Block.dirt.blockId));
		// info.getFlatLayers().add(new FlatLayerInfo(1, Block.grass.blockId));
		return info;
	}
}
