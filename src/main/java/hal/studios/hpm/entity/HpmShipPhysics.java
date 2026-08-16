package hal.studios.hpm.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

final class HpmShipPhysics {
    private HpmShipPhysics() {}

    static void applyOriginalBuoyancy(Entity entity, double probeYOffset, double liftVelocity) {
        if (!isWaterAt(entity, probeYOffset)) return;
        Vec3 velocity = entity.getDeltaMovement();
        entity.setDeltaMovement(velocity.x, liftVelocity, velocity.z);
        entity.fallDistance = 0.0F;
    }

    static boolean isWaterAt(Entity entity, double probeYOffset) {
        BlockPos probe = BlockPos.containing(entity.getX(), entity.getY() + probeYOffset, entity.getZ());
        return entity.level().getFluidState(probe).is(FluidTags.WATER);
    }
}
