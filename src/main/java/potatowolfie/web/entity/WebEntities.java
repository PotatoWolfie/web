package potatowolfie.web.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import potatowolfie.web.Web;
import potatowolfie.web.entity.abominable_spider_queen.AbominableSpiderQueenEntity;
import potatowolfie.web.entity.baby_spider.BabySpiderEntity;
import potatowolfie.web.entity.spider_web.PoisonSpiderWebEntity;
import potatowolfie.web.entity.spider_web.SpiderWebEntity;
import potatowolfie.web.entity.spider_web_projectile.PoisonSpiderWebProjectileEntity;
import potatowolfie.web.entity.spider_web_projectile.SpiderWebProjectileEntity;

public class WebEntities {

    public static final EntityType<SpiderWebEntity> SPIDER_WEB = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Web.MOD_ID, "spider_web"),
            EntityType.Builder.<SpiderWebEntity>of(SpiderWebEntity::new, MobCategory.MISC)
                    .sized(2.5F, 0.9F)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Web.MOD_ID, "spider_web"))));

    public static final EntityType<PoisonSpiderWebEntity> POISON_SPIDER_WEB = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Web.MOD_ID, "poison_spider_web"),
            EntityType.Builder.<PoisonSpiderWebEntity>of(PoisonSpiderWebEntity::new, MobCategory.MISC)
                    .sized(2.5F, 0.9F)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Web.MOD_ID, "poison_spider_web"))));

    public static final EntityType<SpiderWebProjectileEntity> SPIDER_WEB_FLYING = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Web.MOD_ID, "spider_web_flying"),
            EntityType.Builder.<SpiderWebProjectileEntity>of(SpiderWebProjectileEntity::new, MobCategory.MISC)
                    .sized(0.3125F, 0.3125F)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Web.MOD_ID, "spider_web_flying"))));

    public static final EntityType<PoisonSpiderWebProjectileEntity> POISON_SPIDER_WEB_FLYING = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Web.MOD_ID, "poison_spider_web_flying"),
            EntityType.Builder.<PoisonSpiderWebProjectileEntity>of(PoisonSpiderWebProjectileEntity::new, MobCategory.MISC)
                    .sized(0.3125F, 0.3125F)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Web.MOD_ID, "poison_spider_web_flying"))));

    public static final EntityType<BabySpiderEntity> BABY_SPIDER = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Web.MOD_ID, "baby_spider"),
            EntityType.Builder.of(BabySpiderEntity::new, MobCategory.MONSTER)
                    .sized(0.9F, 0.6125F).clientTrackingRange(32)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Web.MOD_ID, "baby_spider"))));

    public static final EntityType<AbominableSpiderQueenEntity> ABOMINABLE_SPIDER_QUEEN = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Web.MOD_ID, "abominable_spider_queen"),
            EntityType.Builder.of(AbominableSpiderQueenEntity::new, MobCategory.MONSTER)
                    .sized(1.8F, 1.9F).eyeHeight(0.65F).clientTrackingRange(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Web.MOD_ID, "abominable_spider_queen"))));

    public static void registerModEntities() {
        Web.LOGGER.info("Registering Mod Entities for " + Web.MOD_ID);

        FabricDefaultAttributeRegistry.register(
                WebEntities.ABOMINABLE_SPIDER_QUEEN,
                AbominableSpiderQueenEntity.createQueenAttributes());
    }
}