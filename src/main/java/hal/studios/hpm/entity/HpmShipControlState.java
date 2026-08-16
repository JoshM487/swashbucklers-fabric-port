package hal.studios.hpm.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.Vec3;

final class HpmShipControlState {
    private static final double ORIGINAL_WORLD_SPEED_MULTIPLIER = 0.8D;

    private final double maxSpeed;
    private final float turnDegreesPerTick;
    private final double passengerAttachmentY;

    private boolean sailUp;
    private boolean sailDown;
    private boolean turnLeft;
    private boolean turnRight;
    private boolean hadPilot;
    private float sailSpeed;
    private float shipYaw;

    HpmShipControlState(double maxSpeed, float turnDegreesPerTick, double passengerAttachmentY) {
        this.maxSpeed = maxSpeed;
        this.turnDegreesPerTick = turnDegreesPerTick;
        this.passengerAttachmentY = passengerAttachmentY;
    }

    void setInput(boolean sailUp, boolean sailDown, boolean turnLeft, boolean turnRight) {
        this.sailUp = sailUp;
        this.sailDown = sailDown;
        this.turnLeft = turnLeft;
        this.turnRight = turnRight;
    }

    void tickOriginalControls(AbstractBoat ship) {
        Entity pilot = ship.getControllingPassenger();
        if (pilot == null) {
            resetWhenUnpiloted(ship);
            return;
        }

        if (!this.hadPilot) {
            // The original right-click procedure copied the ship's current yaw into the
            // pilot's shipYaw variable. Keep the ship orientation it had when boarded.
            this.shipYaw = wrapDegrees(ship.getYRot());
            this.sailSpeed = 0.0F;
            this.hadPilot = true;
        }

        // Player variables in the original mod changed by exactly one percentage point
        // per tick, clamped to 100% ahead and -40% astern.
        if (this.sailUp && this.sailSpeed < 100.0F) {
            this.sailSpeed = Math.min(100.0F, this.sailSpeed + 1.0F);
        }
        if (this.sailDown && this.sailSpeed > -40.0F) {
            this.sailSpeed = Math.max(-40.0F, this.sailSpeed - 1.0F);
        }

        // Original steering only worked with positive sail speed. Small and medium
        // ships used ~3 degrees/tick, while the corvette used 1 degree/tick.
        if (this.sailSpeed > 0.1F) {
            if (this.turnLeft) {
                this.shipYaw -= this.turnDegreesPerTick;
            }
            if (this.turnRight) {
                this.shipYaw += this.turnDegreesPerTick;
            }
            this.shipYaw = wrapDegrees(this.shipYaw);
        }

        ship.setYRot(this.shipYaw);

        // Vanilla boats are locally vehicle-controlled. Only the controlling client
        // should generate horizontal motion; the server receives the normal vehicle
        // movement packets. Running this again on the dedicated server with no local
        // key state would fight the client and cause rubber-banding.
        if (!ship.level().isClientSide()) {
            return;
        }

        if (!ship.isInWaterOrBubble()) {
            return;
        }

        Vec3 current = ship.getDeltaMovement();

        // These two guards are copied from the original SailspeedProcedure. They reset
        // a non-trivial throttle if the ship is physically blocked and has no X/Z motion.
        if (current.x == 0.0D && current.z == 0.0D) {
            if (this.sailSpeed > 8.0F || this.sailSpeed < -5.0F) {
                this.sailSpeed = 0.0F;
            }
        }

        double scalar = ORIGINAL_WORLD_SPEED_MULTIPLIER * this.maxSpeed * (this.sailSpeed / 100.0D);
        Vec3 facing = ship.getLookAngle();
        ship.setDeltaMovement(scalar * facing.x, current.y, scalar * facing.z);
    }

    void resetWhenUnpiloted(AbstractBoat ship) {
        if (this.hadPilot) {
            this.sailSpeed = 0.0F;
            this.shipYaw = wrapDegrees(ship.getYRot());
        }
        this.hadPilot = false;
        this.sailUp = false;
        this.sailDown = false;
        this.turnLeft = false;
        this.turnRight = false;
    }

    double passengerAttachmentY() {
        return this.passengerAttachmentY;
    }

    float sailSpeed() {
        return this.sailSpeed;
    }

    float shipYaw() {
        return this.shipYaw;
    }

    private static float wrapDegrees(float degrees) {
        while (degrees > 360.0F) {
            degrees -= 360.0F;
        }
        while (degrees < 0.0F) {
            degrees += 360.0F;
        }
        return degrees;
    }
}
