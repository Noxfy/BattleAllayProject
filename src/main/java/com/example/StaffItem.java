package com.example;

import com.example.entities.BattleAllayEntity;
import com.example.entities.ModEntities;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.Optional;

public class StaffItem extends Item {
    public StaffItem(Properties properties) {
        super(properties);
    }

    public static Optional<Allay> getClosestAllay(Player player, double radius) {
        // 1. Create a search area around the player
        AABB searchBox = player.getBoundingBox().inflate(radius);

        // 2. Query the level for all Allay entities
        return player.level().getEntitiesOfClass(Allay.class, searchBox, (allay) -> true)
                .stream()
                // 3. Find the minimum based on distance to the player
                .min(Comparator.comparingDouble(allay -> allay.distanceToSqr(player)));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        // 1. Only run logic on the server side to prevent ghost entities
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {

            //RANGE OF 6 BLOCKS
            getClosestAllay(player, 6.0).ifPresentOrElse(allay -> {
                // Logic for when an Allay is found (e.g., allay.setPos(...))

                //summon new battle allay
                BattleAllayEntity battleAllay = new BattleAllayEntity(ModEntities.BATTLE_ALLAY, level);

                if (battleAllay != null) {
                    battleAllay.setPos(allay.getX(), allay.getY(), allay.getZ());

                    battleAllay.setPlayerOwner(player);

                    serverLevel.addFreshEntity(battleAllay);

                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.EVOKER_PREPARE_WOLOLO, SoundSource.PLAYERS, 1.0F, 1.0F);
                }

                //kill old allay
                allay.kill(serverLevel);
            }, () -> {
                // Logic for when no Allay is nearby
                player.sendOverlayMessage(Component.literal("No Allays found nearby."));
            });
        }

        // Returns visual success/swing animation on the client, and true data success on the server
        return super.use(level, player, hand);
    }

}
