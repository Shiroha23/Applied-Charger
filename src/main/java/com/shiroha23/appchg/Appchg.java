package com.shiroha23.appchg;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import java.util.concurrent.atomic.AtomicReference;

import appeng.block.networking.EnergyCellBlock;
import appeng.block.networking.EnergyCellBlockItem;
import appeng.blockentity.networking.EnergyCellBlockEntity;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.init.client.InitItemModelsProperties;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Appchg.MODID)
public class Appchg {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "appchg";
    // Create a Deferred Register to hold Blocks which will all be registered under the "appchg" namespace
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "appchg" namespace
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "appchg" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<Block> ULTRA_DENSE_ENERGY_CELL = BLOCKS.register("ultra_dense_energy_cell",
            () -> new EnergyCellBlock(12800000, 3200, 12800));
    public static final RegistryObject<Item> ULTRA_DENSE_ENERGY_CELL_ITEM = ITEMS.register("ultra_dense_energy_cell",
            () -> new EnergyCellBlockItem(ULTRA_DENSE_ENERGY_CELL.get(), new Item.Properties()));

    public static final RegistryObject<Block> ULTIMATE_ENERGY_CELL = BLOCKS.register("ultimate_energy_cell",
            () -> new EnergyCellBlock(102400000, 6400, 102400));
    public static final RegistryObject<Item> ULTIMATE_ENERGY_CELL_ITEM = ITEMS.register("ultimate_energy_cell",
            () -> new EnergyCellBlockItem(ULTIMATE_ENERGY_CELL.get(), new Item.Properties()));

        // Register a creative mode tab for our items
        public static final RegistryObject<CreativeModeTab> APPCHG_TAB = CREATIVE_MODE_TABS.register("appchg_tab",
            () -> CreativeModeTab.builder()
                .icon(() -> new ItemStack(ULTRA_DENSE_ENERGY_CELL_ITEM.get()))
                .title(Component.translatable("itemGroup.appchg"))
                .displayItems((parameters, output) -> {
                output.accept(ULTRA_DENSE_ENERGY_CELL_ITEM.get());
                output.accept(ULTIMATE_ENERGY_CELL_ITEM.get());
                }).build());

    public static final RegistryObject<BlockEntityType<EnergyCellBlockEntity>> ENERGY_CELL_BLOCK_ENTITY = BLOCK_ENTITIES.register(
            "energy_cell",
            () -> {
            AtomicReference<BlockEntityType<EnergyCellBlockEntity>> typeHolder = new AtomicReference<>();
            var supplier = (BlockEntityType.BlockEntitySupplier<EnergyCellBlockEntity>)
                (pos, state) -> new EnergyCellBlockEntity(typeHolder.get(), pos, state);
            var type = BlockEntityType.Builder.of(
                supplier,
                ULTRA_DENSE_ENERGY_CELL.get(),
                ULTIMATE_ENERGY_CELL.get())
                .build(null);
            typeHolder.set(type);
            return type;
            });

    public Appchg() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            var type = ENERGY_CELL_BLOCK_ENTITY.get();
            ((EnergyCellBlock) ULTRA_DENSE_ENERGY_CELL.get()).setBlockEntity(EnergyCellBlockEntity.class, type, null, null);
            ((EnergyCellBlock) ULTIMATE_ENERGY_CELL.get()).setBlockEntity(EnergyCellBlockEntity.class, type, null, null);
            AEBaseBlockEntity.registerBlockEntityItem(type, ULTRA_DENSE_ENERGY_CELL_ITEM.get());
        });
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            ItemProperties.register(ULTRA_DENSE_ENERGY_CELL_ITEM.get(), InitItemModelsProperties.ENERGY_FILL_LEVEL_ID,
                    (stack, level, entity, seed) -> {
                        var energyCell = (EnergyCellBlockItem) ULTRA_DENSE_ENERGY_CELL_ITEM.get();
                        double curPower = energyCell.getAECurrentPower(stack);
                        double maxPower = energyCell.getAEMaxPower(stack);
                        return (float) (curPower / maxPower);
                    });
            ItemProperties.register(ULTIMATE_ENERGY_CELL_ITEM.get(), InitItemModelsProperties.ENERGY_FILL_LEVEL_ID,
                    (stack, level, entity, seed) -> {
                        var energyCell = (EnergyCellBlockItem) ULTIMATE_ENERGY_CELL_ITEM.get();
                        double curPower = energyCell.getAECurrentPower(stack);
                        double maxPower = energyCell.getAEMaxPower(stack);
                        return (float) (curPower / maxPower);
                    });
        }
    }
}

