package potatowolfie.web.block.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Fertilizable;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.entity.player.PlayerEntity;
import potatowolfie.web.block.WebBlocks;

import java.util.ArrayList;
import java.util.List;

public class SpiderMossBlock extends Block implements Fertilizable {

    public SpiderMossBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (stack.isOf(Items.BONE_MEAL)) {
            if (!world.isClient) {
                if (this.isFertilizable(world, pos, state)) {
                    this.grow((ServerWorld) world, world.random, pos, state);

                    if (!player.getAbilities().creativeMode) {
                        stack.decrement(1);
                    }

                    world.playSound(null, pos, SoundEvents.ITEM_BONE_MEAL_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    ((ServerWorld) world).spawnParticles(ParticleTypes.HAPPY_VILLAGER,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            15, 0.5, 0.5, 0.5, 0.0);
                }
            }
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
        return !getValidSpreadPositions(world, pos).isEmpty() || !getValidGrassPositions(world, pos).isEmpty();
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        List<BlockPos> validSpreadPositions = getValidSpreadPositions(world, pos);
        List<BlockPos> validGrassPositions = getValidGrassPositions(world, pos);

        if (!validSpreadPositions.isEmpty()) {
            int spreadCount = 3 + random.nextInt(6);
            spreadCount = Math.min(spreadCount, validSpreadPositions.size());

            for (int i = 0; i < spreadCount; i++) {
                BlockPos targetPos = validSpreadPositions.get(random.nextInt(validSpreadPositions.size()));
                validSpreadPositions.remove(targetPos);

                world.setBlockState(targetPos, this.getDefaultState());

                world.spawnParticles(ParticleTypes.HAPPY_VILLAGER,
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

                world.setBlockState(targetPos, WebBlocks.SPIDER_GRASS.getDefaultState());

                world.spawnParticles(ParticleTypes.HAPPY_VILLAGER,
                        targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5,
                        5, 0.3, 0.3, 0.3, 0.0);
            }
        }
    }

    private List<BlockPos> getValidSpreadPositions(WorldView world, BlockPos center) {
        List<BlockPos> validPositions = new ArrayList<>();

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }

                    BlockPos checkPos = center.add(x, y, z);

                    if (canSpreadTo(world, checkPos)) {
                        validPositions.add(checkPos);
                    }
                }
            }
        }

        return validPositions;
    }

    private List<BlockPos> getValidGrassPositions(WorldView world, BlockPos center) {
        List<BlockPos> validPositions = new ArrayList<>();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = -1; y <= 2; y++) {
                    BlockPos checkPos = center.add(x, y, z);

                    if (canSpawnGrassAt(world, checkPos)) {
                        validPositions.add(checkPos);
                    }
                }
            }
        }

        return validPositions;
    }

    private boolean canSpawnGrassAt(WorldView world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        BlockState belowState = world.getBlockState(pos.down());

        return state.isAir() && belowState.isOf(this);
    }

    private boolean canSpreadTo(WorldView world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);

        if (!state.isIn(BlockTags.MOSS_REPLACEABLE)) {
            return false;
        }

        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = pos.offset(direction);
            BlockState adjacentState = world.getBlockState(adjacentPos);

            if (adjacentState.isSideSolidFullSquare(world, adjacentPos, direction.getOpposite())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return false;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
    }
}