package hal.studios.hpm.gametest;

import java.util.Set;

import hal.studios.hpm.client.renderer.HpmShipRenderer;
import hal.studios.hpm.entity.HpmControllableShip;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("UnstableApiUsage")
public final class HpmClientRenderGameTest implements FabricClientGameTest {
    private static final Set<String> SHIP_IDS = Set.of(
        "hpm:raft",
        "hpm:swashbuckler",
        "hpm:swashbucklerupgraded",
        "hpm:cutter",
        "hpm:cuttermilitarised",
        "hpm:cutter_pirate",
        "hpm:corvette_steamship"
    );

    @Override
    public void runTest(ClientGameTestContext context) {
        HpmShipRenderer.resetCiRenderProbe();

        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            TestServerContext server = singleplayer.getServer();

            server.runCommand("time set noon");
            server.runCommand("weather clear");
            server.runCommand("fill -16 63 -4 16 63 28 minecraft:stone");
            server.runCommand("tp @a 0 65 0 0 12");

            String[] ships = {
                "hpm:raft",
                "hpm:swashbuckler",
                "hpm:swashbucklerupgraded",
                "hpm:cutter",
                "hpm:cuttermilitarised",
                "hpm:cutter_pirate",
                "hpm:corvette_steamship"
            };
            int[] xs = {-9, -6, -3, 0, 3, 6, 9};

            for (int i = 0; i < ships.length; i++) {
                server.runCommand("summon " + ships[i] + " " + xs[i] + " 64 18");
            }

            context.waitTicks(20);
            singleplayer.getClientLevel().waitForChunksRender();
            context.waitFor(client -> HpmShipRenderer.ciHasRenderedAll(SHIP_IDS), 30 * 20);
            context.takeScreenshot("swashbucklers-seven-ships-rendered");
            System.out.println("SWASHBUCKLERS_CLIENT_RENDER_TEST_OK " + HpmShipRenderer.ciRenderedIds());

            /*
             * Exact regression test for real original-style piloting:
             * - large open water
             * - raft yaw is explicitly 0
             * - player/camera faces 90 degrees sideways before mounting
             * - W must move along SHIP yaw, not camera yaw
             * - releasing W must retain the sail percentage and keep driving
             * - A must rotate ship yaw by the original small-ship rate
             * - pilot must remain centred, then dismount at ship centre rather than
             *   using vanilla Boat's side/shore dismount search
             */
            server.runCommand("kill @e[type=#minecraft:boat]");
            server.runCommand("fill -50 62 -50 50 62 50 minecraft:stone");
            server.runCommand("fill -50 63 -50 50 64 50 minecraft:water");
            server.runCommand("tp @a 0 65 0 90 0");
            server.runCommand("summon hpm:raft 0 64 4 {Rotation:[0f,0f],Tags:[\"hpm_ci_control\"]}");
            context.waitTicks(10);
            server.runCommand("ride @p mount @e[type=hpm:raft,tag=hpm_ci_control,limit=1]");
            context.waitTicks(10);

            Vec3 start = context.computeOnClient(client -> {
                if (client.player == null) {
                    throw new AssertionError("Client player was unavailable during original ship control test");
                }
                Entity vehicle = client.player.getVehicle();
                if (!(vehicle instanceof HpmControllableShip)) {
                    throw new AssertionError("Player could not mount controllable hpm:raft");
                }
                double seatHorizontalOffset = Math.hypot(
                        client.player.getX() - vehicle.getX(),
                        client.player.getZ() - vehicle.getZ());
                if (seatHorizontalOffset > 0.20D) {
                    throw new AssertionError("Raft pilot was not centred; horizontal seat offset=" + seatHorizontalOffset);
                }
                return vehicle.position();
            });

            // Raise the original sail percentage for 30 ticks. This is NOT hold-to-row.
            context.getInput().holdKeyFor(options -> options.keyUp, 30);
            context.waitTicks(2);

            ControlSnapshot afterSailUp = snapshot(context);
            if (afterSailUp.sailSpeed < 25.0F || afterSailUp.sailSpeed > 35.0F) {
                throw new AssertionError("Original sail throttle did not ramp near 30%; got " + afterSailUp.sailSpeed);
            }

            double movedX = afterSailUp.position.x - start.x;
            double movedZ = afterSailUp.position.z - start.z;
            double firstDistance = Math.hypot(movedX, movedZ);
            if (firstDistance < 0.75D) {
                throw new AssertionError("Raft did not move under original sail physics; distance=" + firstDistance);
            }
            if (Math.abs(movedZ) < Math.abs(movedX) * 2.0D) {
                throw new AssertionError("Raft followed the sideways rider/camera instead of ship yaw; dx="
                        + movedX + " dz=" + movedZ);
            }

            // Release W. Original sails stay where they were set and keep propelling.
            Vec3 coastStart = afterSailUp.position;
            context.waitTicks(20);
            ControlSnapshot afterRelease = snapshot(context);
            double coastDistance = horizontalDistance(coastStart, afterRelease.position);
            if (Math.abs(afterRelease.sailSpeed - afterSailUp.sailSpeed) > 1.5F) {
                throw new AssertionError("Sail speed did not persist after W release: before="
                        + afterSailUp.sailSpeed + " after=" + afterRelease.sailSpeed);
            }
            if (coastDistance < 0.75D) {
                throw new AssertionError("Raft stopped like a vanilla rowboat after W release; distance=" + coastDistance);
            }

            // With positive sails, A rotates a small ship at ~3 degrees per tick.
            float yawBeforeTurn = afterRelease.shipYaw;
            context.getInput().holdKeyFor(options -> options.keyLeft, 5);
            context.waitTicks(2);
            ControlSnapshot afterTurn = snapshot(context);
            float yawDelta = smallestAngleDifference(yawBeforeTurn, afterTurn.shipYaw);
            if (yawDelta < 9.0F || yawDelta > 24.0F) {
                throw new AssertionError("Small-ship original steering rate was wrong; yaw delta=" + yawDelta);
            }

            // Bring the persistent throttle back to approximately zero before dismount.
            int downTicks = Math.max(1, Math.round(afterTurn.sailSpeed));
            context.getInput().holdKeyFor(options -> options.keyDown, downTicks);
            context.waitTicks(3);
            ControlSnapshot stopped = snapshot(context);
            if (Math.abs(stopped.sailSpeed) > 2.0F) {
                throw new AssertionError("S could not lower sail throttle back to zero; got " + stopped.sailSpeed);
            }

            Vec3 shipAtDismount = stopped.position;
            context.takeScreenshot("swashbucklers-original-mounted-pilot");
            context.getInput().holdKeyFor(options -> options.keyShift, 2);
            context.waitTicks(5);

            context.computeOnClient(client -> {
                if (client.player == null) {
                    throw new AssertionError("Client player vanished during dismount test");
                }
                if (client.player.getVehicle() != null) {
                    throw new AssertionError("Player did not dismount hpm:raft");
                }
                double fromShipCentre = Math.hypot(
                        client.player.getX() - shipAtDismount.x,
                        client.player.getZ() - shipAtDismount.z);
                if (fromShipCentre > 0.80D) {
                    throw new AssertionError("Dismount used vanilla boat side/shore placement; centre distance=" + fromShipCentre);
                }
                return true;
            });

            System.out.println("SWASHBUCKLERS_ORIGINAL_CONTROL_OK sail=" + afterSailUp.sailSpeed
                    + " coast=" + coastDistance + " turn=" + yawDelta);
            context.takeScreenshot("swashbucklers-original-dismount");
        }
    }

    private static ControlSnapshot snapshot(ClientGameTestContext context) {
        return context.computeOnClient(client -> {
            if (client.player == null || client.player.getVehicle() == null) {
                throw new AssertionError("Player was unexpectedly dismounted during original control test");
            }
            Entity vehicle = client.player.getVehicle();
            if (!(vehicle instanceof HpmControllableShip ship)) {
                throw new AssertionError("Mounted vehicle is not HpmControllableShip");
            }
            return new ControlSnapshot(vehicle.position(), ship.hpm$getSailSpeed(), ship.hpm$getShipYaw());
        });
    }

    private static double horizontalDistance(Vec3 a, Vec3 b) {
        return Math.hypot(b.x - a.x, b.z - a.z);
    }

    private static float smallestAngleDifference(float a, float b) {
        float delta = Math.abs(a - b) % 360.0F;
        return delta > 180.0F ? 360.0F - delta : delta;
    }

    private record ControlSnapshot(Vec3 position, float sailSpeed, float shipYaw) {}
}
