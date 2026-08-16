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

        /*
         * Vanilla vehicle movement is controlled by the riding client and then sent to
         * the logical server. Keep the recreated Swashbucklers sail/yaw state on that
         * same authority path. If the server independently ran its copy with no key
         * state it would continually overwrite the client's ship yaw and cause
         * rubber-banding in multiplayer.
         */
        if (!ship.level().isClientSide()) {
            return;
        }

        if (!this.hadPilot) {
            // The original right-click procedure copied the ship's existing YRot into
            // the rider's shipYaw variable. Boarding never rotated the hull to camera.
            this.shipYaw = wrapDegrees(ship.getYRot());
            this.sailSpeed = 0.0F;
            this.hadPilot = true;
        }

        // Original controls changed persistent sail percentage one point per tick.
        if (this.sailUp && this.sailSpeed < 100.0F) {
            this.sailSpeed = Math.min(100.0F, this.sailSpeed + 1.0F);
        }
        if (this.sailDown && this.sailSpeed > -40.0F) {
            this.sailSpeed = Math.max(-40.0F, this.sailSpeed - 1.0F);
        }

        // Original steering only operated while moving ahead.
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

        // Use the same direct fluid probe as the recovered original buoyancy procedure.
        if (!HpmShipPhysics.isWaterAt(ship, 0.0D)) {
            return;
        }

        Vec3 current = ship.getDeltaMovement();

        // Original SailspeedProcedure reset a significant throttle when physically
        // blocked and both horizontal velocity components were exactly zero.
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
