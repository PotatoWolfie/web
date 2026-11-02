package potatowolfie.web.block.custom;

import net.minecraft.util.StringRepresentable;

public enum EggStage implements StringRepresentable {
    OLD("old"),
    ACTIVE("active"),
    DECAYED("decayed");

    private final String name;

    EggStage(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}