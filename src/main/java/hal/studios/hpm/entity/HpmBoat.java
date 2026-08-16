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
    private boolean runningVanillaBoatTick;

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
        /*
         * AbstractBoat contains a vanilla-only safety that ejects passengers after the
         * hull has been considered underwater for roughly 60 ticks. The original
         * Swashbucklers ships were PathfinderMob vehicles and never had that behavior.
         * Mark only the call into vanilla Boat.tick so ejectPassengers can distinguish
         * that internal auto-eject from a real external ejection/removal.
         */
        this.runningVanillaBoatTick = true;
        try {
            super.tick();
        } finally {
            this.runningVanillaBoatTick = false;
        }

        HpmShipPhysics.applyOriginalBuoyancy(this, this.buoyancyProbeYOffset, this.buoyancyLiftVelocity);
        this.control.tickOriginalControls(this);
    }

    @Override
    public void ejectPassengers() {
        if (this.runningVanillaBoatTick && this.getControllingPassenger() != null) {
            return;
        }
        super.ejectPassengers();
    }

    /**
     * Capture normal movement key states but deliberately do not pass them to Boat.
     * Swashbucklers used W/S for persistent sail percentage and A/D for ship yaw,
     * rather than vanilla rowing.
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
     * Original ships inherited generic Entity dismounting rather than Boat's
     * side/shore search, so put the rider at the ship's top centre.
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
