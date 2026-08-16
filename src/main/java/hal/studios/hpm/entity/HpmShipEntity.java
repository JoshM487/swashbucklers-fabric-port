package hal.studios.hpm.entity;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class HpmShipEntity extends PathfinderMob implements HpmControllableShip {
    private final HpmShipSpec spec;
    private final HpmShipControlState control;

    public HpmShipEntity(EntityType<? extends HpmShipEntity> type, Level level, HpmShipSpec spec) {
        super(type, level);
        this.spec = spec;
        this.control = new HpmShipControlState(spec);
        this.setPersistenceRequired();
        this.setNoAi(true);
    }

    public static AttributeSupplier.Builder createShipAttributes(HpmShipSpec spec) {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, spec.movementSpeed())
                .add(Attributes.MAX_HEALTH, spec.maxHealth())
                .add(Attributes.ARMOR, 0.0D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    protected void registerGoals() {
        // Original ships had no autonomous navigation goals.
    }

    @Override
    public void baseTick() {
        super.baseTick();
        HpmShipPhysics.applyOriginalBuoyancy(this, this.spec.buoyancyProbeYOffset(), this.spec.buoyancyLiftVelocity());
        if (!this.level().isClientSide()) this.control.tick(this);
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale) {
        return super.getPassengerAttachmentPoint(passenger, dimensions, scale)
                .add(0.0D, this.spec.passengerYOffset(), 0.0D);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player.isSecondaryUseActive() && this.spec.cargo()) {
            // Preserve the original distinction: shift-use is cargo interaction and must
            // never mount the pilot. Full legacy cargo menus are a separate parity system.
            return InteractionResult.SUCCESS;
        }
        super.mobInteract(player, hand);
        player.startRiding(this);
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public boolean canCollideWith(Entity other) {
        return true;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().isEmpty();
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isPassenger()) {
            super.travel(Vec3.ZERO);
        } else {
            super.travel(travelVector);
        }
    }

    @Override
    public void hpm$setOriginalInput(boolean sailUp, boolean sailDown, boolean turnLeft, boolean turnRight) {
        this.control.setInput(this, sailUp, sailDown, turnLeft, turnRight);
    }

    @Override
    public float hpm$getSailSpeed() { return this.control.sailSpeed(); }

    @Override
    public float hpm$getShipYaw() { return this.control.shipYaw(); }
}
