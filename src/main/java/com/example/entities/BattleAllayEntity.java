package com.example.entities;

import com.example.HornItem;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
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

    private static final EntityDataAccessor<Boolean> DATA_STATIONARY_ID =
            SynchedEntityData.defineId(BattleAllayEntity.class, EntityDataSerializers.BOOLEAN);

    public BattleAllayEntity(EntityType<? extends BattleAllayEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STATIONARY_ID, false);
    }

    public boolean isStationary() {
        return this.entityData.get(DATA_STATIONARY_ID);
    }

    public void setStationary(boolean stationary) {
        this.entityData.set(DATA_STATIONARY_ID, stationary);
    }


    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this));

        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false,
                (target, level) -> !(target instanceof BattleAllayEntity)));
    }


    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (this.ownerUuid != null && player.getUUID().equals(this.ownerUuid)) {
            if (stack.getItem() instanceof HornItem) {

                if (!this.level().isClientSide()) {
                    boolean newState = !this.isStationary();
                    this.setStationary(newState);

                    String message = newState ? "Battle Allay is now Stationary." : "Battle Allay is Following.";
                    player.sendOverlayMessage(Component.literal(message));

                    this.setTarget(null);
                }

                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isStationary() && this.getTarget() == null && !this.level().isClientSide()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.5, 0.5, 0.5));
        }
    }


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
        output.putBoolean("IsStationary", this.isStationary());
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
        this.setStationary(input.getBooleanOr("IsStationary", false));
    }

    class FollowOwnerGoal extends Goal {
        private final BattleAllayEntity allay;

        public FollowOwnerGoal(BattleAllayEntity allay) {
            this.allay = allay;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.allay.isStationary() || this.allay.getTarget() != null) return false;

            Player owner = this.allay.getPlayerOwner();
            return owner != null && this.allay.distanceToSqr(owner) > 25.0;
        }

        @Override
        public void tick() {
            Player owner = this.allay.getPlayerOwner();
            if (owner != null) {
                this.allay.getMoveControl().setWantedPosition(
                        owner.getX(),
                        owner.getY() + 1.5,
                        owner.getZ(),
                        1.0D
                );
            }
        }
    }
}