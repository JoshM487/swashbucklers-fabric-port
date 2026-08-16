package hal.studios.hpm.network;

import hal.studios.hpm.HpmMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record HpmShipInputPayload(int entityId, int flags) implements CustomPacketPayload {
    public static final Identifier ID = HpmMod.id("ship_input");
    public static final Type<HpmShipInputPayload> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, HpmShipInputPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, HpmShipInputPayload::entityId,
            ByteBufCodecs.INT, HpmShipInputPayload::flags,
            HpmShipInputPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
