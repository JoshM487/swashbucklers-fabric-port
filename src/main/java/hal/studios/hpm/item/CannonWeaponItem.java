package hal.studios.hpm.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class CannonWeaponItem extends Item {
    private final Item ammo;
    private final int explosionPower;

    public CannonWeaponItem(Properties properties, Item ammo, int explosionPower) {
        super(properties);
        this.ammo = ammo;
        this.explosionPower = explosionPower;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack weapon = player.getItemInHand(hand);
        InteractionHand ammoHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack ammunition = player.getItemInHand(ammoHand);
        if (ammunition.getItem() != ammo) return InteractionResult.PASS;

        if (!level.isClientSide()) {
            LargeFireball projectile = new LargeFireball(level, player, player.getLookAngle(), explosionPower);
            level.addFreshEntity(projectile);
            ammunition.consume(1, player);
            weapon.hurtAndBreak(1, player, hand);
        }
        return InteractionResult.SUCCESS;
    }
}
