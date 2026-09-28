package com.shiroha23.appchg;

import com.mojang.serialization.MapCodec;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import appeng.block.AEBaseEntityBlock;
import appeng.block.networking.EnergyCellBlock;
import appeng.block.networking.EnergyCellBlockItem;
import appeng.blockentity.networking.EnergyCellBlockEntity;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.ClientTickingBlockEntity;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.api.ids.AEComponents;
import appeng.api.AECapabilities;
import appeng.api.networking.IInWorldGridNodeHost;

@Mod(Appchg.MODID)
public class Appchg {

    public static final String MODID = "appchg";
    private static final BlockEntityTicker<EnergyCellBlockEntity> SERVER_TICKER =
            ServerTickingBlockEntity.class.isAssignableFrom(EnergyCellBlockEntity.class)
                    ? (level, pos, state, entity) -> ((ServerTickingBlockEntity) entity).serverTick()
                    : null;
    private static final BlockEntityTicker<EnergyCellBlockEntity> CLIENT_TICKER =
            ClientTickingBlockEntity.class.isAssignableFrom(EnergyCellBlockEntity.class)
                    ? (level, pos, state, entity) -> ((ClientTickingBlockEntity) entity).clientTick()
                    : null;

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final Supplier<Block> ULTRA_DENSE_ENERGY_CELL = BLOCKS.registerBlock("ultra_dense_energy_cell",
            properties -> new EnergyCellBlock(properties, 12800000, 3200, 12800));
    public static final Supplier<Item> ULTRA_DENSE_ENERGY_CELL_ITEM = ITEMS.registerItem("ultra_dense_energy_cell",
            properties -> new EnergyCellBlockItem(ULTRA_DENSE_ENERGY_CELL.get(), properties.useBlockDescriptionPrefix()));

    public static final Supplier<Block> ULTIMATE_ENERGY_CELL = BLOCKS.registerBlock("ultimate_energy_cell",
            properties -> new EnergyCellBlock(properties, 102400000, 6400, 102400));
    public static final Supplier<Item> ULTIMATE_ENERGY_CELL_ITEM = ITEMS.registerItem("ultimate_energy_cell",
            properties -> new EnergyCellBlockItem(ULTIMATE_ENERGY_CELL.get(), properties.useBlockDescriptionPrefix()));

    public static final Supplier<BlockEntityType<EnergyCellBlockEntity>> ULTRA_DENSE_ENERGY_CELL_BLOCK_ENTITY = registerEnergyCellBlockEntity(
            "ultra_dense_energy_cell", ULTRA_DENSE_ENERGY_CELL, ULTRA_DENSE_ENERGY_CELL_ITEM);
    public static final Supplier<BlockEntityType<EnergyCellBlockEntity>> ULTIMATE_ENERGY_CELL_BLOCK_ENTITY = registerEnergyCellBlockEntity(
            "ultimate_energy_cell", ULTIMATE_ENERGY_CELL, ULTIMATE_ENERGY_CELL_ITEM);

    public static final Supplier<CreativeModeTab> APPCHG_TAB = CREATIVE_MODE_TABS.register("appchg_tab",
            () -> CreativeModeTab.builder()
                .icon(() -> new ItemStack(ULTRA_DENSE_ENERGY_CELL_ITEM.get()))
                .title(Component.translatable("itemGroup.appchg"))
                .displayItems((parameters, output) -> {
                    output.accept(ULTRA_DENSE_ENERGY_CELL_ITEM.get());
                    ItemStack ultraDenseFilled = new ItemStack(ULTRA_DENSE_ENERGY_CELL_ITEM.get());
                    ultraDenseFilled.set(AEComponents.STORED_ENERGY, 1.28E7d);
                    output.accept(ultraDenseFilled);

                    output.accept(ULTIMATE_ENERGY_CELL_ITEM.get());
                    ItemStack ultimateFilled = new ItemStack(ULTIMATE_ENERGY_CELL_ITEM.get());
                    ultimateFilled.set(AEComponents.STORED_ENERGY, 1.024E8d);
                    output.accept(ultimateFilled);
                }).build());

    public Appchg(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
    }

    @SuppressWarnings("unchecked")
    private static Supplier<BlockEntityType<EnergyCellBlockEntity>> registerEnergyCellBlockEntity(
            String id,
                        Supplier<Block> blockSupplier,
                        Supplier<Item> itemSupplier) {
        return BLOCK_ENTITIES.register(id, () -> {
            AtomicReference<BlockEntityType<EnergyCellBlockEntity>> typeHolder = new AtomicReference<>();
            BlockEntityType.BlockEntitySupplier<EnergyCellBlockEntity> supplier = (pos, state) ->
                                        new EnergyCellBlockEntity(typeHolder.get(), pos, state);

            var type = new BlockEntityType<>(supplier, blockSupplier.get());
            typeHolder.set(type);

                        var block = (AEBaseEntityBlock<EnergyCellBlockEntity>) blockSupplier.get();
                        block.setBlockEntity(EnergyCellBlockEntity.class, type, CLIENT_TICKER, SERVER_TICKER);
                        AEBaseBlockEntity.registerBlockEntityItem(type, itemSupplier.get());

            return type;
        });
    }

    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onRegisterItemModelProperties(RegisterRangeSelectItemModelPropertyEvent event) {
            event.register(EnergyFillLevelProperty.ID, EnergyFillLevelProperty.CODEC);
        }
    }

    private static final class EnergyFillLevelProperty implements RangeSelectItemModelProperty {
        private static final Identifier ID = Identifier.parse("appchg:energy_fill_level");
        private static final MapCodec<EnergyFillLevelProperty> CODEC = MapCodec.unit(EnergyFillLevelProperty::new);

        @Override
        public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
            if (stack.getItem() instanceof EnergyCellBlockItem energyCell) {
                double currentPower = energyCell.getAECurrentPower(stack);
                double maxPower = energyCell.getAEMaxPower(stack);
                return (float) (currentPower / maxPower);
            }

            return 0;
        }

        @Override
        public MapCodec<? extends RangeSelectItemModelProperty> type() {
            return CODEC;
        }
    }

        @EventBusSubscriber(modid = MODID)
        public static class CommonModEvents {

                @SubscribeEvent
                public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
                        event.registerBlockEntity(
                                        AECapabilities.IN_WORLD_GRID_NODE_HOST,
                                        ULTRA_DENSE_ENERGY_CELL_BLOCK_ENTITY.get(),
                                        (object, context) -> (IInWorldGridNodeHost) object);
                        event.registerBlockEntity(
                                        AECapabilities.IN_WORLD_GRID_NODE_HOST,
                                        ULTIMATE_ENERGY_CELL_BLOCK_ENTITY.get(),
                                        (object, context) -> (IInWorldGridNodeHost) object);
                }
        }
}

