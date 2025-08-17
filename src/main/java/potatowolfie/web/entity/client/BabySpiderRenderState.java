package potatowolfie.web.entity.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;

@Environment(EnvType.CLIENT)
public class BabySpiderRenderState extends LivingEntityRenderState {
    public float headYaw;
    public float pitch;
    public float limbSwingAnimationProgress;
    public float limbSwingAmplitude;
    public boolean climbing;
}