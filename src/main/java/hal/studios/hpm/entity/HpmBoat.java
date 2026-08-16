package hal.studios.hpm.entity;

import java.util.function.Supplier;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class HpmBoat extends Boat implements HpmControllableShip {
    private final double buoyancyProbeYOffset;
    private final double buoyancyLiftVelocity;
    private final HpmShipControlState control;

    public HpmBoat(EntityType<? extends Boat> type, Level level, Supplier<Item> dropItem,
            double buoyancyProbeYOffset, double buoyancyLiftVelocity,
            double maxSpeed, float turnDegreesPerTick, double passengerAttachmentY) {
        super(type, level, dropItem);
        this.buoyancyProbeYOffset = buoyancyProbeYOffset;
        this.buoyancyLiftVelocity = buoyancyLiftVelocity;
        this.control = new HpmShipControlState(maxSpeed, turnDegreesPerTick, passengerAttachmentY);
    }

    @Override
    public void tick() {
        super.tick();
        HpmShipPhysics.applyOriginalBuoyancy(this, this.buoyancyProbeYOffset, this.buoyancyLiftVelocity);
        this.control.tickOriginalControls(this);
    }

    /**
     * Capture the normal movement key states, but deliberately do not pass them to
     * Boat. Vanilla Boat would immediately row/turn; Swashbucklers instead used the
     * keys to alter persistent sail percentage and ship yaw.
     */
    @Override
    public void setInput(boolean left, boolean right, boolean forward, boolean back) {
        this.control.setInput(forward, back, left, right);
    }

    /** Original PathfinderMob ships carried one centered pilot, not boat seat offsets. */
    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale) {
        return new Vec3(0.0D, this.control.passengerAttachmentY(), 0.0D);
    }

    /** PathfinderMob did not clamp a rider's camera yaw to the vehicle like Boat does. */
    @Override
    protected void clampRotation(Entity passenger) {
        // Intentionally no-op: preserve the original free-look mounting behaviour.
    }

    /**
     * The original primary ship entities inherited generic Entity dismounting rather
     * than Boat's shore/side search. Generic vehicles dismount from the vehicle's top
     * centre, so keep the player with the ship instead of ejecting them to a boat side.
     */
    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        return new Vec3(this.getX(), this.getBoundingBox().maxY, this.getZ());
    }

    @Override
    protected int getMaxPassengers() {
        return 1;
    }

    @Override
    public void hpm$setOriginalInput(boolean sailUp, boolean sailDown, boolean turnLeft, boolean turnRight) {
        this.control.setInput(sailUp, sailDown, turnLeft, turnRight);
    }

    @Override
    public float hpm$getSailSpeed() {
        return this.control.sailSpeed();
    }

    @Override
    public float hpm$getShipYaw() {
        return this.control.shipYaw();
    }
}
