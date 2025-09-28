package com.burningsulphur.delights_now_loading.items;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class StewSpawner extends Item {
    private static final int MAX_USES = 4;        // like durability
    private static final int TICK_INTERVAL = 20 * 30; // 30 seconds between recharge

    public StewSpawner(Properties properties) {
        super(properties
                .durability(MAX_USES) // makes it behave like a tool with durability
                .food(new FoodProperties.Builder()
                        .nutrition(4) // hunger restored
                        .saturationMod(0.3f) // saturation restored
                        .build()));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity) {
        if (!world.isClientSide && entity instanceof Player player) {
            // Damage the item by 1 use when eaten
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(p.getUsedItemHand()));
        }
        return super.finishUsingItem(stack, world, entity);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        if (!world.isClientSide && entity instanceof Player) {
            // Add a custom tag to track recharge timer
            CompoundTag tag = stack.getOrCreateTag();
            int timer = tag.getInt("RechargeTimer");

            if (stack.getDamageValue() > 0) { // only recharge if damaged
                if (timer >= TICK_INTERVAL) {
                    stack.setDamageValue(stack.getDamageValue() - 1); // repair by 1
                    tag.putInt("RechargeTimer", 0);
                } else {
                    tag.putInt("RechargeTimer", timer + 1);
                }
            }
        }
        super.inventoryTick(stack, world, entity, slot, selected);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x824A9A;
    }
}
