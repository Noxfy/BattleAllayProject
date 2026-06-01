package com.example;

import com.example.entities.BattleAllayEntity;
import com.example.entities.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class StaffItem extends Item {
    public StaffItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        // 1. Only run logic on the server side to prevent ghost entities
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {

            // 2. Instantiate your entity
            // NOTE: Replace 'YOUR_ENTITY_TYPE' with your actual registered EntityType entry
            BattleAllayEntity allay = new BattleAllayEntity(ModEntities.BATTLE_ALLAY, level);

            if (allay != null) {
                // 3. Position the Allay slightly above and in front of the player
                allay.setPos(player.getX(), player.getY() + 1.0, player.getZ());

                // 4. HERE IS THE MAGIC: Set the player who used the item as the owner
                allay.setPlayerOwner(player);

                // 5. Spawn the entity into the world
                serverLevel.addFreshEntity(allay);

                // 6. Optional: Play a nice cosmetic sound effect at the spawn location
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM, SoundSource.PLAYERS, 1.0F, 1.0F);

                // 7. Consume 1 item from the stack if the player isn't in Creative Mode
//                if (!player.getAbilities().instabuild) {
//                    itemStack.shrink(1);
//                }
            }
        }

        // Returns visual success/swing animation on the client, and true data success on the server
        return super.use(level, player, hand);
    }

}
