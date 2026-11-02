package potatowolfie.web.entity.spider_web_block;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.Direction;

public class SpiderWebBlockRenderState extends EntityRenderState {
    public boolean shouldRender;
    public Direction facing = Direction.UP;
}