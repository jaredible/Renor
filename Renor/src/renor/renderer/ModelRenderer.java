package renor.renderer;

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.List;

import renor.misc.GLAllocation;
import renor.model.ModelBase;
import renor.model.ModelBox;
import renor.util.Tessellator;

public class ModelRenderer {
	public final String boxName;
	public List<ModelBox> cubeList;
	private int displayList;
	private int textureOffsetX;
	private int textureOffsetY;
	public float textureWidth;
	public float textureHeight;
	public float rotationPointX;
	public float rotationPointY;
	public float rotationPointZ;
	public float rotationAngleX;
	public float rotationAngleY;
	public float rotationAngleZ;
	private boolean compiled;
	public boolean mirror;
	public boolean showModel;

	public ModelRenderer(ModelBase modelBase, String name) {
		boxName = name;

		cubeList = new ArrayList<ModelBox>();
		displayList = 0;
		textureWidth = 64.0f;
		textureHeight = 32.0f;
		compiled = false;
		mirror = false;
		showModel = true;
		setTextureSize(modelBase.textureWidth, modelBase.textureHeight);
	}

	public ModelRenderer(ModelBase modelBase) {
		this(modelBase, null);
	}

	public ModelRenderer(ModelBase modelBase, int textureOffsetX, int textureOffsetY) {
		this(modelBase);
		setTextureOffset(textureOffsetX, textureOffsetY);
	}

	public void render(float n) {
		if (showModel) {
			if (!compiled) compileDisplayList(n);

			glPushMatrix();
			glTranslatef(rotationPointX * n, rotationPointY * n, rotationPointZ * n);

			if (rotationAngleZ != 0.0f) glRotatef(rotationAngleZ * (180.0f / (float) Math.PI), 0.0f, 0.0f, 1.0f);

			if (rotationAngleY != 0.0f) glRotatef(rotationAngleY * (180.0f / (float) Math.PI), 0.0f, 1.0f, 0.0f);

			if (rotationAngleZ != 0.0f) glRotatef(rotationAngleZ * (180.0f / (float) Math.PI), 1.0f, 0.0f, 0.0f);

			glCallList(displayList);

			glPopMatrix();
		}
	}

	private void compileDisplayList(float n) {
		displayList = GLAllocation.generateDisplayLists(1);
		glNewList(displayList, GL_COMPILE);
		Tessellator tess = Tessellator.instance;

		for (int i = 0; i < cubeList.size(); ++i)
			cubeList.get(i).render(tess, n);

		glEndList();
		compiled = true;
	}

	public ModelRenderer setTextureSize(int textureWidth, int textureHeight) {
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
		return this;
	}

	public ModelRenderer setTextureOffset(int textureOffsetX, int textureOffsetY) {
		this.textureOffsetX = textureOffsetX;
		this.textureOffsetY = textureOffsetY;
		return this;
	}
}
