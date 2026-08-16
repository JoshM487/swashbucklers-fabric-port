package hal.studios.hpm.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

final class HpmShipPhysics {
    private HpmShipPhysics() {}

    /**
     * Recreates the original Swashbucklers buoyancy procedure.
     *
     * The 1.21.4 NeoForge entities were custom mobs, not vanilla Boat entities. Their
     * tick procedures sampled a point slightly above the entity origin and forced a
     * small upward velocity while that point was in water. Keeping that behavior here
     * prevents the 26.1.2 compatibility boats from falling through the water column.
     */
    static void applyOriginalBuoyancy(Entity entity, double probeYOffset, double liftVelocity) {
        if (!isWaterAt(entity, probeYOffset)) {
            return;
        }

        Vec3 velocity = entity.getDeltaMovement();
        entity.setDeltaMovement(velocity.x, liftVelocity, velocity.z);
        entity.fallDistance = 0.0F;
    }

    static boolean isWaterAt(Entity entity, double probeYOffset) {
        BlockPos probe = BlockPos.containing(entity.getX(), entity.getY() + probeYOffset, entity.getZ());
        return entity.level().getFluidState(probe).is(FluidTags.WATER);
    }
}
