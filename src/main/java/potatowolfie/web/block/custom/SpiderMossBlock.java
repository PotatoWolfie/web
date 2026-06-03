package potatowolfie.web.block.custom;

import potatowolfie.web.block.WebBlocks;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class SpiderMossBlock extends Block implements BonemealableBlock {

    public SpiderMossBlock(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.is(Items.BONE_MEAL)) {
            if (!world.isClientSide()) {
                if (this.isValidBonemealTarget(world, pos, state)) {
                    this.performBonemeal((ServerLevel) world, world.getRandom(), pos, state);

                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }

                    world.playSound(null, pos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                    ((ServerLevel) world).sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            15, 0.5, 0.5, 0.5, 0.0);
                }
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state) {
        return !getValidSpreadPositions(world, pos).isEmpty() || !getValidGrassPositions(world, pos).isEmpty();
    }

    @Override
    public boolean isBonemealSuccess(Level world, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state) {
        List<BlockPos> validSpreadPositions = getValidSpreadPositions(world, pos);
        List<BlockPos> validGrassPositions = getValidGrassPositions(world, pos);

        if (!validSpreadPositions.isEmpty()) {
            int spreadCount = 3 + random.nextInt(6);
            spreadCount = Math.min(spreadCount, validSpreadPositions.size());

            for (int i = 0; i < spreadCount; i++) {
                BlockPos targetPos = validSpreadPositions.get(random.nextInt(validSpreadPositions.size()));
                validSpreadPositions.remove(targetPos);

                world.setBlockAndUpdate(targetPos, this.defaultBlockState());

                world.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5,
                        8, 0.5, 0.5, 0.5, 0.0);
            }
        }

        if (!validGrassPositions.isEmpty()) {
            int grassCount = 1 + random.nextInt(4);
            grassCount = Math.min(grassCount, validGrassPositions.size());

            for (int i = 0; i < grassCount; i++) {
                BlockPos targetPos = validGrassPositions.get(random.nextInt(validGrassPositions.size()));
                validGrassPositions.remove(targetPos);

                world.setBlockAndUpdate(targetPos, WebBlocks.SPIDER_GRASS.defaultBlockState());

                world.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5,
                        5, 0.3, 0.3, 0.3, 0.0);
            }
        }
    }

    private List<BlockPos> getValidSpreadPositions(LevelReader world, BlockPos center) {
        List<BlockPos> validPositions = new ArrayList<>();

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }

                    BlockPos checkPos = center.offset(x, y, z);

                    if (canSpreadTo(world, checkPos)) {
                        validPositions.add(checkPos);
                    }
                }
            }
        }

        return validPositions;
    }

    private List<BlockPos> getValidGrassPositions(LevelReader world, BlockPos center) {
        List<BlockPos> validPositions = new ArrayList<>();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = -1; y <= 2; y++) {
                    BlockPos checkPos = center.offset(x, y, z);

                    if (canSpawnGrassAt(world, checkPos)) {
                        validPositions.add(checkPos);
                    }
                }
            }
        }

        return validPositions;
    }

    private boolean canSpawnGrassAt(LevelReader world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        BlockState belowState = world.getBlockState(pos.below());

        return state.isAir() && belowState.is(this);
    }

    private boolean canSpreadTo(LevelReader world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);

        if (!state.is(BlockTags.MOSS_REPLACEABLE)) {
            return false;
        }

        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = pos.relative(direction);
            BlockState adjacentState = world.getBlockState(adjacentPos);

            if (adjacentState.isFaceSturdy(world, adjacentPos, direction.getOpposite())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return false;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
    }
}