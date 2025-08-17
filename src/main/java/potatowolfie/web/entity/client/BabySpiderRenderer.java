package potatowolfie.web.entity.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.util.Identifier;
import potatowolfie.web.entity.custom.BabySpiderEntity;

@Environment(EnvType.CLIENT)
public class BabySpiderRenderer extends MobEntityRenderer<BabySpiderEntity, LivingEntityRenderState, BabySpiderModel> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/spider/spider.png");

    public BabySpiderRenderer(EntityRendererFactory.Context context) {
        super(context, new BabySpiderModel(context.getPart(WebEntityModelLayers.BABY_SPIDER)), 0.4F);
    }

    @Override
    protected float getLyingPositionRotationDegrees() {
        return 180.0F;
    }

    @Override
    public Identifier getTexture(LivingEntityRenderState state) {
        return TEXTURE;
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public void updateRenderState(BabySpiderEntity spiderEntity, LivingEntityRenderState livingEntityRenderState, float f) {
        super.updateRenderState(spiderEntity, livingEntityRenderState, f);
    }
}