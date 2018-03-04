package renor.model;

import renor.level.entity.Entity;
import renor.renderer.ModelRenderer;
import renor.util.MathHelper;

public class ModelBiped extends ModelBase {
	public ModelRenderer head;
	public ModelRenderer body;
	public ModelRenderer leftUpperArm;
	public ModelRenderer leftLowerArm;
	public ModelRenderer rightUpperArm;
	public ModelRenderer rightLowerArm;
	public ModelRenderer leftUpperLeg;
	public ModelRenderer leftLowerLeg;
	public ModelRenderer rightUpperLeg;
	public ModelRenderer rightLowerLeg;
	public boolean isSneak;

	public ModelBiped(int textureWidth, int textureHeight) {
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;

		head = new ModelRenderer(this, 0, 0);
		// head.addBox(-4.0f, -8.0f, -4.0f, 8, 8, 8, 0.0f);
		// head.setRotationPoint(0.0f, 0.0f, 0.0f);
		body = new ModelRenderer(this, 16, 16);
		// bipedBody.addBox(-4.0f, 0.0f, -2.0f, 8, 12, 4, 0.0f);
		// bipedBody.setRotationPoint(0.0f, 0.0f, 0.0f);
		isSneak = false;
	}

	public void render(Entity entity, float par0, float par1, float par2, float par3, float par4, float par5) {
		head.render(par5);
		body.render(par5);
		// leftUpperArm.render(par5);
		// leftLowerArm.render(par5);
		// rightUpperArm.render(par5);
		// rightLowerArm.render(par5);
		// leftUpperLeg.render(par5);
		// leftLowerLeg.render(par5);
		// rightUpperLeg.render(par5);
		// rightLowerLeg.render(par5);
	}

	public void setRotationAngles(float par0, float par1, float par2, float par3, float par4, float par5, Entity entity) {
		head.rotationAngleY = par3;
		head.rotationAngleX = par4;

		float var0;
		if (onGround >= -9990.0f) {
			var0 = onGround;
			body.rotationAngleY = MathHelper.sin(MathHelper.sqrt_float(var0) * (float) Math.PI * 2.0f) * 0.2f;
		}

		if (isSneak) {
			body.rotationAngleX = 0.5f;
			head.rotationPointY = 1.0f;
		} else {
			body.rotationAngleX = 0.0f;
			head.rotationPointY = 0.0f;
		}
	}
}
