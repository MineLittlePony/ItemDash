package mnm.mods.itemdash;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Comparator;

public enum ItemSorter {

    BY_ID((a, b) -> {
        String aId = Item.REGISTRY.getNameForObject(a.getItem()).toString();
        String bId = Item.REGISTRY.getNameForObject(b.getItem()).toString();
        return aId.compareToIgnoreCase(bId);
    }),
    DEFAULT(Comparator.comparingInt(a -> Item.getIdFromItem(a.getItem()))),
    BY_NAME((a, b) -> a.getDisplayName().compareToIgnoreCase(b.getDisplayName()));

    private final Comparator<ItemStack> sort;

    private ItemSorter(final Comparator<ItemStack> sort) {
        this.sort = sort;
    }

    public Comparator<ItemStack> getSort() {
        return (a, b) -> {
            // wrap the sorter and include metadata
            int i = sort.compare(a, b);
            if (i == 0) {
                i = a.getMetadata() - b.getMetadata();
            }
            return i;
        };
    }
}
