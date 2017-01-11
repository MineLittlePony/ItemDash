package mnm.mods.itemdash.gui;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import mnm.mods.itemdash.Favorites;
import mnm.mods.itemdash.LiteModItemDash;
import mnm.mods.itemdash.ducks.IGuiContainer;
import mnm.mods.itemdash.easing.EasingType;
import mnm.mods.itemdash.easing.EasingsFactory;
import mnm.mods.itemdash.gui.dash.CustomizeDash;
import mnm.mods.itemdash.gui.dash.Dash;
import mnm.mods.itemdash.gui.dash.DashSettings;
import mnm.mods.itemdash.gui.dash.FavoritesDash;
import mnm.mods.itemdash.gui.dash.MainDash;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemDash extends DashElement {

    public static final int DASH_ICON_W = 18;

    private enum Tabs {
        TOGGLE,
        ITEMS,
        FAVORITES,
//        CUSTOMIZE,
        SETTINGS
    }

    private final NonNullList<ItemStack> items;

    private Minecraft mc = Minecraft.getMinecraft();

    private List<SideTab> tabs = Lists.newArrayList();

    public int xPos;
    public int yPos;
    public int width;
    public int height;

    @Nonnull
    public Dash currentDash;
    @Nonnull
    private final Favorites favorites;
    @Nullable
    private CustomizeDash customizeDash;

    private int toggleTimer;

    public boolean dirty = true;

    public ItemDash(final Set<String> ignored, Favorites favorites) {
        final NonNullList<ItemStack> list = NonNullList.create();
        for (Item it : Item.REGISTRY)
            it.getSubItems(it, null, list);
        Function<Item, ResourceLocation> namer = Item.REGISTRY::getNameForObject;
        this.items = list.stream()
                .filter(it -> !ignored.contains(namer.apply(it.getItem()).toString()))
                .collect(Collectors.toCollection(NonNullList::create));
        this.favorites = favorites;
        int i = 0;
        // enable
        this.tabs.add(new SideTab(Tabs.TOGGLE.ordinal(), i++, 0, 20, false, this) {
            @Override
            public void drawTab() {
                this.texU = isVisible() ? 0 : 20;
                super.drawTab();
            }
        });
        // main
        SideTab items = new SideTab(Tabs.ITEMS.ordinal(), i++, 80, 40, true, this);
        items.active = true;
        this.tabs.add(items);
        // favorites
        this.tabs.add(new SideTab(Tabs.FAVORITES.ordinal(), i++, 40, 40, true, this));
//        this.tabs.add(new SideTab(Tabs.CUSTOMIZE.ordinal(), i++, 60, 40, true, this));
        // TODO potions
        // settings
        this.tabs.add(new SideTab(Tabs.SETTINGS.ordinal(), i++, 20, 40, true, this));

        this.currentDash = new MainDash(this, this.items);
    }

    public void setCurrentDash(@Nonnull Dash dash) {
        this.currentDash.onClose();

        this.currentDash = dash;
    }

    private void onTabActivated(@Nonnull SideTab tab) {

        Preconditions.checkPositionIndex(tab.id, Tabs.values().length, "Tab is out of bounds. Expected 0-" + Tabs.values().length + " but found " + tab.id);
        Tabs tabs = Tabs.values()[tab.id];
        // check if the tab is active right now.
        if (tab.active) {
            if (isVisible())
                doSearch();
            else
                setVisible(true);
            return;
        }
        boolean open = true;
        switch (tabs) {
            case TOGGLE:
                open = !this.isVisible();
                break;
            case ITEMS:
                setCurrentDash(new MainDash(this, this.items));
                break;
            case SETTINGS:
                setCurrentDash(new DashSettings(this));
                break;
            case FAVORITES:
                setCurrentDash(new FavoritesDash(this, this.favorites));
                break;
//            case CUSTOMIZE:
//                if (customizeDash != null && customizeDash.isWorking())
//                    setCurrentDash(customizeDash);
//                else
//                    setCurrentDash(customizeDash = new CustomizeDash(this, ItemStack.EMPTY));
//                break;

        }
        if (open != this.isVisible()) {
            this.setVisible(open);
        }

        if (tab.activatable) {

            for (SideTab sideTab : this.tabs) {
                sideTab.active = false;
            }
            tab.active = true;
        }
    }

    public void setVisible(boolean visible) {
        if (isVisible() == visible)
            return;
        LiteModItemDash.getInstance().setVisible(visible);
        toggleTimer = mc.ingameGUI.getUpdateCounter();
        LiteModItemDash.getInstance().saveConfig();
    }

    private boolean isVisible() {
        return LiteModItemDash.getInstance().isVisible();
    }

    public boolean isFocused() {
        return this.currentDash.isFocused();
    }

    public Favorites getFavorites() {
        return favorites;
    }

    public void onTick() {
        this.currentDash.onTick();
    }

    public void updateDash(GuiContainer screen) {

        this.checkForGuiChanges(screen);
        int guiWidth = ((IGuiContainer) screen).getXSize();
        int newLeft = xPos / 2 - guiWidth / 2;
        if (!mc.player.getActivePotionEffects().isEmpty()) {
            newLeft += 50;
        }
        ((IGuiContainer) screen).setGuiLeft(newLeft);

    }

    private void checkForGuiChanges(GuiContainer cont) {
        int yPos = 0;
        int width = (int) ((cont.width - ((IGuiContainer) cont).getXSize()) / (3f / 2f));
        width = (width / DASH_ICON_W) * DASH_ICON_W;
        int height = cont.height;
        int xPos;
        int tick = mc.ingameGUI.getUpdateCounter() - this.toggleTimer;
        EasingType easing = EasingsFactory.getInstance().quadratic();
        final float time = 10;
        if (!isVisible()) {
            xPos = cont.width - width;
            if (tick < time)
                xPos = (int) easing.in().ease(tick, xPos, width, time);
            else
                xPos = cont.width;

        } else {
            xPos = cont.width;
            if (tick < time)
                xPos = (int) easing.in().ease(tick, xPos, -width, time);
            else
                xPos = cont.width - width;
        }

        if (this.xPos != xPos || this.yPos != yPos || this.width != width || this.height != height) {
            this.dirty |= width != this.width || height != this.height;
            this.xPos = xPos;
            this.yPos = yPos;
            this.width = width - 15;
            this.height = height;
            this.currentDash.update(xPos, yPos, width, height);
        }
    }

    public void preRender(int mousex, int mousey) {

        GlStateManager.enableAlpha();
        drawBackground();
        this.currentDash.preRender(mousex, mousey);

    }

    public void postRender(int mousex, int mousey) {

        this.currentDash.postRender(mousex, mousey);

    }

    private void drawBackground() {
        this.zLevel = 300;
        mc.getTextureManager().bindTexture(BG);
        GlStateManager.color(1, 1, 1);
        this.drawBorders(xPos - 2, yPos, width + 17, height, 0, 0, 18, 18,
                TOP | LEFT | BOTTOM | BOTTOM_LEFT);

        this.tabs.forEach(SideTab::drawTab);

        this.zLevel = 0;

    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        this.currentDash.mouseClicked(mouseX, mouseY, mouseButton);
        for (SideTab tab : tabs) {
            if (tab.mouseClicked(mouseX, mouseY)) {
                this.onTabActivated(tab);
                break;
            }
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        this.currentDash.mouseReleased(mouseX, mouseY, mouseButton);
    }

    public void mouseClickMove(int x, int y) {
        this.currentDash.mouseClickMove(x, y);
    }

    private void doSearch() {
        if (this.currentDash instanceof MainDash)
            ((MainDash) this.currentDash).doSearch();
    }

    public void keyTyped(char key, int code) {
        if (!currentDash.isFocused()) {
            if (code == Keyboard.KEY_O) {
                setVisible(!isVisible());
            }
        }
        this.currentDash.keyTyped(key, code);

    }

    public void scroll(int i) {
        this.currentDash.scroll(i);
    }
}
