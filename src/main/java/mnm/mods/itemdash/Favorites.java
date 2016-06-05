package mnm.mods.itemdash;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class Favorites {

    private static final String NAME = "favorites";

    private List<ItemStack> items = Lists.newArrayList();

    public void add(ItemStack item) {
        this.items.add(item);
    }

    public boolean has(ItemStack item) {
        return this.items.contains(item);
    }

    public void remove(ItemStack item) {
        this.items.remove(items);
    }

    public Collection<ItemStack> getItems() {
        return this.items;
    }

    public void writeToNbt(NBTTagCompound tag) {
        NBTTagList list = new NBTTagList();
        for (ItemStack item : this.items) {
            NBTTagCompound ntc = new NBTTagCompound();
            item.writeToNBT(ntc);
            list.appendTag(ntc);
        }
        tag.setTag(NAME, list);
    }

    public void readFromNbt(NBTTagCompound tag) {
        List<ItemStack> list = Lists.newArrayList();
        NBTTagList nbtlist = tag.getTagList(NAME, 10);
        for (int i = 0; i < nbtlist.tagCount(); i++) {
            NBTTagCompound item = nbtlist.getCompoundTagAt(i);
            ItemStack stack = ItemStack.loadItemStackFromNBT(item);
            list.add(stack);
        }
        this.items = list;
    }
}
