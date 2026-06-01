package com.example.entities;

import com.example.ModItems;
import com.example.StaffItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.UUID;

public class BattleAllayEntity extends Vex {

    @Nullable
    private UUID ownerUuid;

    // 1. Define our State Tracker for the Stationary/Following mode
    private static final EntityDataAccessor<Boolean> DATA_STATIONARY_ID =
            SynchedEntityData.defineId(BattleAllayEntity.class, EntityDataSerializers.BOOLEAN);

    public BattleAllayEntity(EntityType<? extends BattleAllayEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        // Initialize the stationary state to false (Following) by default
        builder.define(DATA_STATIONARY_ID, false);
    }

    public boolean isStationary() {
        return this.entityData.get(DATA_STATIONARY_ID);
    }

    public void setStationary(boolean stationary) {
        this.entityData.set(DATA_STATIONARY_ID, stationary);
    }

    // --- AI GOALS REGISTRATION ---

    @Override
    protected void registerGoals() {
        super.registerGoals(); // Keeps the vanilla Vex attacking/charging behaviors

        // Custom Goal: Follow the owner if not stationary
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this));

        // Custom Goal: Attack ANY nearby Monster (Zombies, Skeletons, etc.)
        // We add a check to make sure it doesn't try to attack other Battle Allays!
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false,
                (target) -> !(target instanceof BattleAllayEntity)));
    }

    // --- INTERACTION (STAFF CONTROL) ---

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Check if the player clicking is the owner, and they are holding the summoner item
        if (this.ownerUuid != null && player.getUUID().equals(this.ownerUuid)) {
            if (stack.getItem() instanceof StaffItem) {

                // Only run logical state changes on the server side
                if (!this.level().isClientSide()) {
                    boolean newState = !this.isStationary();
                    this.setStationary(newState);

                    // Optional: Send a nice chat message to the owner confirming the state
                    String message = newState ? "Battle Allay is now Stationary." : "Battle Allay is Following.";
                    //player.displayClientMessage(Component.literal(message), true);

                    // Clear its current target and navigation so it stops what it's doing immediately
                    this.setTarget(null);
                }

                // Swing the staff successfully
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }
        return super.mobInteract(player, hand);
    }

    /**
     * Prevents the Vex from wandering around randomly if it has been told to stay put.
     */
    @Override
    public void tick() {
        super.tick();
        // If stationary and not actively fighting, lock its movement so it just hovers in place
        if (this.isStationary() && this.getTarget() == null && !this.level().isClientSide()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.5, 0.5, 0.5)); // Gently slow to a halt
        }
    }

    // --- PREVIOUS METHODS (Unchanged) ---

    public void setPlayerOwner(@Nullable Player player) {
        if (player != null) {
            this.ownerUuid = player.getUUID();
        } else {
            this.ownerUuid = null;
        }
    }

    @Nullable
    public Player getPlayerOwner() {
        if (this.ownerUuid != null) {
            return this.level().getPlayerByUUID(this.ownerUuid);
        }
        return null;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (this.getOwner() != null && target == this.getOwner()) return false;
        if (this.ownerUuid != null && target.getUUID().equals(this.ownerUuid)) return false;
        return super.canAttack(target);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (this.ownerUuid != null && target != null && target.getUUID().equals(this.ownerUuid)) {
            super.setTarget(null);
            return;
        }
        super.setTarget(target);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (this.ownerUuid != null && target.getUUID().equals(this.ownerUuid)) return false;
        return super.doHurtTarget(level, target);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.ownerUuid != null) {
            output.putString("OwnerUUID", this.ownerUuid.toString());
        }
        // Save the stationary state
        output.writeBoolean("IsStationary", this.isStationary());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        String uuidStr = input.getStringOr("OwnerUUID", "");
        if (!uuidStr.isEmpty()) {
            try {
                this.ownerUuid = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException e) {
                this.ownerUuid = null;
            }
        }
        // Load the stationary state
        this.setStationary(input.getBooleanOr("IsStationary", false));
    }

    // --- CUSTOM AI GOALS ---

    /**
     * A custom goal that makes the Vex fly smoothly toward the owner,
     * utilizing the Vex's native MoveControl so it can clip through blocks to reach you.
     */
    class FollowOwnerGoal extends Goal {
        private final BattleAllayEntity allay;

        public FollowOwnerGoal(BattleAllayEntity allay) {
            this.allay = allay;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            // Don't follow if we are stationary, or if we are actively attacking something
            if (this.allay.isStationary() || this.allay.getTarget() != null) return false;

            Player owner = this.allay.getPlayerOwner();
            // Start following if the owner is more than 5 blocks (25 squared) away
            return owner != null && this.allay.distanceToSqr(owner) > 25.0;
        }

        @Override
        public void tick() {
            Player owner = this.allay.getPlayerOwner();
            if (owner != null) {
                // Set the destination slightly above the owner's head
                this.allay.getMoveControl().setWantedPosition(
                        owner.getX(),
                        owner.getY() + 1.5,
                        owner.getZ(),
                        1.0D // Speed modifier
                );
            }
        }
    }
}