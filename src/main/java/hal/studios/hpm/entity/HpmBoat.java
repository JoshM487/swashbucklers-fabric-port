package hal.studios.hpm.entity;

import java.util.function.Supplier;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public final class HpmBoat extends Boat {
    private final double buoyancyProbeYOffset;
    private final double buoyancyLiftVelocity;

    public HpmBoat(EntityType<? extends Boat> type, Level level, Supplier<Item> dropItem,
            double buoyancyProbeYOffset, double buoyancyLiftVelocity) {
        super(type, level, dropItem);
        this.buoyancyProbeYOffset = buoyancyProbeYOffset;
        this.buoyancyLiftVelocity = buoyancyLiftVelocity;
    }

    @Override
    public void tick() {
        super.tick();
        HpmShipPhysics.applyOriginalBuoyancy(this, this.buoyancyProbeYOffset, this.buoyancyLiftVelocity);
    }
}
