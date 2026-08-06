package com.example.client.entities;

import com.example.ExampleMod;
import com.example.entities.BattleAllayEntity;
import net.minecraft.client.model.animal.allay.AllayModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.entity.AllayRenderer;
import net.minecraft.client.renderer.entity.VexRenderer;

public class BattleAllayEntityRenderer extends MobRenderer<BattleAllayEntity, BattleAllayEntityRenderState, BattleAllayEntityModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ExampleMod.MOD_ID, "textures/entity/battle_allay.png");
    //private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/allay.png");

    public BattleAllayEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new BattleAllayEntityModel(  context.bakeLayer(ModEntityModelLayers.BATTLE_ALLAY)), 0.4f); // 0.375 shadow radius
        this.addLayer(new ItemInHandLayer<>(this));
    }

    @Override
    public BattleAllayEntityRenderState createRenderState() {
        return new BattleAllayEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(BattleAllayEntityRenderState state) {
        return TEXTURE;
    }

    @Override
    public void extractRenderState(final BattleAllayEntity entity, final BattleAllayEntityRenderState state, final float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        ArmedEntityRenderState.extractArmedEntityRenderState(entity, state, this.itemModelResolver, partialTicks);
        state.isCharging = entity.isCharging();
    }
}
