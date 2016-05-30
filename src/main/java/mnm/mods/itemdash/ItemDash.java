package mnm.mods.itemdash;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.input.Keyboard;

import com.google.common.collect.Lists;
import com.google.common.collect.ObjectArrays;

import mnm.mods.itemdash.ducks.IGuiContainer;
import mnm.mods.itemdash.easing.EasingType;
import mnm.mods.itemdash.easing.EasingsFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemDash extends GuiDash implements Scrollable {

    public static final int DASH_ICON_W = 18;

    private final Set<ItemStack> items;
    private Predicate<ItemStack> filter;
    private Comparator<ItemStack> sorter;

    private Minecraft mc = Minecraft.getMinecraft();

    private List<SideTab> tabs = Lists.newArrayList();
    private ItemIcon[][] arrangedIcons = {};

    public int xPos;
    public int yPos;
    public int width;
    public int height;

    private int scroll;

    private GuiTextField search;
    private DashScroller scrollbar;
    private GuiSettings settings;

    // tabs
    private SideTab searchTab;

    private boolean dirty = true;
    private boolean hasSearched;
    private int toggleTimer;
    private int searchTimer;

    public ItemDash(final List<String> ignored) {
        List<ItemStack> list = Lists.newArrayList();
        Item.REGISTRY.forEach((Item it) -> it.getSubItems(it, null, list));
        Function<Item, String> namer = it -> Item.REGISTRY.getNameForObject(it).toString();
        this.items = list.stream()
                .filter(it -> !ignored.contains(namer.apply(it.getItem())))
                .collect(Collectors.toSet());
        this.sorter = ItemSorter.DEFAULT.getSort();
        this.search = new GuiTextField(0, mc.fontRendererObj, 0, 0, 5, 5);
        this.scrollbar = new DashScroller(this);
        this.settings = new GuiSettings(this);
        int i = 0;
        // enable
        this.tabs.add(new SideTab(i++)
                .texU(() -> isEnabled() ? 0 : 18).texV(18)
                .action(() -> setEnabled(!isEnabled())));
        // settings
        this.tabs.add(new SideTab(i++)
                .texU(20).texV(38)
                .action(() -> {
                    if (this.settings.isVisible()) {
                        this.settings.close();
                        this.hasSearched = this.filter != null;
                        this.searchTab.visible = !hasSearched;
                    } else {
                        setEnabled(true);
                        this.settings.open();
                        this.hasSearched = false;
                    }
                }));
        // search
        this.tabs.add(searchTab = new SideTab(i++)
                .texU(0).texV(38)
                .action(() -> this.doSearch()));
    }

    public void filter(Predicate<ItemStack> filter) {
        this.filter = filter;
    }

    public void sort(ItemSorter sort) {
        if (sort == null)
            sort = ItemSorter.DEFAULT;
        this.sorter = sort.getSort();
    }

    public void setEnabled(boolean enable) {
        if (isEnabled() == enable)
            return;
        if (enable && settings.isVisible())
            settings.close();
        this.hasSearched = this.filter != null;
        this.searchTab.visible = !hasSearched;
        LiteModItemDash.getInstance().enabled = enable;
        toggleTimer = mc.ingameGUI.getUpdateCounter();
        LiteModItemDash.getInstance().saveConfig();
    }

    public boolean isEnabled() {
        return LiteModItemDash.getInstance().enabled;
    }

    public void onTick() {
        if (this.search == null)
            return;
        if (mc.ingameGUI.getUpdateCounter() % 4 == 0)
            this.search.updateCursorCounter();
    }

    public void arrangeItems(GuiContainer cont) {
        List<ItemStack> stacks;
        if (filter != null)
            stacks = items.stream().filter(filter).collect(Collectors.toList());
        else
            stacks = Lists.newArrayList(items);
        Collections.sort(stacks, sorter);
        final int totalCols = this.width / DASH_ICON_W;
        ItemIcon[][] icons = new ItemIcon[0][];
        for (int i = 0; i < stacks.size(); i++) {
            int row = i / totalCols;
            int col = i % totalCols;
            if (icons.length <= row)
                icons = concatArray(icons, new ItemIcon[totalCols]);
            icons[row][col] = new ItemIcon(stacks.get(i));
        }
        this.arrangedIcons = icons;
        scroll(0);
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
        if (hasSearched) {
            tick = mc.ingameGUI.getUpdateCounter() - searchTimer;
            if (tick < 2)
                yPos = (int) easing.in().ease(tick, yPos, 14, 2);
            else
                yPos += 14;
            height = cont.height - yPos;
        }

        if (this.xPos != xPos || this.yPos != yPos || this.width != width || this.height != height) {
            dirty |= width != this.width || height != this.height;
            this.xPos = xPos;
            this.yPos = yPos;
            this.width = width - 15;
            this.height = height;
            if (!search.isFocused()) {
                // this.hasSearched = false;
            }
            if (dirty) {
                GuiTextField text = new GuiTextField(0, mc.fontRendererObj, xPos + 2, yPos - 14, width, 14);
                if (search != null) {
                    text.setText(search.getText());
                    text.setCursorPosition(search.getCursorPosition());
                    text.setFocused(search.isFocused());
                }
                text.setTextColor(-1);
                this.search = text;
            }
            search.xPosition = xPos;
            search.yPosition = yPos - 14;
        }
        if (dirty) {
            arrangeItems(cont);
            dirty = false;
        }
    }

    public void updateDash(InventoryEffectRenderer screen) {

        checkForGuiChanges(screen);
        int guiWidth = ((IGuiContainer) screen).getXSize();
        int newLeft = xPos / 2 - guiWidth / 2;
        if (!mc.thePlayer.getActivePotionEffects().isEmpty()) {
            newLeft += 50;
        }
        ((IGuiContainer) screen).setGuiLeft(newLeft);

    }

    public void preRender(GuiContainer cont, int mousex, int mousey) {

        GlStateManager.enableAlpha();
        drawBackground();
        if (!this.settings.isVisible()) {
            this.scrollbar.drawScrollbar();
            renderItems(mousex, mousey);
        }
    }

    public void postRender(GuiContainer cont, int mousex, int mousey) {

        if (this.settings.isVisible())
            this.settings.draw(mousex, mousey);
        else {
            this.search.drawTextBox();
            ItemIcon icon = getItem(mousex, mousey);
            if (icon != null) {
                icon.renderTooltip(mousex, mousey);
                RenderHelper.disableStandardItemLighting();
            }
        }
    }

    private void drawBackground() {
        this.zLevel = 300;
        mc.getTextureManager().bindTexture(BG);
        GlStateManager.color(1, 1, 1);
        this.drawBorders(xPos - 2, yPos, width + 17, height, 0, 0, 18, 18,
                TOP | LEFT | BOTTOM | BOTTOM_LEFT);
        // search box
        if (hasSearched)
            this.drawBorders(xPos - 2, this.search.yPosition, width + 17, 16, 0, 0, 18, 18,
                    LEFT | TOP | TOP_LEFT | RIGHT);

        this.tabs.forEach(it -> it.draw());

        this.zLevel = 0;

    }

    private void renderItems(int mousex, int mousey) {
        mousey++;
        ItemIcon[][] arrangedIcons = getVisibleItems();
        for (int i = 0; i < arrangedIcons.length; i++) {
            for (int j = 0; j < arrangedIcons[i].length; j++) {
                ItemIcon icon = arrangedIcons[i][j];
                if (icon == null)
                    continue;
                int xPos = j * DASH_ICON_W + this.xPos;
                int yPos = i * DASH_ICON_W + this.yPos;
                icon.renderAt(xPos + 1, yPos + 2);
                if (mousex >= xPos && mousey >= yPos && mousex < xPos + DASH_ICON_W && mousey < yPos + DASH_ICON_W)
                    Gui.drawRect(xPos, yPos + 1, xPos + DASH_ICON_W, yPos + DASH_ICON_W + 1, 0x66ffffff);
            }
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (this.settings.isVisible()) {
            this.settings.mouseClick(mouseX, mouseY);
        } else {
            this.search.mouseClicked(mouseX, mouseY, mouseButton);
            ItemIcon item = getItem(mouseX, mouseY);
            if (item != null) {
                LiteModItemDash.getInstance().giveItem(item.getStack(mouseButton));
            }
            scrollbar.mouseClick(mouseX, mouseY, mouseButton);
        }
        this.tabs.forEach(it -> it.onClick(mouseX, mouseY));
    }

    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (!settings.isVisible())
            scrollbar.mouseRelease(mouseX, mouseY, mouseButton);
    }

    public void mouseClickMove(int x, int y, int lastButton, long buttonTime) {
        if (!settings.isVisible())
            scrollbar.mouseDrag(x, y);

    }

    private void doSearch() {

        this.search.setFocused(true);
        this.search.setText("");
        this.filter = null;
        if (!isEnabled())
            setEnabled(true);
        if (!hasSearched) {
            this.hasSearched = true;
            this.searchTimer = mc.ingameGUI.getUpdateCounter();
        }
        this.settings.close();
        this.searchTab.visible = false;
    }

    public void keyTyped(char key, int code) {
        if (!search.isFocused()) {
            if (code == Keyboard.KEY_O) {
                setEnabled(!isEnabled());
            }
            if (code == Keyboard.KEY_E) {
                onClose();
            }
        }
        if (this.settings.isVisible()) {
            this.settings.keyTyped(key, code);
            return;
        }
        if (code == Keyboard.KEY_TAB) {
            doSearch();
            this.filter = null;
        }
        if (code == Keyboard.KEY_ESCAPE) {
            if (this.search.isFocused())
                this.search.setFocused(false);
            else
                onClose();
        }

        if (this.search.textboxKeyTyped(key, code)) {
            if (search.getText().isEmpty()) {
                this.filter = null;
            } else {
                this.filter = ItemFilters.nameContains(search.getText());
            }
            dirty = this.search.isFocused();
        }
    }

    private void onClose() {
        markDirty();
        this.settings.close();
        this.hasSearched = filter != null;
        this.searchTab.visible = !hasSearched;
    }

    public void markDirty() {
        this.hasSearched = this.filter != null;
        // this.search.setText("");
        // this.filter = null;
        this.dirty = true;
        this.tabs.forEach(it -> it.visible = true);

    }

    @Override
    public int getX() {
        return xPos;
    }

    @Override
    public int getY() {
        return yPos;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getWindowHeight() {
        return height;
    }

    @Override
    public int getScroll() {
        return scroll * DASH_ICON_W;
    }

    @Override
    public void setScroll(int newScroll) {
        if (settings.isVisible())
            return;
        scroll = newScroll / DASH_ICON_W;
        scroll = Math.max(scroll, 0);
        scroll = Math.min(scroll, arrangedIcons.length - height / DASH_ICON_W);

    }

    @Override
    public void scroll(int dir) {
        setScroll((scroll + dir) * DASH_ICON_W);
    }

    private ItemIcon getItem(int mouseX, int mouseY) {
        mouseX -= xPos;
        mouseY -= yPos - 1;
        if (mouseX <= 0 || mouseY <= 0)
            return null;
        int count = this.width / DASH_ICON_W;
        int col = mouseX / DASH_ICON_W;
        int row = mouseY / DASH_ICON_W;
        // outside
        ItemIcon[][] visible = getVisibleItems();
        if (row < 0 || col < 0 || row >= visible.length || col >= count)
            return null;
        return visible[row][col];
    }

    public boolean isSearching() {
        return search.isFocused() || this.settings.isFocused();
    }

    private ItemIcon[][] getVisibleItems() {
        int rows = this.height / DASH_ICON_W;

        int start = scroll;
        if (start < 0)
            start = 0;
        int end = rows + scroll + 1;
        if (end > arrangedIcons.length)
            end = arrangedIcons.length;

        return ArrayUtils.subarray(arrangedIcons, start, end);
    }

    /** So the compiler doesn't complain about ambiguous references */
    private static <T> T[] concatArray(T[] ta, T t) {
        return ObjectArrays.concat(ta, t);
    }

    public class SideTab extends GuiDash {

        public boolean visible = true;
        private final int yPos;
        private IntSupplier texU;
        private IntSupplier texV;
        private Runnable action;

        public SideTab(int yPos) {
            this.yPos = yPos * 20;
        }

        public SideTab texU(IntSupplier tex) {
            this.texU = tex;
            return this;
        }

        public SideTab texU(int i) {
            return texU(() -> i);
        }

        public SideTab texV(IntSupplier tex) {
            this.texV = tex;
            return this;
        }

        public SideTab texV(int i) {
            return texV(() -> i);
        }

        public SideTab action(Runnable click) {
            this.action = click;
            return this;
        }

        public void onClick(int x, int y) {
            if (action != null)
                if (visible && x < ItemDash.this.xPos && x > ItemDash.this.xPos - 20 && y > yPos && y < yPos + 20)
                    action.run();
        }

        public void draw() {
            this.zLevel = 299;
            if (visible) {
                drawBorders(ItemDash.this.xPos - 20, yPos, 20, 20, 0, 0, 18, 18, TOP | TOP_LEFT | LEFT | BOTTOM_LEFT | BOTTOM);
                int u = this.texU == null ? -1 : texU.getAsInt();
                int v = this.texV == null ? -1 : texV.getAsInt();
                if (u >= 0 && v >= 0) {
                    this.drawTexturedModalRect(ItemDash.this.xPos - 20, yPos, u, v, 20, 20);
                }
            }
            this.zLevel = 0;
        }
    }

    @Override
    public int getContentHeight() {
        return arrangedIcons.length * DASH_ICON_W;
    }

}
