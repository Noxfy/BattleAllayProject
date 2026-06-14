package com.example;

import com.example.entities.BattleAllayEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.List;

public class HornItem extends InstrumentItem {

    public HornItem(Properties properties) {
        super(properties);
    }

    public static List<BattleAllayEntity> getAllAllaysInRange(Player player, double radius) {
        AABB searchBox = player.getBoundingBox().inflate(radius);

        // This returns the entire collection (list) of found mobs
        return player.level().getEntitiesOfClass(BattleAllayEntity.class, searchBox, (allay) -> true);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {

            List<BattleAllayEntity> nearbyAllays = getAllAllaysInRange(player, 16.0);

            if (!nearbyAllays.isEmpty()) {

                // THE CHANGE: Filter this stream so it only looks at Allays you own before finding the closest one
                BattleAllayEntity closestAllay = nearbyAllays.stream()
                        .filter(allay -> {
                            Player owner = allay.getPlayerOwner();
                            return owner != null && player.getUUID().equals(owner.getUUID());
                        })
                        .min(Comparator.comparingDouble(allay -> allay.distanceToSqr(player)))
                        .orElse(null); // Returns null if no owned allays are nearby

                // Safety check: Only run the rest of your code if an owned Allay was actually found
                if (closestAllay != null) {
                    boolean targetState = !closestAllay.isStationary();

                    nearbyAllays.forEach(allay -> {
                        Player owner = allay.getPlayerOwner();
                        if (owner != null && player.getUUID().equals(owner.getUUID())) {
                            allay.setStationary(targetState);
                        }
                    });

                    return InteractionResult.SUCCESS;
                }
            }
        }

        return super.use(level, player, hand);
    }
}