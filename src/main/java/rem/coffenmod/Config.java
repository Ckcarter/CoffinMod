package rem.coffenmod;

import net.minecraftforge.common.ForgeConfigSpec;

public final class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.DoubleValue GHOST_SPEED = BUILDER.comment("Ghost chase acceleration").defineInRange("ghostSpeed", 0.08D, 0.01D, 1.0D);
    public static final ForgeConfigSpec.DoubleValue GHOST_DAMAGE = BUILDER.comment("Base ghost attack damage").defineInRange("ghostDamage", 2.0D, 0.0D, 100.0D);
    public static final ForgeConfigSpec.BooleanValue OTHER_PLAYERS_CAN_KILL = BUILDER.define("otherPlayersCanKill", false);
    public static final ForgeConfigSpec.BooleanValue CHASE_OTHER_PLAYERS = BUILDER.define("chaseOtherPlayers", true);
    public static final ForgeConfigSpec.BooleanValue EQUIP_BOWS = BUILDER.define("equipBows", true);
    public static final ForgeConfigSpec.BooleanValue HEADSTONE = BUILDER.define("headstone", true);
    public static final ForgeConfigSpec SPEC = BUILDER.build();
    private Config() {}
}
