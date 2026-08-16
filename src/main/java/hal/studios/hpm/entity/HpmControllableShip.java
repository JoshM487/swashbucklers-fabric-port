package hal.studios.hpm.entity;

/**
 * Marker/control contract for the compatibility ship entities.
 *
 * Vanilla boats treat W/S/A/D as direct rowing controls. Swashbucklers did not:
 * W/S changed a persistent sail percentage and A/D changed the ship's own yaw.
 * The concrete boat wrappers capture Minecraft's local vehicle input through this
 * contract and feed it into the recreated original movement procedure.
 */
public interface HpmControllableShip {
    void hpm$setOriginalInput(boolean sailUp, boolean sailDown, boolean turnLeft, boolean turnRight);

    float hpm$getSailSpeed();

    float hpm$getShipYaw();
}
