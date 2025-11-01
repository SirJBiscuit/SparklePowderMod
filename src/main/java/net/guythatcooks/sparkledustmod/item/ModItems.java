// Classes naming since everyother word is capital
// If classes are green means they haven't been commit yet

// Deffered Register is how items work


package net.guythatcooks.sparkledustmod.item;

import net.guythatcooks.sparkledustmod.SparkleDustMod;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// Deffered Registery is a list of items that sit in limbo and when you want them they are there.
// And you tell it to shove it inside of your modid

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SparkleDustMod.MODID);
    // () -> is a supplier this supplies the deffered register with a list of items to the new item with item properties
    // with just  this command its in the game, but its missing a texture, en_us.lang name, its not in a creativetab, it has no recipe

    public static final DeferredItem<Item> SPARKLEDUST = ITEMS.register("sparkledust",
            () -> new CustomSparkleEffect(new Item.Properties()
                    .stacksTo(16)
                    .rarity(Rarity.RARE)
                    .fireResistant()
            )
    );

    public static final DeferredItem<Item> GUIDE_CRYSTAL = ITEMS.register("guidecrystal",
            () -> new Item(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)
                    .fireResistant()
            )
    );

    public static final DeferredItem<Item> BOWTIE = ITEMS.register("bowtie",
            () -> new Item(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.RARE)
                    .fireResistant()
            )
    );

    public static final DeferredItem<Item> SPARKLE_WAND = ITEMS.register("sparkle_wand",
            () -> new CustomWandEffect(new Item.Properties()
                    .stacksTo(1)
                    .durability(200)
                    .rarity(Rarity.RARE)
                    .fireResistant()
            )
    );

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);

    }
}
