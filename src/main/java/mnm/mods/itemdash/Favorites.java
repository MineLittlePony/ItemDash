package mnm.mods.itemdash;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class Favorites {

    private static final String NAME = "favorites";

    private Set<ItemWrapper> items = Sets.newHashSet();

    public void add(ItemStack item) {
        this.items.add(new ItemWrapper(item));
    }

    public boolean has(ItemStack item) {
        return this.items.contains(new ItemWrapper(item));
    }

    public void remove(ItemStack item) {
        this.items.remove(new ItemWrapper(item));
    }

    public Collection<ItemStack> getItems() {
        return this.items.stream()
                .map(ItemWrapper::getItem)
                .collect(Collectors.toList());
    }

    public void writeToNbt(NBTTagCompound tag) {
        NBTTagList list = new NBTTagList();
        for (ItemStack item : getItems()) {
            NBTTagCompound ntc = new NBTTagCompound();
            item.writeToNBT(ntc);
            list.appendTag(ntc);
        }
        tag.setTag(NAME, list);
    }

    public void readFromNbt(NBTTagCompound tag) {
        this.items.clear();
        NBTTagList nbtlist = tag.getTagList(NAME, 10);
        for (int i = 0; i < nbtlist.tagCount(); i++) {
            NBTTagCompound item = nbtlist.getCompoundTagAt(i);
            ItemStack stack = ItemStack.loadItemStackFromNBT(item);
            add(stack);
        }
    }

    private static class ItemWrapper {

        private final ItemStack item;

        public ItemWrapper(ItemStack item) {
            this.item = item;
        }

        @Override
        public int hashCode() {
            int result = 6;
            result = 37 * result + Item.REGISTRY.getNameForObject(this.item.getItem()).hashCode();
            result = 37 * result + this.item.getMetadata();
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (obj == this)
                return true;
            if (obj instanceof ItemStack)
                return equals((ItemStack) obj);
            if (obj instanceof ItemWrapper)
                return equals(((ItemWrapper) obj).getItem());

            return false;

        }

        private boolean equals(ItemStack stack) {
            if (this.item == stack || this.item.equals(stack))
                return true;

            Item item1 = stack.getItem();
            int meta1 = stack.getMetadata();

            Item item2 = this.item.getItem();
            int meta2 = this.item.getMetadata();

            return item1 == item2 && meta1 == meta2;
        }

        ItemStack getItem() {
            return this.item;
        }
    }
}
