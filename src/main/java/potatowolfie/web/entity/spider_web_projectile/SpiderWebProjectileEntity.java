package potatowolfie.web.entity.spider_web_projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import potatowolfie.web.entity.WebEntities;
import potatowolfie.web.entity.spider_web.SpiderWebEntity;
import potatowolfie.web.item.WebItems;
import potatowolfie.web.particle.WebParticles;
import potatowolfie.web.sound.WebSounds;

public class SpiderWebProjectileEntity extends AbstractArrow {

    private int trailCooldown = 5;

    public SpiderWebProjectileEntity(Level world, LivingEntity owner) {
        this(WebEntities.SPIDER_WEB_FLYING, world, owner);
    }

    protected SpiderWebProjectileEntity(EntityType<? extends SpiderWebProjectileEntity> type, Level world, LivingEntity owner) {
        super(type, world);
        if (owner != null) {
            this.setOwner(owner);
            this.setPos(owner.getX(), owner.getEyeY() - 0.3, owner.getZ());

            if (!world.isClientSide()) {
                world.playSound(null, owner.getX(), owner.getEyeY(), owner.getZ(),
                        WebSounds.WEB_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }

    public SpiderWebProjectileEntity(Level world, double x, double y, double z) {
        super(WebEntities.SPIDER_WEB_FLYING, world);
        this.setPos(x, y, z);
    }

    public SpiderWebProjectileEntity(EntityType<? extends SpiderWebProjectileEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    public void tick() {
        super.tick();

        Vec3 velocity = this.getDeltaMovement();
        if (velocity != null && velocity.lengthSqr() > 0.0001) {
            this.setRot(
                    (float)(Math.atan2(velocity.x, velocity.z) * 180.0 / Math.PI),
                    (float)(Math.atan2(velocity.y, Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z)) * 180.0 / Math.PI)
            );
        }

        if (this.level().isClientSide()) {
            if (--this.trailCooldown <= 0) {
                this.trailCooldown = 3 + this.random.nextInt(4);
                this.level().addParticle(getTrailParticle(),
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getY() + 0.1 + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
                        0.0, 0.0, 0.0);
            }
        }
    }

    protected SimpleParticleType getTrailParticle() {
        return WebParticles.WEB_TRAIL;
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        if (!this.level().isClientSide()) {
            Vec3 pos = this.position();
            this.level().playSound(null, pos.x, pos.y, pos.z,
                    WebSounds.WEB_LAND, SoundSource.NEUTRAL, 0.8F, 1.0F);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);

        if (blockHitResult == null) {
            return;
        }

        Level world = this.level();
        if (world != null && !world.isClientSide()) {
            Vec3 pos = this.position();
            world.playSound(null, pos.x, pos.y, pos.z,
                    WebSounds.WEB_LAND, SoundSource.BLOCKS, 0.8F, 1.0F);

            BlockPos hitPos = blockHitResult.getBlockPos();
            Direction hitSide = blockHitResult.getDirection();

            if (hitPos != null && hitSide != null) {
                BlockPos spawnPos = hitPos.relative(hitSide);
                spawnSpiderWebEntityAt(spawnPos, resolveFacing(hitPos, hitSide, world));
            }
            this.discard();
        }
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(WebItems.SPIDER_WEB);
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.EMPTY;
    }

    private void spawnSpiderWebEntity() {
        BlockPos currentPos = this.blockPosition();
        if (currentPos != null) {
            spawnSpiderWebEntityAt(currentPos, Direction.DOWN);
        }
    }

    protected SpiderWebEntity createWebEntity(Level world) {
        return new SpiderWebEntity(WebEntities.SPIDER_WEB, world);
    }

    private void spawnSpiderWebEntityAt(BlockPos pos, Direction facing) {
        if (pos == null) {
            return;
        }

        Level world = this.level();
        if (world == null) {
            return;
        }

        if (world.getBlockState(pos).canBeReplaced()) {
            SpiderWebEntity spiderWebEntity = createWebEntity(world);
            if (spiderWebEntity != null) {
                spiderWebEntity.setPos(pos.getX() + 0.5F, pos.getY(), pos.getZ() + 0.5F);

                spiderWebEntity.setOwner(this.getOwner());
                spiderWebEntity.setFacing(facing);

                world.addFreshEntity(spiderWebEntity);
            }
        }
    }

    /**
     * Decides what surface the web should visually stick to. Floor hits (the
     * common case, hitSide == UP) always use the default DOWN/hanging look.
     * Ceiling hits (hitSide == DOWN) always get the upside-down look - no
     * area requirement, since a single block ceiling should still look right.
     * Wall hits (horizontal hitSide) only get the sideways look if the
     * surface is part of a solid 2x2 block area, so a thin 1-wide ledge
     * doesn't awkwardly force a sideways web when it should really read as
     * a "floor" hit. Anything that doesn't qualify falls back to the default look.
     * Note: this only changes rendering - the trap hitbox stays exactly as it was.
     */
    private Direction resolveFacing(BlockPos hitPos, Direction hitSide, Level world) {
        if (hitSide == Direction.UP) {
            return Direction.DOWN;
        }

        if (hitSide == Direction.DOWN) {
            return Direction.UP;
        }

        if (has2x2SolidArea(hitPos, hitSide, world)) {
            return hitSide;
        }

        return Direction.DOWN;
    }

    private boolean has2x2SolidArea(BlockPos hitPos, Direction hitSide, Level world) {
        Direction[] axes = perpendicularAxes(hitSide);
        Direction a = axes[0];
        Direction b = axes[1];

        BlockPos[] toCheck = new BlockPos[]{
                hitPos,
                hitPos.relative(a),
                hitPos.relative(b),
                hitPos.relative(a).relative(b)
        };

        for (BlockPos pos : toCheck) {
            if (isSolidSurface(world, pos)) {
                continue;
            }
            return checkAlternateCorner(hitPos, a, b, world);
        }
        return true;
    }

    private boolean checkAlternateCorner(BlockPos hitPos, Direction a, Direction b, Level world) {
        Direction aOpp = a.getOpposite();
        Direction bOpp = b.getOpposite();

        Direction[][] cornerPairs = new Direction[][]{
                {a, bOpp}, {aOpp, b}, {aOpp, bOpp}
        };

        for (Direction[] pair : cornerPairs) {
            BlockPos[] toCheck = new BlockPos[]{
                    hitPos,
                    hitPos.relative(pair[0]),
                    hitPos.relative(pair[1]),
                    hitPos.relative(pair[0]).relative(pair[1])
            };

            boolean allSolid = true;
            for (BlockPos pos : toCheck) {
                if (!isSolidSurface(world, pos)) {
                    allSolid = false;
                    break;
                }
            }
            if (allSolid) {
                return true;
            }
        }
        return false;
    }

    private boolean isSolidSurface(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return !state.canBeReplaced();
    }

    private Direction[] perpendicularAxes(Direction facing) {
        switch (facing.getAxis()) {
            case Y:
                return new Direction[]{Direction.NORTH, Direction.EAST};
            case X:
                return new Direction[]{Direction.UP, Direction.SOUTH};
            case Z:
            default:
                return new Direction[]{Direction.UP, Direction.EAST};
        }
    }
}