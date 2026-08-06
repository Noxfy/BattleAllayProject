package com.example.client.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.model.animal.allay.AllayModel;
import net.minecraft.client.renderer.entity.state.AllayRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.client.model.monster.vex.VexModel;

public class BattleAllayEntityModel extends EntityModel<BattleAllayEntityRenderState> implements ArmedModel<BattleAllayEntityRenderState> {
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart right_arm;
    private final ModelPart left_arm;
    private final ModelPart right_wing;
    private final ModelPart left_wing;
    private static final float FLYING_ANIMATION_X_ROT = ((float)Math.PI / 4F);
    private static final float MAX_HAND_HOLDING_ITEM_X_ROT_RAD = -1.134464F;
    private static final float MIN_HAND_HOLDING_ITEM_X_ROT_RAD = (-(float)Math.PI / 3F);

    public BattleAllayEntityModel(final ModelPart root) {
        super(root.getChild("root"), RenderTypes::entityTranslucent);
        this.head = this.root.getChild("head");
        this.body = this.root.getChild("body");
        this.right_arm = this.body.getChild("right_arm");
        this.left_arm = this.body.getChild("left_arm");
        this.right_wing = this.body.getChild("right_wing");
        this.left_wing = this.body.getChild("left_wing");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 23.5F, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -5.0F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.99F, 0.0F));
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 10).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 16).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 5.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offset(0.0F, -4.0F, 0.0F));
        body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(23, 0).addBox(-0.75F, -0.5F, -1.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(-0.01F)), PartPose.offset(-1.75F, 0.5F, 0.0F));
        body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(23, 6).addBox(-0.25F, -0.5F, -1.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(-0.01F)), PartPose.offset(1.75F, 0.5F, 0.0F));
        body.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(16, 14).addBox(0.0F, 1.0F, 0.0F, 0.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 0.0F, 0.6F));
        body.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(16, 14).addBox(0.0F, 1.0F, 0.0F, 0.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 0.0F, 0.6F));
        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    public void setupAnim(final BattleAllayEntityRenderState state) {
        super.setupAnim(state);
        float animationSpeed = state.walkAnimationSpeed;
        float animationPos = state.walkAnimationPos;
        float flapSpeed = state.ageInTicks * 20.0F * ((float)Math.PI / 180F) + animationPos;
        float flapAmount = Mth.cos((double)flapSpeed) * (float)Math.PI * 0.15F + animationSpeed;
        float idleBobSpeed = state.ageInTicks * 9.0F * ((float)Math.PI / 180F);
        float flyingFactor = Math.min(animationSpeed / 0.3F, 1.0F);
        float idleBobFactor = 1.0F - flyingFactor;
        float holdingItemFactor = state.holdingAnimationProgress;
        if (state.isDancing) {
            float danceSpeed = state.ageInTicks * 8.0F * ((float)Math.PI / 180F) + animationSpeed;
            float danceFrequency = Mth.cos((double)danceSpeed) * 16.0F * ((float)Math.PI / 180F);
            float spinningRotation = state.spinningProgress;
            float headTiltZ = Mth.cos((double)danceSpeed) * 14.0F * ((float)Math.PI / 180F);
            float headTiltY = Mth.cos((double)danceSpeed) * 30.0F * ((float)Math.PI / 180F);
            this.root.yRot = state.isSpinning ? 12.566371F * spinningRotation : this.root.yRot;
            this.root.zRot = danceFrequency * (1.0F - spinningRotation);
            this.head.yRot = headTiltY * (1.0F - spinningRotation);
            this.head.zRot = headTiltZ * (1.0F - spinningRotation);
        } else {
            this.head.xRot = state.xRot * ((float)Math.PI / 180F);
            this.head.yRot = state.yRot * ((float)Math.PI / 180F);
        }

        this.right_wing.xRot = 0.43633232F * (1.0F - flyingFactor);
        this.right_wing.yRot = (-(float)Math.PI / 4F) + flapAmount;
        this.left_wing.xRot = 0.43633232F * (1.0F - flyingFactor);
        this.left_wing.yRot = ((float)Math.PI / 4F) - flapAmount;
        this.body.xRot = flyingFactor * ((float)Math.PI / 4F);

// Body floating bobbing stays active
        ModelPart var10000 = this.root;
        var10000.y += (float)Math.cos((double)idleBobSpeed) * 0.25F * idleBobFactor;

// ==========================================
// 2. ARMS (FROZEN AT 40% PHASE)
// ==========================================
// Fixed progress angle locks arm motion completely
        float progress40 = (float)Math.PI * .8f;

        float armFlyingRotX = holdingItemFactor * Mth.lerp(flyingFactor, (-(float)Math.PI / 3F), -1.134464F);
        this.right_arm.xRot = armFlyingRotX;
        this.left_arm.xRot = armFlyingRotX;

        float armIdleBobFactor = idleBobFactor * (1.0F - holdingItemFactor);

// Uses 'progress40' instead of 'idleBobSpeed' so the arm position never changes
        float armIdleBobAmount = 0.43633232F - Mth.cos(progress40 + ((float)Math.PI * 1.5F)) * (float)Math.PI * 0.075F * armIdleBobFactor;

        this.left_arm.zRot = -armIdleBobAmount;
        this.right_arm.zRot = armIdleBobAmount;
        this.right_arm.yRot = 0.27925268F * holdingItemFactor;
        this.left_arm.yRot = -0.27925268F * holdingItemFactor;
    }

    public void translateToHand(final BattleAllayEntityRenderState state, final HumanoidArm arm, final PoseStack poseStack) {
        float yOffset = 0.0F;
        float zOffset = 3.0F;

        this.root.translateAndRotate(poseStack);
        this.body.translateAndRotate(poseStack);

// Arm pivot
        poseStack.translate(-0.0625F, -0.1F, 0.0F);

// Rotations
        poseStack.mulPose(Axis.XP.rotation(this.right_arm.xRot));
        poseStack.mulPose(Axis.YP.rotation(this.right_arm.yRot));
        poseStack.mulPose(Axis.ZP.rotation(this.right_arm.zRot));

// Scale
        poseStack.scale(0.6F, 0.7F, 0.7F);

// Offset: Decreased Y to move the item UP into the palm
        poseStack.translate(0.025F, -.050, 0.05F);
    }
}