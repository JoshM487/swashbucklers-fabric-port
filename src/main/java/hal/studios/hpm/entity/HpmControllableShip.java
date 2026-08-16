package hal.studios.hpm.entity;

public interface HpmControllableShip {
    void hpm$setOriginalInput(boolean sailUp, boolean sailDown, boolean turnLeft, boolean turnRight);
    float hpm$getSailSpeed();
    float hpm$getShipYaw();
}
