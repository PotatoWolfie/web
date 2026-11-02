package potatowolfie.web.entity.abominable_spider_queen;

import java.util.ArrayList;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import potatowolfie.web.entity.WebEntities;
import potatowolfie.web.entity.spider_web_projectile.PoisonSpiderWebProjectileEntity;
import potatowolfie.web.entity.spider_web_projectile.SpiderWebProjectileEntity;
import potatowolfie.web.goals.AbominableSpiderQueenAttackGoal;

public class AbominableSpiderQueenEntity extends Spider {
    public static final float DASH_HEALTH_THRESHOLD = 0.5F;
    private static final float SUMMON_HEALTH_THRESHOLD = 0.25F;
    private static final float POISONOUS_MINION_CHANCE = 0.25F;
    private static final float BABY_SPIDER_CHANCE = 0.5F;
    private static final int BITE_POISON_TICKS = 12 * 20;

    private static final int HOME_RADIUS = 15;
    private static final double HOME_RADIUS_SQR = HOME_RADIUS * HOME_RADIUS;
    private static final double HOME_RETURN_DONE_SQR = 10.0 * 10.0;

    private boolean hasSummonedReinforcements = false;
    @Nullable
    private BlockPos homePos = null;

    public AbominableSpiderQueenEntity(EntityType<? extends AbominableSpiderQueenEntity> type, Level level) {
        super(type, level);
        this.xpReward = 50;
    }

