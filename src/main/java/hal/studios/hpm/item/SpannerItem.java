package hal.studios.hpm.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class SpannerItem extends Item {
    public SpannerItem(Properties properties) { super(properties); }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        Entity vehicle = player.getVehicle();
        if (!(vehicle instanceof VehicleEntity ship)) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            ship.setDamage(0.0F);
            ship.setHurtTime(0);
            ItemStack spanner = player.getItemInHand(hand);
            spanner.hurtAndBreak(1, player, hand);
        }
        return InteractionResult.SUCCESS;
    }
}
