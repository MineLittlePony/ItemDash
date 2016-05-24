package mnm.mods.itemdash;

import java.util.function.Predicate;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public final class ItemFilters {

    private ItemFilters() {}

    public static Predicate<ItemStack> nameContains(String search) {
        final String query = search.toLowerCase().trim();
        return (item) -> {
            String id = Item.REGISTRY.getNameForObject(item.getItem()).toString();
            String name = item.getDisplayName().toLowerCase();
            return name.contains(query) || id.contains(query);
        };
    }
}
