// Classes naming since everyother word is capital
// If classes are green means they haven't been commit yet

// Deffered Register is how items work

package net.guythatcooks.sparkledustmod.item;

import net.guythatcooks.sparkledustmod.SparkleDustMod;
import net.minecraft.world.item.Item;
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
            () -> new Item(new Item.Properties()));

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
