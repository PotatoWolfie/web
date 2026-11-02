package potatowolfie.web.world;

import net.minecraft.resources.Identifier;
import potatowolfie.web.Web;
import potatowolfie.web.world.biome.WebRegion;
import potatowolfie.web.world.biome.WebSurfaceRules;
import terrablender.api.MaterialRuleManager;
import terrablender.api.Regions;
import terrablender.api.TerraBlenderApi;

public class WebTerraBlender implements TerraBlenderApi {
    @Override
    public void onTerraBlenderInitialized() {
        Regions.register(new WebRegion(Identifier.fromNamespaceAndPath(Web.MOD_ID, "overworld"), 2));
        MaterialRuleManager.addRules(MaterialRuleManager.RuleCategory.OVERWORLD,
                Web.MOD_ID, WebSurfaceRules::makeRules);
    }
}