package mnm.mods.itemdash;

import com.google.common.base.Predicate;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public final class ItemFilters {

    private ItemFilters() {}

    public static Predicate<ItemStack> nameContains(String search) {
        final String query = search.toLowerCase().trim();
        return (it) -> {
            String id = Item.itemRegistry.getNameForObject(it.getItem()).toString();
            String name = it.getDisplayName().toLowerCase();
            return name.contains(query) || id.contains(query);
        };
    }
}
