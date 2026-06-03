package potatowolfie.web.entity.custom;

import potatowolfie.web.Web;
import potatowolfie.web.entity.WebEntities;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

public class SpiderWebEntity extends Entity {

    private int age = 0;
    private static final int MAX_AGE = 60;
    private static final int TRAP_DURATION = 40;
    public static final double PLAYER_WEB_SPEED_MULTIPLIER = 0.25;

    private static final TagKey<EntityType<?>> WEB_IMMUNE_TAG = TagKey.create(Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Web.MOD_ID, "web_immune"));

    public final AnimationState webDieAnimationState = new AnimationState();

    private boolean isDieAnimationRunning = false;
    private boolean animationStartedThisTick = false;

    public enum WebState {
        DYING
    }

    private static final EntityDataAccessor<Integer> DATA_ID_STATE =
            SynchedEntityData.defineId(SpiderWebEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> DATA_OWNER_ID =
            SynchedEntityData.defineId(SpiderWebEntity.class, EntityDataSerializers.INT);

    private WebState webState = WebState.DYING;
    private WebState previousState = WebState.DYING;
    private boolean isChangingState = false;

    private static final java.util.Map<LivingEntity, SpiderWebEntity> GLOBALLY_TRAPPED_ENTITIES = new java.util.HashMap<>();

    private final Set<LivingEntity> trappedEntities = new HashSet<>();
    private final java.util.Map<LivingEntity, Float> originalMovementSpeeds = new java.util.HashMap<>();
    private final java.util.Map<LivingEntity, Float> originalJumpStrengths = new java.util.HashMap<>();

    private final Set<Spider> alertedSpiders = new HashSet<>();

    private LivingEntity webTarget = null;

    private static boolean isWebImmune(LivingEntity entity) {
        return entity.is(WEB_IMMUNE_TAG);
    }

    public int getTickCount() {
        return this.tickCount;
    }

    public SpiderWebEntity(EntityType<?> type, Level world) {
        super(type, world);
        this.noPhysics = false;
        this.setInvulnerable(false);
    }

    public SpiderWebEntity(Level world, double x, double y, double z) {
        this(WebEntities.SPIDER_WEB, world);
        this.setPos(x, y, z);
        this.setBoundingBox(new AABB(x - 1.0, y - 0.5, z - 1.0, x + 1.0, y + 1.5, z + 1.0));

        if (world.isClientSide()) {
            this.webDieAnimationState.start(0);
            this.isDieAnimationRunning = true;
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ID_STATE, WebState.DYING.ordinal());
        builder.define(DATA_OWNER_ID, -1);
    }

    @Override
    public void tick() {
        if (this.isRemoved() || this.level() == null) {
            return;
        }

        animationStartedThisTick = false;

        super.tick();

        if (!this.level().isClientSide()) {

            if (this.tickCount == 1) {
                trapEntitiesInRange();
            }

            if (this.tickCount <= TRAP_DURATION) {
                maintainTrappedEntities();
                if (!trappedEntities.isEmpty()) {
                    alertAllSpiders();
                } else {
                    clearSpiderTargets();
                }
            } else if (this.tickCount == TRAP_DURATION + 1) {
                releaseAllEntities();
                clearSpiderTargets();
            }

            if (this.tickCount >= MAX_AGE) {
                releaseAllEntities();
                clearSpiderTargets();
                this.discard();
            }
        }

        if (this.level().isClientSide()) {
            try {
                updateAnimations();
            } catch (Exception ignored) {
            }
        }
    }

    private void alertAllSpiders() {
        LivingEntity target = null;
        for (LivingEntity trapped : trappedEntities) {
            if (trapped.isAlive() && !(trapped instanceof Spider) && !isWebImmune(trapped)) {
                target = trapped;
                break;
            }
        }

        if (target == null) {
            clearSpiderTargets();
            return;
        }

        AABB searchBox = AABB.ofSize(this.position(), 128, 64, 128);
        List<Spider> nearbySpiders = this.level().getEntitiesOfClass(
                Spider.class,
                searchBox,
                spider -> spider.isAlive()
        );

        for (Spider spider : nearbySpiders) {
            double distance = spider.distanceTo(target);
            if (distance <= 64.0) {
                spider.setTarget(target);
                alertedSpiders.add(spider);
            }
        }
    }

    private void clearSpiderTargets() {
        Iterator<Spider> iterator = alertedSpiders.iterator();
        while (iterator.hasNext()) {
            Spider spider = iterator.next();
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
        if (this.level().isClientSide()) {
            if (!isDieAnimationRunning) {
                this.webDieAnimationState.start(this.tickCount);
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

            if (!this.level().isClientSide()) {
                this.entityData.set(DATA_ID_STATE, newState.ordinal());
            } else {
                startStateAnimation(newState);
            }

            isChangingState = false;
        }
    }

    private void startStateAnimation(WebState state) {
        if (!this.level().isClientSide() || animationStartedThisTick) return;

        animationStartedThisTick = true;

        this.webDieAnimationState.start(this.tickCount);
        this.isDieAnimationRunning = true;
    }

    private void stopAllAnimations() {
        if (this.level().isClientSide()) {
            webDieAnimationState.stop();
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
        if (DATA_ID_STATE.equals(data) && this.level().isClientSide()) {
            WebState newState = WebState.values()[this.entityData.get(DATA_ID_STATE)];
            if (this.webState != newState && !isChangingState) {
                isChangingState = true;

                this.previousState = this.webState;
                this.webState = newState;

                startStateAnimation(newState);

                isChangingState = false;
            }
        }
        super.onSyncedDataUpdated(data);
    }

    private java.util.UUID ownerUUID = null;

    public void setOwner(Entity owner) {
        if (owner != null) {
            this.entityData.set(DATA_OWNER_ID, owner.getId());
        }
    }

    public boolean isOwnerPlayer() {
        int ownerId = this.entityData.get(DATA_OWNER_ID);
        if (ownerId == -1) return false;

        Entity owner = this.level().getEntity(ownerId);
        return owner instanceof net.minecraft.world.entity.player.Player;
    }

    public static SpiderWebEntity getWebTrappingEntity(LivingEntity entity) {
        return GLOBALLY_TRAPPED_ENTITIES.get(entity);
    }

    private void trapEntitiesInRange() {
        AABB webBox = this.getBoundingBox().inflate(0.1);

        List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(
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

        AABB escapeCheckBox = this.getBoundingBox().inflate(0.1);

        while (iterator.hasNext()) {
            LivingEntity entity = iterator.next();

            if (!entity.isAlive() || isWebImmune(entity)) {
                releaseEntity(entity);
                iterator.remove();
                continue;
            }

            if (isOwnerPlayer()) {
                if (!escapeCheckBox.intersects(entity.getBoundingBox())) {
                    releaseEntity(entity);
                    iterator.remove();
                    continue;
                }
            }

            if (!isOwnerPlayer()) {
                entity.setSpeed(0.0F);
                double yVelocity = entity.getDeltaMovement().y;
                entity.setDeltaMovement(0, entity.isNoGravity() ? 0 : yVelocity, 0);
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

        float originalSpeed = entity.getSpeed();
        originalMovementSpeeds.put(entity, originalSpeed);

        if (isOwnerPlayer()) {
            entity.setSpeed(originalSpeed * 0.4F);
        } else {
            entity.setSpeed(0.0F);
            entity.setDeltaMovement(0, 0, 0);
        }

        if (entity.isNoGravity()) {
            originalJumpStrengths.put(entity, 1.0f);
        } else {
            originalJumpStrengths.put(entity, 0.42f);
        }
    }

    private void releaseEntity(LivingEntity entity) {
        GLOBALLY_TRAPPED_ENTITIES.remove(entity);

        Float originalSpeed = originalMovementSpeeds.remove(entity);
        if (originalSpeed != null) {
            entity.setSpeed(originalSpeed);
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
        if (!this.level().isClientSide()) {
            releaseAllEntities();
            clearSpiderTargets();
        }
        super.remove(reason);
    }

    public boolean shouldPreventJump(LivingEntity entity) {
        return trappedEntities.contains(entity) && this.tickCount <= TRAP_DURATION && !isWebImmune(entity);
    }

    @Override
    public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput view) {
        String stateString = view.getStringOr("WebState", "DYING");
        this.webState = WebState.DYING;
        if (!this.level().isClientSide()) {
            this.entityData.set(DATA_ID_STATE, WebState.DYING.ordinal());
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput view) {
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
        return (float) this.tickCount / MAX_AGE;
    }

    public int getAge() {
        return this.tickCount;
    }

    public boolean isAnimationActive() {
        return this.webDieAnimationState.isStarted();
    }

    public float getAnimationProgress() {
        return (float) this.tickCount / MAX_AGE;
    }

    public Set<LivingEntity> getTrappedEntities() {
        return new HashSet<>(trappedEntities);
    }

    public boolean isTrapping() {
        return this.tickCount <= TRAP_DURATION;
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