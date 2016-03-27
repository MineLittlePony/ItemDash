package mnm.mods.itemdash;

import java.util.Comparator;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public enum ItemSorter {

    BY_ID((a, b) -> {
        String aId = Item.itemRegistry.getNameForObject(a.getItem()).toString();
        String bId = Item.itemRegistry.getNameForObject(b.getItem()).toString();
        return aId.compareToIgnoreCase(bId);
    }),
    DEFAULT((a, b) -> Item.getIdFromItem(a.getItem()) - Item.getIdFromItem(b.getItem())),
    BY_NAME((a, b) -> a.getDisplayName().compareToIgnoreCase(b.getDisplayName()));

    private final Comparator<ItemStack> sort;

    private ItemSorter(final Comparator<ItemStack> sort) {
        this.sort = sort;
    }

    public Comparator<ItemStack> getSort() {
        return sort;
    }
}
