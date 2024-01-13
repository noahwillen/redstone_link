package net.noah.redstoneplus;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(RedstonePlus.MOD_ID)
public class RedstonePlus
{
    public static final String MOD_ID = "redstoneplus";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MOD_ID);

    public static final RegistryObject<Block> REDSTONE_LINK_BLOCK = BLOCKS.register("redstone_link", () -> new RedstoneLinkBlock(BlockBehaviour.Properties.copy(Blocks.REPEATER).noOcclusion()));
    public static final RegistryObject<Item> REDSTONE_LINK_BLOCK_ITEM = ITEMS.register("redstone_link", () -> new BlockItem(REDSTONE_LINK_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<RedstoneLinkEntity>> REDSTONE_LINK_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("redstone_link_be", () ->
                    BlockEntityType.Builder.of(RedstoneLinkEntity::new,
                            REDSTONE_LINK_BLOCK.get()).build(null));

    public static final RegistryObject<Block> ENDER_WATCHER_BLOCK = BLOCKS.register("ender_watcher", () -> new EnderWatcherBlock(BlockBehaviour.Properties.copy(Blocks.DISPENSER).strength(3.5f, 17.5f)));
    public static final RegistryObject<Item> ENDER_WATCHER_BLOCK_ITEM = ITEMS.register("ender_watcher", () -> new BlockItem(ENDER_WATCHER_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<EnderWatcherBlockEntity>> ENDER_WATCHER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("ender_watcher_be", () ->
                    BlockEntityType.Builder.of(EnderWatcherBlockEntity::new,
                            ENDER_WATCHER_BLOCK.get()).build(null));


    public RedstonePlus()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        MinecraftForge.EVENT_BUS.register(this);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
    }
}
