package potatowolfie.web.block.custom;

public interface GelClimber {
    int web$getStickTicks();

    void web$setStickTicks(int ticks);

    int web$getLastCountTick();

    void web$setLastCountTick(int tick);

    void web$allowGelClimb();

    boolean web$canGelClimb();

    void web$allowGelHold();

    boolean web$isGelHolding();

    void web$releaseGelHold();
}