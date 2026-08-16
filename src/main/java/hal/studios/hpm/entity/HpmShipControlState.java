package hal.studios.hpm.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

final class HpmShipControlState {
    private static final double WORLD_SHIP_SPEED = 0.8D;

    private final HpmShipSpec spec;
    private boolean sailUp;
    private boolean sailDown;
    private boolean turnLeft;
    private boolean turnRight;
    private long lastInputTick = Long.MIN_VALUE;
    private float sailSpeed;
    private float shipYaw;
    private boolean hadPilot;

    HpmShipControlState(HpmShipSpec spec) {
        this.spec = spec;
    }

    void setInput(HpmShipEntity ship, boolean sailUp, boolean sailDown, boolean turnLeft, boolean turnRight) {
        this.sailUp = sailUp;
        this.sailDown = sailDown;
        this.turnLeft = turnLeft;
        this.turnRight = turnRight;
        this.lastInputTick = ship.level().getGameTime();
    }

    void tick(HpmShipEntity ship) {
        Entity pilot = ship.getFirstPassenger();
        if (pilot == null) {
            this.sailUp = this.sailDown = this.turnLeft = this.turnRight = false;
            this.hadPilot = false;
            return;
        }

        if (!this.hadPilot) {
            this.shipYaw = wrapDegrees(ship.getYRot());
            this.hadPilot = true;
        }

        long now = ship.level().getGameTime();
        if (now - this.lastInputTick > 3L) {
            this.sailUp = this.sailDown = this.turnLeft = this.turnRight = false;
        }

        if (this.sailUp && this.sailSpeed < 100.0F) this.sailSpeed = Math.min(100.0F, this.sailSpeed + 1.0F);
        if (this.sailDown && this.sailSpeed > -40.0F) this.sailSpeed = Math.max(-40.0F, this.sailSpeed - 1.0F);

        if (this.sailSpeed > 0.1F) {
            if (this.turnLeft) this.shipYaw -= this.spec.turnDegreesPerTick();
            if (this.turnRight) this.shipYaw += this.spec.turnDegreesPerTick();
            this.shipYaw = wrapDegrees(this.shipYaw);
        }

        ship.setYRot(this.shipYaw);
        ship.setYBodyRot(this.shipYaw);
        ship.setYHeadRot(this.shipYaw);

        Vec3 current = ship.getDeltaMovement();
        if (current.x == 0.0D && current.z == 0.0D && (this.sailSpeed > 8.0F || this.sailSpeed < -5.0F)) {
            this.sailSpeed = 0.0F;
        }

        if (HpmShipPhysics.isWaterAt(ship, 0.0D)) {
            double scalar = WORLD_SHIP_SPEED * this.spec.maxSpeed() * (this.sailSpeed / 100.0D);
            Vec3 facing = ship.getLookAngle();
            ship.setDeltaMovement(scalar * facing.x, current.y, scalar * facing.z);
        }
    }

    float sailSpeed() { return this.sailSpeed; }
    float shipYaw() { return this.shipYaw; }

    private static float wrapDegrees(float degrees) {
        while (degrees > 360.0F) degrees -= 360.0F;
        while (degrees < 0.0F) degrees += 360.0F;
        return degrees;
    }
}
