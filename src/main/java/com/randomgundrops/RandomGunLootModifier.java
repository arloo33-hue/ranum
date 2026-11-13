package com.randomgundrops;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

/** Replaces the drops of broken blocks with a random TACZ gun. Chests and mobs are left alone. */
public class RandomGunLootModifier extends LootModifier {
    public static final Supplier<Codec<RandomGunLootModifier>> CODEC = Suppliers.memoize(
            () -> RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, RandomGunLootModifier::new)));

    public RandomGunLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @NotNull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        if (!Config.ENABLED.get() || loot.isEmpty()) return loot;
        BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        if (state == null) return loot;                       // not a block drop

        if (context.getRandom().nextInt(100) >= Config.CHANCE.get()) return loot;

        List<ResourceLocation> guns = TaczCompat.gunIds();
        if (guns.isEmpty()) return loot;
        Item gunItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz", "modern_kinetic_gun"));
        if (gunItem == null || gunItem == Items.AIR) return loot;

        ResourceLocation gunId;
        if (Config.PER_BLOCK.get()) {
            ResourceLocation blockKey = ForgeRegistries.BLOCKS.getKey(state.getBlock());
            long seed = (blockKey == null ? 0 : blockKey.toString().hashCode()) * 31L + Config.SALT.get();
            gunId = guns.get(new Random(seed).nextInt(guns.size()));
        } else {
            gunId = guns.get(context.getRandom().nextInt(guns.size()));
        }

        ItemStack gun = new ItemStack(gunItem);
        CompoundTag tag = gun.getOrCreateTag();
        tag.putString("GunId", gunId.toString());
        String fireMode = TaczCompat.firstFireMode(gunId);
        if (fireMode != null) tag.putString("GunFireMode", fireMode);

        ObjectArrayList<ItemStack> result = new ObjectArrayList<>();
        result.add(gun);

        if (Config.ADD_AMMO.get()) {
            ResourceLocation ammoId = TaczCompat.ammoFor(gunId);
            if (ammoId == null) {
                List<ResourceLocation> all = TaczCompat.ammoIds();
                if (!all.isEmpty()) ammoId = all.get(context.getRandom().nextInt(all.size()));
            }
            Item ammoItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz", "ammo"));
            if (ammoId != null && ammoItem != null && ammoItem != Items.AIR) {
                ItemStack ammo = new ItemStack(ammoItem, 30);
                ammo.getOrCreateTag().putString("AmmoId", ammoId.toString());
                result.add(ammo);
            }
        }

        if (Config.ADD_ATTACHMENT.get()) {
            List<ResourceLocation> all = TaczCompat.attachmentIds();
            Item attachItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz", "attachment"));
            if (!all.isEmpty() && attachItem != null && attachItem != Items.AIR) {
                ItemStack att = new ItemStack(attachItem);
                att.getOrCreateTag().putString("AttachmentId", all.get(context.getRandom().nextInt(all.size())).toString());
                result.add(att);
            }
        }

        if (Config.KEEP_NORMAL.get()) result.addAll(loot);
        return result;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
