package com.example.client.entities;

import com.example.ExampleMod;
import com.example.entities.BattleAllayEntity;
import net.minecraft.client.model.animal.allay.AllayModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class BattleAllayEntityRenderer extends MobRenderer<BattleAllayEntity, BattleAllayEntityRenderState, AllayModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ExampleMod.MOD_ID, "textures/entity/battle_allay.png");
    //private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/allay.png");

    public BattleAllayEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new AllayModel(  context.bakeLayer(ModEntityModelLayers.BATTLE_ALLAY)), 0.375f); // 0.375 shadow radius
    }

    @Override
    public BattleAllayEntityRenderState createRenderState() {
        return new BattleAllayEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(BattleAllayEntityRenderState state) {
        return TEXTURE;
    }
}
