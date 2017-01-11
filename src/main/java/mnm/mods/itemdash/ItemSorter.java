package mnm.mods.itemdash;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;

public enum ItemSorter {

    DEFAULT(Comparator.comparingInt(a -> Item.getIdFromItem(a.getItem()))),
    BY_ID(comparingStringIgnoreCase(a -> Objects.toString(Item.REGISTRY.getNameForObject(a.getItem())))),
    BY_NAME(comparingStringIgnoreCase(ItemStack::getDisplayName));

    private final Comparator<ItemStack> sort;

    ItemSorter(final Comparator<ItemStack> sort) {
        this.sort = sort.thenComparingInt(ItemStack::getMetadata);
    }

    public Comparator<ItemStack> getSort() {
        return this.sort;
    }

    private static <T> Comparator<T> comparingStringIgnoreCase(Function<T, String> func) {
        Objects.requireNonNull(func);
        return (Comparator<T> & Serializable)
                (c1, c2) -> func.apply(c1).compareToIgnoreCase(func.apply(c2));
    }

}
