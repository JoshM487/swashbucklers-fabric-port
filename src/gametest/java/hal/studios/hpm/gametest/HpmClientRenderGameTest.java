package hal.studios.hpm.gametest;

import java.util.Set;

import hal.studios.hpm.client.renderer.HpmShipRenderer;
import hal.studios.hpm.entity.HpmControllableShip;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("UnstableApiUsage")
public final class HpmClientRenderGameTest implements FabricClientGameTest {
    private static final Set<String> SHIP_IDS = Set.of("hpm:raft", "hpm:swashbuckler", "hpm:swashbucklerupgraded", "hpm:cutter", "hpm:cuttermilitarised", "hpm:cutter_pirate", "hpm:corvette_steamship");

    @Override
    public void runTest(ClientGameTestContext context) {
        HpmShipRenderer.resetCiRenderProbe();
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            TestServerContext server = singleplayer.getServer();
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            server.runCommand("fill -16 63 -4 16 63 28 minecraft:stone");
            server.runCommand("tp @a 0 65 0 0 12");
            String[] ships = {"hpm:raft","hpm:swashbuckler","hpm:swashbucklerupgraded","hpm:cutter","hpm:cuttermilitarised","hpm:cutter_pirate","hpm:corvette_steamship"};
            int[] xs = {-9,-6,-3,0,3,6,9};
            for (int i=0;i<ships.length;i++) server.runCommand("summon " + ships[i] + " " + xs[i] + " 64 18");
            context.waitTicks(20);
            singleplayer.getClientLevel().waitForChunksRender();
            context.waitFor(client -> HpmShipRenderer.ciHasRenderedAll(SHIP_IDS), 30 * 20);
            context.takeScreenshot("swashbucklers-seven-ships-visible");
            System.out.println("SWASHBUCKLERS_CLIENT_RENDER_TEST_OK " + HpmShipRenderer.ciRenderedIds());

            server.runCommand("kill @e[type=#minecraft:boat]");
            server.runCommand("fill -50 62 -50 50 62 50 minecraft:stone");
            server.runCommand("fill -50 63 -50 50 64 50 minecraft:water");
            server.runCommand("tp @a 0 65 0 90 0");
            server.runCommand("summon hpm:raft 0 64 4 {Rotation:[0f,0f],Tags:[\"hpm_ci_control\"]}");
            context.waitTicks(10);
            server.runCommand("ride @p mount @e[type=hpm:raft,tag=hpm_ci_control,limit=1]");
            context.waitTicks(10);

            Vec3 start = context.computeOnClient(client -> {
                if (client.player == null || !(client.player.getVehicle() instanceof AbstractBoat vehicle)) throw new AssertionError("Player could not mount hpm:raft");
                if (!(vehicle instanceof HpmControllableShip)) throw new AssertionError("Raft is not using Swashbucklers control path");
                double horizontal = Math.hypot(client.player.getX()-vehicle.getX(), client.player.getZ()-vehicle.getZ());
                if (horizontal > 0.25D) throw new AssertionError("Pilot not centered: " + horizontal);
                return vehicle.position();
            });

            // Regression check for the user's exact case: A/D must steer at 0% sail.
            float zeroSail = sailSpeed(context);
            if (Math.abs(zeroSail) > 0.01F) throw new AssertionError("Expected 0% sail before steering test, got " + zeroSail);
            float yawSailsDownBefore = entityYaw(context);
            context.getInput().holdKeyFor(options -> options.keyLeft, 5);
            context.waitTicks(8);
            float yawSailsDownAfter = entityYaw(context);
            float sailsDownTurn = angleDiff(yawSailsDownBefore, yawSailsDownAfter);
            if (sailsDownTurn < 6.0F) throw new AssertionError("Ship cannot steer with sails down: " + sailsDownTurn);
            if (Math.abs(sailSpeed(context)) > 0.01F) throw new AssertionError("Steering at sails-down unexpectedly changed throttle");

            // Controls are intentionally flipped from the prior build: S raises sail/throttle.
            context.getInput().holdKeyFor(options -> options.keyDown, 30);
            context.waitTicks(8);
            Vec3 afterS = vehiclePosition(context);
            double moved = horizontalDistance(start, afterS);
            float raisedSail = sailSpeed(context);
            if (raisedSail < 20.0F) throw new AssertionError("S did not raise sail throttle: " + raisedSail);
            if (moved < 0.10D) throw new AssertionError("Raft did not respond physically when S raised sail: " + moved);

            Vec3 coastStart = afterS;
            context.waitTicks(20);
            Vec3 afterRelease = vehiclePosition(context);
            double coast = horizontalDistance(coastStart, afterRelease);
            if (coast < 0.05D) throw new AssertionError("Ship did not preserve any sail movement after S release: " + coast);

            float yawBefore = entityYaw(context);
            context.getInput().holdKeyFor(options -> options.keyLeft, 5);
            context.waitTicks(8);
            float yawAfter = entityYaw(context);
            float turn = angleDiff(yawBefore,yawAfter);
            if (turn < 6.0F) throw new AssertionError("Ship did not turn with original steering: " + turn);

            // W must now lower the already-raised sail throttle.
            float beforeW = sailSpeed(context);
            context.getInput().holdKeyFor(options -> options.keyUp, 15);
            context.waitTicks(4);
            float afterW = sailSpeed(context);
            if (afterW >= beforeW - 8.0F) throw new AssertionError("W did not lower sail throttle: before=" + beforeW + " after=" + afterW);

            context.getInput().holdKeyFor(options -> options.keyShift, 2);
            context.waitTicks(5);
            context.computeOnClient(client -> {
                if (client.player == null || client.player.getVehicle()!=null) throw new AssertionError("Player did not dismount");
                return true;
            });

            // Reproduce the user's corvette seating case and enforce the lowered deck position.
            server.runCommand("kill @e[type=#minecraft:boat]");
            server.runCommand("summon hpm:corvette_steamship 0 64 4 {Rotation:[0f,0f],Tags:[\"hpm_ci_corvette_seat\"]}");
            context.waitTicks(10);
            server.runCommand("ride @p mount @e[type=hpm:corvette_steamship,tag=hpm_ci_corvette_seat,limit=1]");
            context.waitTicks(10);
            double seatDeltaY = context.computeOnClient(client -> {
                if (client.player == null || !(client.player.getVehicle() instanceof AbstractBoat vehicle)) throw new AssertionError("Player could not mount corvette");
                return client.player.getY() - vehicle.getY();
            });
            if (seatDeltaY > 1.10D) throw new AssertionError("Corvette pilot still sits too high: deltaY=" + seatDeltaY);
            if (seatDeltaY < 0.20D) throw new AssertionError("Corvette pilot was lowered into the hull/water: deltaY=" + seatDeltaY);
            context.takeScreenshot("swashbucklers-lowered-corvette-seat");

            System.out.println("SWASHBUCKLERS_SAILS_DOWN_STEERING_OK turn="+sailsDownTurn+" sail="+zeroSail);
            System.out.println("SWASHBUCKLERS_FLIPPED_CONTROLS_OK moved="+moved+" coast="+coast+" turn="+turn+" raisedSail="+raisedSail+" afterW="+afterW);
            System.out.println("SWASHBUCKLERS_CORVETTE_SEAT_OK deltaY="+seatDeltaY);
        }
    }

    private static Vec3 vehiclePosition(ClientGameTestContext context) {
        return context.computeOnClient(client -> {
            if (client.player==null || client.player.getVehicle()==null) throw new AssertionError("Unexpected dismount");
            return client.player.getVehicle().position();
        });
    }

    private static float entityYaw(ClientGameTestContext context) {
        return context.computeOnClient(client -> {
            Entity vehicle = client.player == null ? null : client.player.getVehicle();
            if (!(vehicle instanceof AbstractBoat)) throw new AssertionError("No ship vehicle");
            return vehicle.getYRot();
        });
    }

    private static float sailSpeed(ClientGameTestContext context) {
        return context.computeOnClient(client -> {
            Entity vehicle = client.player == null ? null : client.player.getVehicle();
            if (!(vehicle instanceof HpmControllableShip ship)) throw new AssertionError("No controllable ship");
            return ship.hpm$getSailSpeed();
        });
    }

    private static double horizontalDistance(Vec3 a, Vec3 b) { return Math.hypot(b.x-a.x,b.z-a.z); }
    private static float angleDiff(float a,float b) { float d=Math.abs(a-b)%360.0F; return d>180.0F?360.0F-d:d; }
}
