package hal.studios.hpm.entity;

public record HpmShipSpec(
        double buoyancyProbeYOffset,
        double buoyancyLiftVelocity,
        double maxSpeed,
        float turnDegreesPerTick,
        double passengerYOffset,
        double maxHealth,
        double movementSpeed,
        boolean cargo) {
}
