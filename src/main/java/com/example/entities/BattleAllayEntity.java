package com.example.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.Objects;
import java.util.UUID;

public class BattleAllayEntity extends Vex {
    @Nullable
    private UUID ownerUuid; // Stores the unique ID of the player owner

    public BattleAllayEntity(EntityType<? extends BattleAllayEntity> type, Level level) {
        super(type, level);
    }

    /**
     * Sets the player owner of this Battle Allay.
     */
    public void setPlayerOwner(@Nullable Player player) {
        if (player != null) {
            this.ownerUuid = player.getUUID();
        } else {
            this.ownerUuid = null;
        }
    }

    /**
     * Dynamically finds and returns the online player matching the stored UUID.
     */
    @Nullable
    public Player getPlayerOwner() {
        if (this.ownerUuid != null) {
            return this.level().getPlayerByUUID(this.ownerUuid);
        }
        return null;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        // 1. Prevents the Vex from attacking its vanilla owner (like an Evoker)
        if (this.getOwner() != null && target == this.getOwner()) {
            return false;
        }

        // 2. Prevents the Vex from attacking its custom Player owner
        Player playerOwner = this.getPlayerOwner();
        if (playerOwner != null && target == playerOwner) {
            return false;
        }

        return super.canAttack(target);
    }

    // --- Modern Persistence (Value I/O Serialization) ---

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        // Safely write the UUID as a String if it exists
        if (this.ownerUuid != null) {
            output.putString("OwnerUUID", this.ownerUuid.toString());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        // Grab the string, defaulting to empty if it doesn't exist yet
        String uuidStr = input.getStringOr("OwnerUUID", "");
        if (!uuidStr.isEmpty()) {
            try {
                this.ownerUuid = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException e) {
                this.ownerUuid = null; // Guard against corrupted data strings
            }
        }
    }
}
