package potatowolfie.web.mixin;

import net.minecraft.entity.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import potatowolfie.web.block.custom.SpiderWebBlock;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void convertBlockToItemSpiderWeb(CallbackInfo ci) {
        ItemEntity itemEntity = (ItemEntity) (Object) this;
        ItemStack stack = itemEntity.getStack();

        if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof SpiderWebBlock spiderWebBlock) {
            ItemStack webstack = spiderWebBlock.getSpiderWebItem();
            webstack.setCount(stack.getCount());

            webstack.applyComponentsFrom(stack.getComponents());

            itemEntity.setStack(webstack);
        }
    }
}