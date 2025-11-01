package net.guythatcooks.sparklemod;

import org.slf4j.Logger;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.LoggerFactory;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
// This part here @Mod(MatchYourModId.MODID) tells neoforge this is your main mod class
@Mod(SparkleMod.MODID)
public class SparkleMod {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "gtcsparklemod";
    // Directly reference a slf4j logger
    //public static final Logger LOGGER = LogUtils.getLogger();
    public static final Logger LOGGER = LoggerFactory.getLogger(SparkleMod.class);
    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.

    // # This is a Constructor, it is called when the mod is loaded
    // The Eventbus is how your mod communicates with neoforge. you register listeners for various events
     // Mod Loading Events
     // Registry Events - items,blocks ect
     // Game Events - player interactions, world events ect
     // Lifecycle Event: FMLCommonSetupEvent - runs on client and server mod loading
     // - FMLClientSetupEvent - runs only on the client
     // - FMLDedicatedServerSetupEvent - runs only on dedicated servers

    public SparkleMod(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);
        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);
        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
    // CommonSetup - runs on both client and server
    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Sparklemod: Common setup complete!");
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

}
