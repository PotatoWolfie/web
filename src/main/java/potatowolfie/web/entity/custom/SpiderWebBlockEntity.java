package potatowolfie.web.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import potatowolfie.web.block.WebBlockEntities;

public class SpiderWebBlockEntity extends BlockEntity {

    public SpiderWebBlockEntity(BlockPos pos, BlockState state) {
        super(WebBlockEntities.SPIDER_WEB_BLOCK_ENTITY, pos, state);
    }
}