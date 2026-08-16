package hal.studios.hpm.item;

import java.util.function.Supplier;

import hal.studios.hpm.entity.HpmShipEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class HpmShipItem extends Item {
    private final Supplier<EntityType<HpmShipEntity>> shipType;

    public HpmShipItem(Properties properties, Supplier<EntityType<HpmShipEntity>> shipType) {
        super(properties);
        this.shipType = shipType;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        HpmShipEntity ship = this.shipType.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        if (ship == null) return InteractionResult.FAIL;

        ship.setPos(player.getX(), player.getY(), player.getZ());
        ship.setYRot(player.getYRot());
        ship.setYBodyRot(player.getYRot());
        ship.setYHeadRot(player.getYRot());
        ship.setDeltaMovement(0.0D, 0.0D, 0.0D);
        level.addFreshEntity(ship);

        if (!player.isCreative()) {
            ItemStack stack = player.getItemInHand(hand);
            stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
