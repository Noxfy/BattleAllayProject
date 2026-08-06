package com.example.client.entities;

import net.minecraft.client.renderer.entity.state.AllayRenderState;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.VexRenderState;

public class BattleAllayEntityRenderState extends ArmedEntityRenderState {
    public boolean isDancing;
    public boolean isSpinning;
    public float spinningProgress;
    public float holdingAnimationProgress;
    public boolean isCharging;
}
