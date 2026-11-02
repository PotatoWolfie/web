package potatowolfie.web.item.custom;

import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.phys.Vec3;
import potatowolfie.web.Web;

public class FangDaggerItem extends Item {

    private static final double BACKSTAB_DOT_THRESHOLD = -0.5D;

    private final int poisonDurationTicks;
    private final float backstabTotalDamage;

    public FangDaggerItem(Item.Properties properties, int poisonDurationTicks, float backstabTotalDamage) {
        super(properties);
        this.poisonDurationTicks = poisonDurationTicks;
        this.backstabTotalDamage = backstabTotalDamage;
    }

    public static Item.Properties createProperties(ToolMaterial material, float attackDamageBaseline,
                                                   float attackSpeedBaseline, float reachReduction) {
        Identifier reachModifierId = Identifier.fromNamespaceAndPath(Web.MOD_ID, "dagger_reach");

        return new Item.Properties()
                .durability(material.durability())
                .repairable(material.repairItems())
                .enchantable(material.enchantmentValue())
                .attributes(ItemAttributeModifiers.builder()
                        .add(Attributes.ATTACK_DAMAGE,
                                new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID,
                                        attackDamageBaseline + material.attackDamageBonus(),
                                        AttributeModifier.Operation.ADD_VALUE),
                                EquipmentSlotGroup.MAINHAND)
                        .add(Attributes.ATTACK_SPEED,
                                new AttributeModifier(Item.BASE_ATTACK_SPEED_ID,
                                        attackSpeedBaseline,
                                        AttributeModifier.Operation.ADD_VALUE),
                                EquipmentSlotGroup.MAINHAND)
                        .add(Attributes.ENTITY_INTERACTION_RANGE,
                                new AttributeModifier(reachModifierId,
                                        -reachReduction,
                                        AttributeModifier.Operation.ADD_VALUE),
                                EquipmentSlotGroup.MAINHAND)
                        .add(Attributes.BLOCK_INTERACTION_RANGE,
                                new AttributeModifier(reachModifierId,
                                        -reachReduction,
                                        AttributeModifier.Operation.ADD_VALUE),
                                EquipmentSlotGroup.MAINHAND)
                        .build());
    }

    @Override
    public void hurtEnemy(ItemStack itemStack, LivingEntity mob, LivingEntity attacker) {
        mob.addEffect(new MobEffectInstance(MobEffects.POISON, this.poisonDurationTicks, 0));
        super.hurtEnemy(itemStack, mob, attacker);
    }

    @Override
    public float getAttackDamageBonus(Entity victim, float damage, DamageSource damageSource) {
        float bonus = super.getAttackDamageBonus(victim, damage, damageSource);
        if (victim instanceof LivingEntity livingVictim && isBackstab(livingVictim, damageSource)) {
            bonus += Math.max(0.0F, this.backstabTotalDamage - damage);
        }
        return bonus;
    }

    private boolean isBackstab(LivingEntity victim, DamageSource damageSource) {
        Entity attackerEntity = damageSource.getEntity();
        if (!(attackerEntity instanceof LivingEntity attacker)) {
            return false;
        }

        Vec3 toAttacker = attacker.position().subtract(victim.position());
        if (toAttacker.lengthSqr() < 1.0E-4) {
            return false;
        }

        double dot = victim.getLookAngle().normalize().dot(toAttacker.normalize());
        return dot < BACKSTAB_DOT_THRESHOLD;
    }
}