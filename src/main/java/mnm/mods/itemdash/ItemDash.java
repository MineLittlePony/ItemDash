package mnm.mods.itemdash;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.lwjgl.input.Keyboard;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import mnm.mods.itemdash.ducks.IGuiContainer;
import mnm.mods.itemdash.easing.EasingType;
import mnm.mods.itemdash.easing.EasingsFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemDash extends DashElement {

    public static final int DASH_ICON_W = 18;

    private static enum Tabs {
        TOGGLE,
        ITEMS,
        FAVORITES,
        SETTINGS,
        SEARCH
    }

    private final Set<ItemStack> items;

    private Minecraft mc = Minecraft.getMinecraft();

    private List<SideTab> tabs = Lists.newArrayList();

    public int xPos;
    public int yPos;
    public int width;
    public int height;

    @Nonnull
    public Dash currentDash;
    private Favorites favorites;

    // tabs
    private SideTab favoritesTab;
    private SideTab searchTab;

    private int toggleTimer;

    public boolean dirty = true;

    public ItemDash(final Set<String> ignored, Favorites favorites) {
        List<ItemStack> list = Lists.newArrayList();
        Item.REGISTRY.forEach((Item it) -> it.getSubItems(it, null, list));
        Function<Item, String> namer = it -> Item.REGISTRY.getNameForObject(it).toString();
        this.items = list.stream()
                .filter(it -> !ignored.contains(namer.apply(it.getItem())))
                .collect(Collectors.toSet());
        this.setCurrentDash(new MainDash(this, this.items));
        this.favorites = favorites;
        int i = 0;
        // enable
        this.tabs.add(new SideTab(Tabs.TOGGLE.ordinal(), i++, 0, 18, this) {
            @Override
            public void drawTab() {
                this.texU = isEnabled() ? 0 : 18;
                super.drawTab();
            }
        });
        // main
        this.tabs.add(new SideTab(Tabs.ITEMS.ordinal(), i++, 40, 38, this));
        // favorites
        this.tabs.add(new SideTab(Tabs.FAVORITES.ordinal(), i++, 40, 38, this));
        // TODO potions nbt
        // settings
        this.tabs.add(new SideTab(Tabs.SETTINGS.ordinal(), i++, 20, 38, this));
        // search
        this.tabs.add(searchTab = new SideTab(Tabs.SEARCH.ordinal(), i++, 0, 38, this));
    }

    public void setCurrentDash(@Nonnull Dash dash) {
        if (dash == null) {
            dash = new MainDash(this, this.items);
        }
        if (this.currentDash != null) {
            if (!(this.currentDash instanceof MainDash) || ((MainDash) this.currentDash).isSearching()) {
                this.searchTab.visible = true;
            }
            this.currentDash.onClose();
        }
        this.currentDash = dash;
    }

    public void onTabActivated(@Nonnull SideTab tab) {

        Preconditions.checkPositionIndex(tab.id, Tabs.values().length, "Tab is out of bounds. Expected 0-" + Tabs.values().length + " but found " + tab.id);
        Tabs tabs = Tabs.values()[tab.id];
        switch (tabs) {
        case TOGGLE:
            this.setEnabled(!this.isEnabled());
            break;
        case ITEMS:
            setCurrentDash(new MainDash(this, this.items));
            break;
        case SETTINGS:
            setCurrentDash(new DashSettings(this));
            break;
        case FAVORITES:
            setCurrentDash(new MainDash(this, this.favorites.getItems()));
            break;
        case SEARCH:
            this.doSearch();

        }
    }

    public void setEnabled(boolean enable) {
        if (isEnabled() == enable)
            return;
        LiteModItemDash.getInstance().enabled = enable;
        toggleTimer = mc.ingameGUI.getUpdateCounter();
        LiteModItemDash.getInstance().saveConfig();
    }

    public boolean isEnabled() {
        return LiteModItemDash.getInstance().enabled;
    }

    public boolean isFocused() {
        return this.currentDash.isFocused();
    }

    public void onTick() {
        this.currentDash.onTick();
    }

    public void updateDash(InventoryEffectRenderer screen) {

        this.checkForGuiChanges(screen);
        int guiWidth = ((IGuiContainer) screen).getXSize();
        int newLeft = xPos / 2 - guiWidth / 2;
        if (!mc.thePlayer.getActivePotionEffects().isEmpty()) {
            newLeft += 50;
        }
        ((IGuiContainer) screen).setGuiLeft(newLeft);

    }

    private void checkForGuiChanges(InventoryEffectRenderer cont) {
        int yPos = 0;
        int width = (int) ((cont.width - ((IGuiContainer) cont).getXSize()) / (3f / 2f));
        width = (width / DASH_ICON_W) * DASH_ICON_W;
        int height = cont.height;
        int xPos;
        int tick = mc.ingameGUI.getUpdateCounter() - this.toggleTimer;
        EasingType easing = EasingsFactory.getInstance().quadratic();
        final float time = 10;
        if (!isEnabled()) {
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

    public void preRender(GuiContainer cont, int mousex, int mousey) {

        GlStateManager.enableAlpha();
        drawBackground();
        this.currentDash.preRender(mousex, mousey);

    }

    public void postRender(GuiContainer cont, int mousex, int mousey) {

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
            if (tab.mouseClicked(mouseX, mouseY, mouseButton)) {
                this.onTabActivated(tab);
                break;
            }
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        this.currentDash.mouseReleased(mouseX, mouseY, mouseButton);
    }

    public void mouseClickMove(int x, int y, int lastButton, long buttonTime) {
        this.currentDash.mouseClickMove(x, y);
    }

    private void doSearch() {
        MainDash dash = new MainDash(this, this.items);
        this.setCurrentDash(dash);
        dash.doSearch();
        if (!isEnabled())
            setEnabled(true);
        this.searchTab.visible = false;
    }

    public void keyTyped(char key, int code) {
        if (!currentDash.isFocused()) {
            if (code == Keyboard.KEY_O) {
                setEnabled(!isEnabled());
            }
        }
        if (code == Keyboard.KEY_TAB) {
            doSearch();
        }
        this.currentDash.keyTyped(key, code);

    }

    public void scroll(int i) {
        this.currentDash.scroll(i);
    }
}