    public static AttributeSupplier.Builder createQueenAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 250.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.ATTACK_DAMAGE, 4.5)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.SCALE, 2.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new AbominableSpiderQueenAttackGoal(this));
        this.goalSelector.addGoal(3, new ReturnHomeGoal(this, 1.0));
        this.goalSelector.addGoal(5, new LeashedStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public SpiderWebProjectileEntity createWebProjectile(Level level) {
        return new PoisonSpiderWebProjectileEntity(level, this);
    }

    public float getHealthFraction() {
        return this.getHealth() / this.getMaxHealth();
    }

    public boolean isFreeRoaming() {
        LivingEntity target = this.getTarget();
        return target != null && target.isAlive();
    }

    public boolean isInsideHomeArea(double x, double z) {
        if (homePos == null) return true;
        double dx = x - (homePos.getX() + 0.5);
        double dz = z - (homePos.getZ() + 0.5);
        return dx * dx + dz * dz <= HOME_RADIUS_SQR;
    }

    private double horizontalDistSqrToHome(double x, double z) {
        if (homePos == null) return 0.0;
        double dx = x - (homePos.getX() + 0.5);
        double dz = z - (homePos.getZ() + 0.5);
        return dx * dx + dz * dz;
    }

    public boolean canDash() {
        return getHealthFraction() <= DASH_HEALTH_THRESHOLD;
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hurt = super.doHurtTarget(level, target);
        if (hurt && target instanceof LivingEntity living && level.getDifficulty() == Difficulty.HARD) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, BITE_POISON_TICKS, 0), this);
        }
        return hurt;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);

        if (homePos == null) {
            homePos = this.blockPosition();
        }

        if (!hasSummonedReinforcements && this.isAlive() && getHealthFraction() <= SUMMON_HEALTH_THRESHOLD) {
            hasSummonedReinforcements = true;
            summonReinforcements(level);
        }
    }

    private void summonReinforcements(ServerLevel level) {
        RandomSource random = this.getRandom();
        DifficultyInstance difficulty = level.getCurrentDifficultyAt(this.blockPosition());

        for (int i = 0; i < 2; i++) {
            EntityType<? extends Mob> type = random.nextFloat() < POISONOUS_MINION_CHANCE
                    ? EntityTypes.CAVE_SPIDER : EntityTypes.SPIDER;
            spawnMinion(level, difficulty, type.create(level, EntitySpawnReason.MOB_SUMMONED));
        }

        if (random.nextFloat() < BABY_SPIDER_CHANCE) {
            spawnMinion(level, difficulty, WebEntities.BABY_SPIDER.create(level, EntitySpawnReason.MOB_SUMMONED));
        }

        this.playSound(SoundEvents.SPIDER_AMBIENT, 2.0F, 0.5F);
    }

    private void spawnMinion(ServerLevel level, DifficultyInstance difficulty, @Nullable Mob minion) {
        if (minion == null) return;
        RandomSource random = this.getRandom();

        boolean placed = false;
        for (int attempt = 0; attempt < 10 && !placed; attempt++) {
            double x = this.getX() + (random.nextDouble() - 0.5) * 8.0;
            double z = this.getZ() + (random.nextDouble() - 0.5) * 8.0;
            minion.snapTo(x, this.getY(), z, random.nextFloat() * 360.0F, 0.0F);
            placed = level.noCollision(minion);
        }
        if (!placed) {
            minion.snapTo(this.getX(), this.getY(), this.getZ(), random.nextFloat() * 360.0F, 0.0F);
        }

        minion.finalizeSpawn(level, difficulty, EntitySpawnReason.MOB_SUMMONED, null);
        minion.setTarget(this.getTarget());
        level.addFreshEntity(minion);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                  EntitySpawnReason reason, @Nullable SpawnGroupData groupData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, groupData);
        if (this.homePos == null) {
            this.homePos = this.blockPosition();
        }
        for (Entity passenger : new ArrayList<>(this.getPassengers())) {
            passenger.discard();
        }
        this.removeAllEffects();
        return result;
    }

    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("HasSummonedReinforcements", this.hasSummonedReinforcements);
        output.putBoolean("HasHome", this.homePos != null);
        if (this.homePos != null) {
            output.putInt("HomeX", this.homePos.getX());
            output.putInt("HomeY", this.homePos.getY());
            output.putInt("HomeZ", this.homePos.getZ());
        }
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.hasSummonedReinforcements = input.getBooleanOr("HasSummonedReinforcements", false);
        if (input.getBooleanOr("HasHome", false)) {
            this.homePos = new BlockPos(
                    input.getIntOr("HomeX", this.getBlockX()),
                    input.getIntOr("HomeY", this.getBlockY()),
                    input.getIntOr("HomeZ", this.getBlockZ()));
        }
    }

    private static class ReturnHomeGoal extends Goal {
        private final AbominableSpiderQueenEntity queen;
        private final double speed;

        ReturnHomeGoal(AbominableSpiderQueenEntity queen, double speed) {
            this.queen = queen;
            this.speed = speed;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return queen.homePos != null
                    && !queen.isFreeRoaming()
                    && !queen.isInsideHomeArea(queen.getX(), queen.getZ());
        }

        @Override
        public boolean canContinueToUse() {
            return queen.homePos != null
                    && !queen.isFreeRoaming()
                    && queen.horizontalDistSqrToHome(queen.getX(), queen.getZ()) > HOME_RETURN_DONE_SQR;
        }

        @Override
        public void start() {
            moveTowardHome();
        }

        @Override
        public void tick() {
            if (queen.getNavigation().isDone()) {
                moveTowardHome();
            }
        }

        @Override
        public void stop() {
            queen.getNavigation().stop();
        }

        private void moveTowardHome() {
            BlockPos home = queen.homePos;
            if (home == null) return;

            double hx = home.getX() + 0.5;
            double hz = home.getZ() + 0.5;
            double dx = hx - queen.getX();
            double dz = hz - queen.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);

            double step = Math.min(dist, 16.0);
            double tx = dist < 1.0E-4 ? hx : queen.getX() + dx / dist * step;
            double tz = dist < 1.0E-4 ? hz : queen.getZ() + dz / dist * step;
            queen.getNavigation().moveTo(tx, home.getY(), tz, speed);
        }
    }

    private static class LeashedStrollGoal extends WaterAvoidingRandomStrollGoal {
        private final AbominableSpiderQueenEntity queen;

        LeashedStrollGoal(AbominableSpiderQueenEntity queen, double speed) {
            super(queen, speed);
            this.queen = queen;
        }

        @Override
        protected @Nullable Vec3 getPosition() {
            for (int i = 0; i < 10; i++) {
                Vec3 pos = super.getPosition();
                if (pos != null && (queen.isFreeRoaming() || queen.isInsideHomeArea(pos.x, pos.z))) {
                    return pos;
                }
            }
            return null;
        }
    }
}