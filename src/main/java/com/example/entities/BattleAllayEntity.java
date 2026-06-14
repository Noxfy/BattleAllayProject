package com.example.entities;

import com.example.HornItem;
import com.example.StaffItem;
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
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.wolf.Wolf;

import java.util.EnumSet;
import java.util.Objects;
import java.util.UUID;
import java.util.Random;

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

        this.goalSelector.addGoal(2, new OwnerHurtTargetGoal(this));

        this.goalSelector.addGoal(4, new AllayChargeAttackGoal(this));

        this.goalSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
    }


    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (this.ownerUuid != null && player.getUUID().equals(this.ownerUuid)) {
            if (stack.getItem() instanceof StaffItem) {

                if (!this.level().isClientSide()) {
                    boolean newState = !this.isStationary();
                    this.setStationary(newState);

                    this.setTarget(null);
                }

                return InteractionResult.SUCCESS;
            } else if (stack.has(DataComponents.ATTRIBUTE_MODIFIERS) && this.getItemInHand(hand).isEmpty() && stack.count() == 1) {
                player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                this.setItemSlot(EquipmentSlot.MAINHAND, stack);
                return InteractionResult.SUCCESS;
            } else if (player.getItemInHand(hand).isEmpty() && !this.getItemInHand(hand).isEmpty()) {
                ItemStack itemInHand = this.getItemInHand(hand);
                this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                player.addItem(itemInHand);
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
            Random random = new Random();
            int numberX = random.nextInt(11) - 5;
            int numberZ = random.nextInt(11) - 5;
            Player owner = this.allay.getPlayerOwner();
            if (owner != null) {
                this.allay.getMoveControl().setWantedPosition(
                        owner.getX() + numberX,
                        owner.getY() + 1.5,
                        owner.getZ() + numberZ,
                        1.0D
                );
            }
        }
    }

    private class AllayChargeAttackGoal extends Goal {
        private final BattleAllayEntity allay;

        public AllayChargeAttackGoal(BattleAllayEntity allay) {
            this.allay = allay;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.allay.getTarget();
            if (target != null && target.isAlive() && !this.allay.getMoveControl().hasWanted() && this.allay.getRandom().nextInt(reducedTickDelay(7)) == 0) {
                return this.allay.distanceToSqr(target) > (double) 4.0F;
            } else {
                return false;
            }
        }

        @Override
        public boolean canContinueToUse() {
            return this.allay.getMoveControl().hasWanted() && this.allay.isCharging() && this.allay.getTarget() != null && this.allay.getTarget().isAlive();
        }

        @Override
        public void start() {
            LivingEntity attackTarget = this.allay.getTarget();
            if (attackTarget != null) {
                Vec3 eyePosition = attackTarget.getEyePosition();
                this.allay.getMoveControl().setWantedPosition(eyePosition.x, eyePosition.y, eyePosition.z, (double) 1.0F);
            }

            this.allay.setIsCharging(true);
            this.allay.playSound(SoundEvents.VEX_CHARGE, 1.0F, 1.0F);
        }

        @Override
        public void stop() {
            this.allay.setIsCharging(false);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity attackTarget = this.allay.getTarget();
            if (attackTarget != null) {
                if (this.allay.getBoundingBox().intersects(attackTarget.getBoundingBox())) {
                    this.allay.doHurtTarget(getServerLevel(this.allay.level()), attackTarget);
                    this.allay.setIsCharging(false);
                } else {
                    double distance = this.allay.distanceToSqr(attackTarget);
                    if (distance < (double) 9.0F) {
                        Vec3 eyePosition = attackTarget.getEyePosition();
                        this.allay.getMoveControl().setWantedPosition(eyePosition.x, eyePosition.y, eyePosition.z, (double) 1.0F);
                    }
                }
            }
        }
    }
    class OwnerHurtByTargetGoal extends TargetGoal {
        private final BattleAllayEntity allay;
        private LivingEntity ownerLastHurtBy;

        public OwnerHurtByTargetGoal(BattleAllayEntity allay) {
            super(allay, false);
            this.allay = allay;
            this.setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            Player owner = this.allay.getPlayerOwner();
            if (!this.allay.isStationary()) {
                if (owner == null) {
                    return false;
                } else {
                    this.ownerLastHurtBy = owner.getLastHurtByMob();
                    return this.ownerLastHurtBy != null;
                }
            } else {
                return false;
            }
        }

        @Override
        public void start() {
            if (this.ownerLastHurtBy != null) {
                this.allay.setTarget(ownerLastHurtBy);
                super.start();
            }
        }
    }

    class OwnerHurtTargetGoal extends TargetGoal {
        private final BattleAllayEntity allay;
        private LivingEntity ownerLastHurt;

        public OwnerHurtTargetGoal(BattleAllayEntity allay) {
            super(allay, false);
            this.allay = allay;
            this.setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            Player owner = this.allay.getPlayerOwner();
            if (!this.allay.isStationary()) {
                if (owner == null) {
                    return false;
                } else {
                    this.ownerLastHurt = owner.getLastHurtMob();
                    return this.ownerLastHurt != null;
                }
            } else {
                return false;
            }
        }

        @Override
        public void start() {
            if (this.ownerLastHurt != null) {
                this.allay.setTarget(ownerLastHurt);
                super.start();
            }
        }
    }

}

