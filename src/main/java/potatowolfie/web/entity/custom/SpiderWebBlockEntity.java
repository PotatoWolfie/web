package potatowolfie.web.entity.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import potatowolfie.web.block.WebBlockEntities;

public class SpiderWebBlockEntity extends BlockEntity {

    public SpiderWebBlockEntity(BlockPos pos, BlockState state) {
        super(WebBlockEntities.SPIDER_WEB_BLOCK_ENTITY, pos, state);
    }
}