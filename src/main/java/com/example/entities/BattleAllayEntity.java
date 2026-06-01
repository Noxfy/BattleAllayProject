package com.example.entities;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.Objects;

public class BattleAllayEntity extends Vex {

    public BattleAllayEntity(EntityType<? extends Vex> type, Level level) {
        super(type, level);
    }

    @Override
    protected @Nullable LivingEntity asValidTarget(@Nullable LivingEntity target) {
        if (target instanceof Player player) {
            if (player.isCreative() || player.isSpectator() || player.get) {
                return null;
            }
        }

        if (target != null && !this.canAttack(target)) {
            return null;
        } else {
            return target;
        }
    }
}
