package potatowolfie.web.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import potatowolfie.web.Web;
import potatowolfie.web.entity.custom.BabySpiderEntity;
import potatowolfie.web.entity.custom.SpiderWebEntity;
import potatowolfie.web.entity.custom.SpiderWebProjectileEntity;

public class WebEntities {

    public static final EntityType<SpiderWebEntity> SPIDER_WEB = Registry.register(Registries.ENTITY_TYPE,
            Identifier.of(Web.MOD_ID, "spider_web"),
            EntityType.Builder.<SpiderWebEntity>create(SpiderWebEntity::new, SpawnGroup.MISC)
                    .dimensions(2.5F, 0.9F)
                    .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(Web.MOD_ID, "spider_web"))));

    public static final EntityType<SpiderWebProjectileEntity> SPIDER_WEB_FLYING = Registry.register(Registries.ENTITY_TYPE,
            Identifier.of(Web.MOD_ID, "spider_web_flying"),
            EntityType.Builder.<SpiderWebProjectileEntity>create(SpiderWebProjectileEntity::new, SpawnGroup.MISC)
                    .dimensions(0.3125F, 0.3125F)
                    .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(Web.MOD_ID, "spider_web_flying"))));

    public static final EntityType<BabySpiderEntity> BABY_SPIDER = Registry.register(Registries.ENTITY_TYPE,
            Identifier.of(Web.MOD_ID, "baby_spider"),
            EntityType.Builder.create(BabySpiderEntity::new, SpawnGroup.MONSTER)
                    .dimensions(0.9F, 0.5125F).maxTrackingRange(32)
                    .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(Web.MOD_ID, "baby_spider"))));

    public static void registerModEntities() {
        Web.LOGGER.info("Registering Mod Entities for " + Web.MOD_ID);
    }
}