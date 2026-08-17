package hal.studios.hpm.entity;

import java.util.function.Supplier;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class HpmChestBoat extends ChestBoat implements HpmControllableShip {
    private final double buoyancyProbeYOffset;
    private final double buoyancyLiftVelocity;
    private final HpmShipControlState control;
    private boolean runningVanillaBoatTick;

    public HpmChestBoat(EntityType<? extends ChestBoat> type, Level level, Supplier<Item> dropItem,
            double buoyancyProbeYOffset, double buoyancyLiftVelocity,
            double maxSpeed, float turnDegreesPerTick, double passengerAttachmentY) {
        super(type, level, dropItem);
        this.buoyancyProbeYOffset = buoyancyProbeYOffset;
        this.buoyancyLiftVelocity = buoyancyLiftVelocity;
        this.control = new HpmShipControlState(maxSpeed, turnDegreesPerTick, passengerAttachmentY);
    }

    @Override
    public void tick() {
        this.runningVanillaBoatTick = true;
        try { super.tick(); } finally { this.runningVanillaBoatTick = false; }
        HpmShipPhysics.applyOriginalBuoyancy(this, this.buoyancyProbeYOffset, this.buoyancyLiftVelocity);
        this.control.tickOriginalControls(this);
    }

    @Override
    public void ejectPassengers() {
        if (this.runningVanillaBoatTick && this.getControllingPassenger() != null) return;
        super.ejectPassengers();
    }

    @Override
    public void setInput(boolean left, boolean right, boolean forward, boolean back) {
        this.control.setInput(forward, back, left, right);
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale) {
        return new Vec3(0.0D, this.control.passengerAttachmentY(), 0.0D);
    }

    @Override
    protected void clampRotation(Entity passenger) {}

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        return new Vec3(this.getX(), this.getBoundingBox().maxY, this.getZ());
    }

    @Override
    protected int getMaxPassengers() { return 1; }

    @Override
    public void hpm$setOriginalInput(boolean sailUp, boolean sailDown, boolean turnLeft, boolean turnRight) {
        this.control.setInput(sailUp, sailDown, turnLeft, turnRight);
    }

    @Override public float hpm$getSailSpeed() { return this.control.sailSpeed(); }
    @Override public float hpm$getShipYaw() { return this.control.shipYaw(); }
}
