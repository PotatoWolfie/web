package potatowolfie.web.entity.custom;

import net.minecraft.entity.AnimationState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import potatowolfie.web.Web;
import potatowolfie.web.entity.WebEntities;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SpiderWebEntity extends Entity {

    private int age = 0;
    private static final int MAX_AGE = 60;
    private static final int TRAP_DURATION = 40;

    private static final TagKey<EntityType<?>> WEB_IMMUNE_TAG = TagKey.of(RegistryKeys.ENTITY_TYPE,
            Identifier.of(Web.MOD_ID, "web_immune"));

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

    private final Set<SpiderEntity> alertedSpiders = new HashSet<>();

    private LivingEntity webTarget = null;

    private static boolean isWebImmune(LivingEntity entity) {
        return entity.getType().isIn(WEB_IMMUNE_TAG);
    }

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
                if (!trappedEntities.isEmpty()) {
                    alertAllSpiders();
                } else {
                    clearSpiderTargets();
                }
            } else if (this.age == TRAP_DURATION + 1) {
                releaseAllEntities();
                clearSpiderTargets();
            }

            if (this.age >= MAX_AGE) {
                releaseAllEntities();
                clearSpiderTargets();
                this.discard();
            }
        }

        try {
            updateAnimations();
        } catch (Exception ignored) {
        }
    }

    private void alertAllSpiders() {
        LivingEntity target = null;
        for (LivingEntity trapped : trappedEntities) {
            if (trapped.isAlive() && !(trapped instanceof SpiderEntity) && !isWebImmune(trapped)) {
                target = trapped;
                break;
            }
        }

        if (target == null) {
            clearSpiderTargets();
            return;
        }

        Box searchBox = Box.of(this.getPos(), 128, 64, 128);
        List<SpiderEntity> nearbySpiders = this.getWorld().getEntitiesByClass(
                SpiderEntity.class,
                searchBox,
                spider -> spider.isAlive()
        );

        for (SpiderEntity spider : nearbySpiders) {
            double distance = spider.distanceTo(target);
            if (distance <= 64.0) {
                spider.setTarget(target);
                alertedSpiders.add(spider);
            }
        }
    }

    private void clearSpiderTargets() {
        Iterator<SpiderEntity> iterator = alertedSpiders.iterator();
        while (iterator.hasNext()) {
            SpiderEntity spider = iterator.next();
            if (spider.isAlive()) {
                LivingEntity currentTarget = spider.getTarget();
                if (currentTarget != null && (webTarget == currentTarget ||
                        originalMovementSpeeds.containsKey(currentTarget))) {
                    spider.setTarget(null);
                }
            }
            iterator.remove();
        }
        alertedSpiders.clear();
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
                entity -> entity.isAlive() && !isWebImmune(entity)
        );

        for (LivingEntity entity : nearbyEntities) {
            trapEntity(entity);

            if (this.webTarget == null) {
                this.webTarget = entity;
            }
        }
    }

    private void maintainTrappedEntities() {
        Iterator<LivingEntity> iterator = trappedEntities.iterator();
        while (iterator.hasNext()) {
            LivingEntity entity = iterator.next();

            if (!entity.isAlive() || isWebImmune(entity)) {
                releaseEntity(entity);
                iterator.remove();
                continue;
            }
        }

        if (trappedEntities.isEmpty()) {
            clearSpiderTargets();
        }
    }

    private void trapEntity(LivingEntity entity) {
        if (isWebImmune(entity)) {
            return;
        }

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
            clearSpiderTargets();
        }
        super.remove(reason);
    }

    public boolean shouldPreventJump(LivingEntity entity) {
        return trappedEntities.contains(entity) && this.age <= TRAP_DURATION && !isWebImmune(entity);
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
        if (isWebImmune(entity)) {
            return false;
        }
        SpiderWebEntity web = GLOBALLY_TRAPPED_ENTITIES.get(entity);
        return web != null && web.isTrapping();
    }

    public static boolean shouldPreventMovement(LivingEntity entity) {
        return isEntityTrapped(entity) && !isWebImmune(entity);
    }

    public static boolean shouldPreventJumping(LivingEntity entity) {
        return isEntityTrapped(entity) && !isWebImmune(entity);
    }

    public static boolean isEntityTouchingWeb(LivingEntity entity) {
        return false;
    }

    public LivingEntity getWebTarget() {
        return webTarget;
    }
}