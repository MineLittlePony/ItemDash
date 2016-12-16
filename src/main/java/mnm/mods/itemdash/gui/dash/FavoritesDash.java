package mnm.mods.itemdash.gui.dash;

import mnm.mods.itemdash.Favorites;

public class FavoritesDash extends MainDash {

    private final Favorites favorites;

    public FavoritesDash(ItemDash itemdash, Favorites favorites) {
        super(itemdash, favorites.getItems());
        this.favorites = favorites;
    }

    @Override
    protected void favoriteItem() {
        super.favoriteItem();
        this.updateItems(favorites.getItems());
    }

}
