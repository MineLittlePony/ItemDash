package mnm.mods.itemdash.gui.dash;

import mnm.mods.itemdash.Favorites;
import mnm.mods.itemdash.gui.ItemDash;
import net.minecraft.item.ItemStack;

public class FavoritesDash extends MainDash {

    private final Favorites favorites;

    public FavoritesDash(ItemDash itemdash, Favorites favorites) {
        super(itemdash, favorites.getItems());
        this.favorites = favorites;
    }

    @Override
    protected void favoriteItem(ItemStack stack) {
        super.favoriteItem(stack);
        this.updateItems(favorites.getItems());
    }

}
