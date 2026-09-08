package rem.coffenmod;

import net.minecraft.util.StringRepresentable;

public enum CoffinPart implements StringRepresentable {
    FOOT("foot"), HEAD("head");
    private final String name;
    CoffinPart(String name) { this.name = name; }
    @Override public String getSerializedName() { return name; }
}
