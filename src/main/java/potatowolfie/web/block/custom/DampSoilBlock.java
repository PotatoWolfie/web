package potatowolfie.web.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import potatowolfie.web.block.WebBlocks;

public class DampSoilBlock extends Block implements BonemealableBlock {
    private static final int ATTEMPT_COUNT = 128;

    public DampSoilBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.is(ItemTags.SHOVELS)) {
            if (canPlacePathAt(world, pos)) {
                if (!world.isClientSide()) {
                    world.setBlock(pos, WebBlocks.DAMP_SOIL_PATH.defaultBlockState(), 3);
                    world.playSound(null, pos, SoundEvents.SHOVEL_FLATTEN.value(), SoundSource.BLOCKS, 1.0f, 1.0f);

                    EquipmentSlot slot = (hand == InteractionHand.MAIN_HAND) ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                    stack.hurtAndBreak(1, player, slot);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    private boolean canPlacePathAt(Level world, BlockPos pos) {
        BlockState blockStateAbove = world.getBlockState(pos.above());
        return blockStateAbove.isAir() || blockStateAbove.getBlock() instanceof FenceGateBlock;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, BonemealSource source) {
        return level.getBlockState(pos.above()).isAir() && level.isInsideBuildHeight(pos.above());
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        BlockPos above = pos.above();

        attemptLoop:
        for (int attempt = 0; attempt < ATTEMPT_COUNT; attempt++) {
            BlockPos testPos = above;
            int randomizeCount = attempt / 16;

            for (int i = 0; i < randomizeCount; i++) {
                int dx = random.nextIntBetweenInclusive(-1, 1);
                int dy = random.nextIntBetweenInclusive(-1, 1) * random.nextInt(3) / 2;
                int dz = random.nextIntBetweenInclusive(-1, 1);
                testPos = testPos.offset(dx, dy, dz);

                if (stopBonemealSpread(level, testPos)) {
                    continue attemptLoop;
                }
            }

            if (level.getBlockState(testPos).isAir() && !level.isOutsideBuildHeight(testPos)
                    && random.nextFloat() < 0.25f) {
                level.setBlockAndUpdate(testPos, WebBlocks.SPIDER_GRASS.defaultBlockState());
            }
        }
    }

    private boolean stopBonemealSpread(ServerLevel level, BlockPos testPos) {
        return !level.getBlockState(testPos.below()).is(this)
                || level.getBlockState(testPos).isCollisionShapeFullBlock(level, testPos);
    }

    @Override
    public BonemealableBlock.Type getType() {
        return BonemealableBlock.Type.NEIGHBOR_SPREADER;
    }
}