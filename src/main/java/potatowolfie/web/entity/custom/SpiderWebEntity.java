package potatowolfie.web.entity.custom;

import net.minecraft.entity.AnimationState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import potatowolfie.web.entity.WebEntities;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SpiderWebEntity extends Entity {

    private int age = 0;
    private static final int MAX_AGE = 65;
    private static final int TRAP_DURATION = 40;

    // Animation state - only death animation
    public final AnimationState webDieAnimationState = new AnimationState();

    private boolean isDieAnimationRunning = false;
    private boolean animationStartedThisTick = false;

    public enum WebState {
        DYING
    }

    private static final TrackedData<Integer> DATA_ID_STATE =
            DataTracker.registerData(SpiderWebEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private WebState webState = WebState.DYING;
    private WebState previousState = WebState.DYING;
    private boolean isChangingState = false;

    private static final java.util.Map<LivingEntity, SpiderWebEntity> GLOBALLY_TRAPPED_ENTITIES = new java.util.HashMap<>();

    private final Set<LivingEntity> trappedEntities = new HashSet<>();
    private final java.util.Map<LivingEntity, Float> originalMovementSpeeds = new java.util.HashMap<>();
    private final java.util.Map<LivingEntity, Float> originalJumpStrengths = new java.util.HashMap<>();

    public int getTickCount() {
        return this.age;
    }

    public SpiderWebEntity(EntityType<?> type, World world) {
        super(type, world);
        this.noClip = false;
        this.setInvulnerable(false);
    }

    public SpiderWebEntity(World world, double x, double y, double z) {
        this(WebEntities.SPIDER_WEB, world);
        this.setPosition(x, y, z);
        this.setBoundingBox(new Box(x - 1.0, y - 0.5, z - 1.0, x + 1.0, y + 1.5, z + 1.0));

        // Start animation immediately on client
        if (world.isClient) {
            this.webDieAnimationState.start(0);
            this.isDieAnimationRunning = true;
        }
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(DATA_ID_STATE, WebState.DYING.ordinal());
    }

    @Override
    public void tick() {
        if (this.isRemoved() || this.getWorld() == null) {
            return;
        }

        animationStartedThisTick = false;
        super.tick();

        if (!this.getWorld().isClient) {
            this.age++;

            if (this.age == 1) {
                trapEntitiesInRange();
            }

            if (this.age <= TRAP_DURATION) {
                maintainTrappedEntities();
            } else if (this.age == TRAP_DURATION + 1) {
                releaseAllEntities();
            }

            if (this.age >= MAX_AGE) {
                releaseAllEntities();
                this.discard();
            }
        }

        try {
            updateAnimations();
        } catch (Exception ignored) {
        }
    }

    private void updateAnimations() {
        if (this.getWorld().isClient()) {
            if (!isDieAnimationRunning) {
                this.webDieAnimationState.start(this.age);
                this.isDieAnimationRunning = true;
            }
        }
    }

    public WebState getWebState() {
        return webState;
    }

    public WebState getPreviousState() {
        return previousState;
    }

    public void setWebState(WebState newState) {
        if (this.webState != newState && !isChangingState) {
            isChangingState = true;

            this.previousState = this.webState;
            this.webState = newState;

            if (!this.getWorld().isClient()) {
                this.dataTracker.set(DATA_ID_STATE, newState.ordinal());
            } else {
                startStateAnimation(newState);
            }

            isChangingState = false;
        }
    }

    private void startStateAnimation(WebState state) {
        if (!this.getWorld().isClient() || animationStartedThisTick) return;

        animationStartedThisTick = true;

        // Always start death animation since that's all we have
        this.webDieAnimationState.start(this.age);
        this.isDieAnimationRunning = true;
    }

    private void stopAllAnimations() {
        if (this.getWorld().isClient()) {
            webDieAnimationState.stop();
        }
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (DATA_ID_STATE.equals(data) && this.getWorld().isClient()) {
            WebState newState = WebState.values()[this.dataTracker.get(DATA_ID_STATE)];
            if (this.webState != newState && !isChangingState) {
                isChangingState = true;

                this.previousState = this.webState;
                this.webState = newState;

                startStateAnimation(newState);

                isChangingState = false;
            }
        }
        super.onTrackedDataSet(data);
    }

    private void trapEntitiesInRange() {
        Box webBox = this.getBoundingBox().expand(0.1);

        List<LivingEntity> nearbyEntities = this.getWorld().getEntitiesByClass(
                LivingEntity.class,
                webBox,
                entity -> entity.isAlive()
        );

        for (LivingEntity entity : nearbyEntities) {
            trapEntity(entity);
        }
    }

    private void maintainTrappedEntities() {
        Iterator<LivingEntity> iterator = trappedEntities.iterator();
        while (iterator.hasNext()) {
            LivingEntity entity = iterator.next();

            if (!entity.isAlive()) {
                releaseEntity(entity);
                iterator.remove();
                continue;
            }
        }
    }

    private void constrainEntityToWeb(LivingEntity entity, Box webBox) {
        Box entityBox = entity.getBoundingBox();
        double entityX = entity.getX();
        double entityY = entity.getY();
        double entityZ = entity.getZ();

        boolean needsRepositioning = false;
        double newX = entityX;
        double newY = entityY;
        double newZ = entityZ;

        if (entityBox.minX < webBox.minX) {
            newX = webBox.minX + (entityBox.maxX - entityBox.minX) / 2;
            needsRepositioning = true;
        } else if (entityBox.maxX > webBox.maxX) {
            newX = webBox.maxX - (entityBox.maxX - entityBox.minX) / 2;
            needsRepositioning = true;
        }

        if (entityBox.minY < webBox.minY) {
            newY = webBox.minY;
            needsRepositioning = true;
        } else if (entityBox.maxY > webBox.maxY) {
            newY = webBox.maxY - (entityBox.maxY - entityBox.minY);
            needsRepositioning = true;
        }

        if (entityBox.minZ < webBox.minZ) {
            newZ = webBox.minZ + (entityBox.maxZ - entityBox.minZ) / 2;
            needsRepositioning = true;
        } else if (entityBox.maxZ > webBox.maxZ) {
            newZ = webBox.maxZ - (entityBox.maxZ - entityBox.minZ) / 2;
            needsRepositioning = true;
        }

        if (needsRepositioning) {
            entity.setPosition(newX, newY, newZ);
            entity.setVelocity(0, 0, 0);

            if (entityBox.minY <= webBox.minY) {
                entity.setOnGround(true);
            }
        }
    }

    private void trapEntity(LivingEntity entity) {
        trappedEntities.add(entity);
        GLOBALLY_TRAPPED_ENTITIES.put(entity, this);

        float originalSpeed = entity.getMovementSpeed();
        originalMovementSpeeds.put(entity, originalSpeed);

        if (entity.hasNoGravity()) {
            originalJumpStrengths.put(entity, 1.0f);
        } else {
            originalJumpStrengths.put(entity, 0.42f);
        }

        entity.setVelocity(0, 0, 0);
    }

    private void releaseEntity(LivingEntity entity) {
        GLOBALLY_TRAPPED_ENTITIES.remove(entity);

        Float originalSpeed = originalMovementSpeeds.remove(entity);
        if (originalSpeed != null) {
            entity.setMovementSpeed(originalSpeed);
        }

        originalJumpStrengths.remove(entity);
    }

    private void releaseAllEntities() {
        for (LivingEntity entity : new HashSet<>(trappedEntities)) {
            releaseEntity(entity);
        }
        trappedEntities.clear();
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.getWorld().isClient) {
            releaseAllEntities();
        }
        super.remove(reason);
    }

    public boolean shouldPreventJump(LivingEntity entity) {
        return trappedEntities.contains(entity) && this.age <= TRAP_DURATION;
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean canHit() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void readCustomData(ReadView view) {
        String stateString = view.getString("WebState", "DYING");
        // Always dying, so no need to change state
        this.webState = WebState.DYING;
        if (!this.getWorld().isClient()) {
            this.dataTracker.set(DATA_ID_STATE, WebState.DYING.ordinal());
        }
    }

    @Override
    protected void writeCustomData(WriteView view) {
        view.putString("WebState", webState.name());
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return false;
    }

    @Override
    public boolean canBeHitByProjectile() {
        return false;
    }

    // State and animation getters following Brine pattern
    public float getAgeProgress() {
        return (float) this.age / MAX_AGE;
    }

    public int getAge() {
        return this.age;
    }

    public boolean isAnimationActive() {
        return this.webDieAnimationState.isRunning();
    }

    public float getAnimationProgress() {
        return (float) this.age / MAX_AGE;
    }

    public Set<LivingEntity> getTrappedEntities() {
        return new HashSet<>(trappedEntities);
    }

    public boolean isTrapping() {
        return this.age <= TRAP_DURATION;
    }

    public static boolean isEntityTrapped(LivingEntity entity) {
        SpiderWebEntity web = GLOBALLY_TRAPPED_ENTITIES.get(entity);
        return web != null && web.isTrapping();
    }

    public static boolean shouldPreventMovement(LivingEntity entity) {
        return isEntityTrapped(entity);
    }

    public static boolean shouldPreventJumping(LivingEntity entity) {
        return isEntityTrapped(entity);
    }
}