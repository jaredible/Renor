package renor.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class NBTBase {
	private String name;

	protected NBTBase(String name) {
		if (name == null) this.name = "";
		else this.name = name;
	}

	abstract void write(DataOutput dataOutput) throws IOException;

	abstract void load(DataInput dataInput) throws IOException;

	public abstract int getId();

	public static NBTBase readNamedTag(DataInput dataInput) throws IOException {
		return null;
	}

	public static void writeNamedTag(NBTBase nbtBase, DataOutput dataOutput) {
	}

	public abstract NBTBase copy();

	public int hashCode() {
		return name.hashCode() ^ getId();
	}
}
