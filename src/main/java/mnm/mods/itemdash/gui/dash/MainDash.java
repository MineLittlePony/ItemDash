package mnm.mods.itemdash.gui.dash;

import com.google.common.collect.Lists;
import com.google.common.collect.ObjectArrays;
import mnm.mods.itemdash.gui.Dash;
import mnm.mods.itemdash.gui.DashScroller;
import mnm.mods.itemdash.Favorites;
import mnm.mods.itemdash.ItemFilters;
import mnm.mods.itemdash.ItemIcon;
import mnm.mods.itemdash.LiteModItemDash;
import mnm.mods.itemdash.gui.Scrollable;
import mnm.mods.itemdash.easing.EasingType;
import mnm.mods.itemdash.easing.EasingsFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MainDash extends Dash implements Scrollable {

    private Collection<ItemStack> items;
    @Nullable
    private Predicate<ItemStack> filter;

    private int yPos;
    private int scroll;

    private GuiTextField search;
    private DashScroller scrollbar;

    private ItemIcon[][] arrangedIcons = {};

    private boolean searching;
    private int searchTimer;

    private int lastMouseX;
    private int lastMouseY;

    public MainDash(ItemDash itemdash, Collection<ItemStack> items) {
        super(itemdash);
        this.items = items;
        this.search = new GuiTextField(0, Minecraft.getMinecraft().fontRendererObj, 0, 0, 5, 5);
        this.scrollbar = new DashScroller(this);
    }

    private Comparator<ItemStack> sorter() {
        return LiteModItemDash.getInstance().sort.getSort();
    }

    private ItemIcon[][] arrangeItems(Collection<ItemStack> items) {
        List<ItemStack> stacks;
        if (filter != null)
            stacks = items.stream().filter(filter).collect(Collectors.toList());
        else
            stacks = Lists.newArrayList(items);
        stacks.sort(sorter());
        final int totalCols = this.itemdash.width / ItemDash.DASH_ICON_W;
        ItemIcon[][] icons = new ItemIcon[0][];
        for (int i = 0; i < stacks.size(); i++) {
            int row = i / totalCols;
            int col = i % totalCols;
            if (icons.length <= row)
                icons = concatArray(icons, new ItemIcon[totalCols]);
            icons[row][col] = new ItemIcon(stacks.get(i));
        }
        return icons;
    }

    private void renderItems(int mousex, int mousey) {
        mousey++;
        ItemIcon[][] arrangedIcons = getVisibleItems();
        for (int i = 0; i < arrangedIcons.length; i++) {
            for (int j = 0; j < arrangedIcons[i].length; j++) {
                ItemIcon icon = arrangedIcons[i][j];
                if (icon == null)
                    continue;
                int xPos = j * ItemDash.DASH_ICON_W + this.itemdash.xPos;
                int yPos = i * ItemDash.DASH_ICON_W + getY();
                icon.renderAt(xPos + 1, yPos + 2);
                if (mousex >= xPos && mousey >= yPos && mousex < xPos + ItemDash.DASH_ICON_W && mousey < yPos + ItemDash.DASH_ICON_W)
                    Gui.drawRect(xPos, yPos + 1, xPos + ItemDash.DASH_ICON_W, yPos + ItemDash.DASH_ICON_W + 1, 0x66ffffff);
            }
        }
    }

    private ItemIcon[][] getVisibleItems() {
        int rows = this.itemdash.height / ItemDash.DASH_ICON_W;

        int start = scroll;
        if (start < 0)
            start = 0;
        int end = rows + scroll + 1;
        if (end > arrangedIcons.length)
            end = arrangedIcons.length;

        return ArrayUtils.subarray(arrangedIcons, start, end);
    }

    @Override
    public void update(int xPos, int yPos, int width, int height) {

        EasingType easing = EasingsFactory.getInstance().quadratic();

        if (searching) {
            int tick = Minecraft.getMinecraft().ingameGUI.getUpdateCounter() - searchTimer;
            if (tick < 2)
                yPos = (int) easing.in().ease(tick, yPos, 14, 2);
            else
                yPos += 14;
            height = itemdash.height - yPos;
        }

        this.yPos = yPos;

        if (itemdash.dirty) {
            GuiTextField text = new GuiTextField(0, Minecraft.getMinecraft().fontRendererObj, xPos + 2, yPos - 14, width, 14);
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

        if (itemdash.dirty) {
            updateItems(this.items);
            this.arrangedIcons = arrangeItems(this.items);
            scroll(0);
            itemdash.dirty = false;
        }
    }

    protected void updateItems(Collection<ItemStack> items) {
        this.items = items;
    }

    @Nullable
    protected ItemIcon getItem(int mouseX, int mouseY) {
        mouseX -= itemdash.xPos;
        mouseY -= getY() - 1;
        if (mouseX <= 0 || mouseY <= 0)
            return null;
        int count = this.itemdash.width / ItemDash.DASH_ICON_W;
        int col = mouseX / ItemDash.DASH_ICON_W;
        int row = mouseY / ItemDash.DASH_ICON_W;
        // outside
        ItemIcon[][] visible = getVisibleItems();
        if (row < 0 || col < 0 || row >= visible.length || col >= count)
            return null;
        return visible[row][col];
    }

    public boolean isSearching() {
        return search.isFocused();
    }

    @Override
    public void onTick() {
        if (this.search == null)
            return;
        if (Minecraft.getMinecraft().ingameGUI.getUpdateCounter() % 4 == 0)
            this.search.updateCursorCounter();
    }

    @Override
    public void preRender(int mousex, int mousey) {
        this.scrollbar.drawScrollbar();
        renderItems(mousex, mousey);
        // search box
        if (searching) {
            this.drawBorders(itemdash.xPos - 2, this.search.yPosition, itemdash.width + 17, 16, 0, 0, 18, 18,
                    LEFT | TOP | TOP_LEFT | RIGHT);
        }
    }

    @Override
    public void postRender(int mousex, int mousey) {
        this.lastMouseX = mousex;
        this.lastMouseY = mousey;
        this.search.drawTextBox();
        ItemIcon icon = getItem(mousex, mousey);
        if (icon != null) {
            icon.renderTooltip(mousex, mousey);
            RenderHelper.disableStandardItemLighting();
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        this.search.mouseClicked(mouseX, mouseY, mouseButton);
        ItemIcon item = getItem(mouseX, mouseY);
        if (item != null) {
            LiteModItemDash.getInstance().giveItem(item.getStack(mouseButton));
        }
        scrollbar.mouseClick(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        scrollbar.mouseRelease(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseClickMove(int x, int y) {
        scrollbar.mouseDrag(x, y);

    }

    @Override
    public void keyTyped(char key, int code) {

        if (code == Keyboard.KEY_F) {
            this.favoriteItem();
        }
        if (code == Keyboard.KEY_N) {
            // this.nbtItem();
        }
        if (code == Keyboard.KEY_ESCAPE) {
            if (this.search.isFocused())
                this.search.setFocused(false);
            else
                onClose();
        }
        if (code == Keyboard.KEY_TAB) {
            doSearch();
        }

        if (this.search.textboxKeyTyped(key, code)) {
            if (search.getText().isEmpty()) {
                this.filter = null;
            } else {
                this.filter = ItemFilters.nameContains(search.getText());
            }
            itemdash.dirty = this.search.isFocused();
        }
    }

    @Override
    public void onClose() {
        markDirty();
        this.searching = filter != null;
    }

    @Override
    public boolean isFocused() {
        return this.search.isFocused();
    }

    public void markDirty() {
//        this.searching = this.filter != null;
        this.itemdash.dirty = true;
    }

    @Override
    public int getX() {
        return itemdash.xPos;
    }

    @Override
    public int getY() {
        return this.yPos;
    }

    @Override
    public int getWidth() {
        return itemdash.width;
    }

    @Override
    public int getWindowHeight() {
        return itemdash.height;
    }

    @Override
    public int getScroll() {
        return scroll * ItemDash.DASH_ICON_W;
    }

    @Override
    public void setScroll(int newScroll) {
        scroll = newScroll / ItemDash.DASH_ICON_W;
        scroll = Math.max(scroll, 0);
        scroll = Math.min(scroll, arrangedIcons.length - itemdash.height / ItemDash.DASH_ICON_W);

    }

    @Override
    public void scroll(int dir) {
        setScroll((scroll + dir) * ItemDash.DASH_ICON_W);
    }

    @Override
    public int getContentHeight() {
        return arrangedIcons.length * ItemDash.DASH_ICON_W;
    }

    public void doSearch() {
        this.search.setText("");
        this.filter = null;
        this.search.setFocused(true);
        if (!searching) {
            this.searching = true;
            this.searchTimer = Minecraft.getMinecraft().ingameGUI.getUpdateCounter();
        }
    }

    protected void favoriteItem() {
        ItemIcon icon = getItem(this.lastMouseX, this.lastMouseY);
        if (icon == null)
            return;

        ItemStack stack = icon.getStack();
        favoriteItem(stack);
    }

    protected void favoriteItem(ItemStack stack) {

        Favorites favorites = itemdash.getFavorites();

        if (favorites.has(stack)) {
            favorites.remove(stack);
        } else {
            favorites.add(stack);

        }
        LiteModItemDash.getInstance().writeDataFile();

    }

    /** So the compiler doesn't complain about ambiguous references */
    private static <T> T[] concatArray(T[] ta, T t) {
        return ObjectArrays.concat(ta, t);
    }

}
