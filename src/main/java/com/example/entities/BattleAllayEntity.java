package com.example.entities;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

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

        // 2. Direct UUID check: Fast and entirely bulletproof
        if (this.ownerUuid != null && target.getUUID().equals(this.ownerUuid)) {
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

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        // If the AI tries to target the player owner, refuse it completely
        if (this.ownerUuid != null && target != null && target.getUUID().equals(this.ownerUuid)) {
            super.setTarget(null);
            return;
        }
        super.setTarget(target);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        // Failsafe: Zero out any contact damage if it bumps into the owner
        if (this.ownerUuid != null && target.getUUID().equals(this.ownerUuid)) {
            return false;
        }
        return super.doHurtTarget(level, target);
    }
}