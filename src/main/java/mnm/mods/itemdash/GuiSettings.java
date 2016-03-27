package mnm.mods.itemdash;

import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.Lists;

import mnm.mods.itemdash.setting.BoolSetting;
import mnm.mods.itemdash.setting.OptionSetting;
import mnm.mods.itemdash.setting.Setting;
import mnm.mods.itemdash.setting.StringSetting;

public class GuiSettings extends GuiDash {

    private final LiteModItemDash litemod = LiteModItemDash.getInstance();
    private final ItemDash itemdash;
    private boolean visible;
    private Predicate<Void> focused;

    private List<Setting<?>> settings = Lists.newArrayList();

    public GuiSettings(ItemDash itemdash) {
        this.itemdash = itemdash;
        this.settings.add(new BoolSetting("Legacy IDs",
                it -> litemod.numIds = it,
                () -> litemod.numIds));
        this.settings.add(new StringSetting(this, "Give Command",
                it -> litemod.giveCommand = it,
                () -> litemod.giveCommand)
                        .preset("Vanilla", "/give {0} {1} {2} {3}")
                        .preset("Essentials", "/i {1}:{3} {2}"));
        this.settings.add(new OptionSetting("Sorting",
                it -> {
                    litemod.sort = it;
                    itemdash.sort(it);
                } , () -> litemod.sort)
                        .option(ItemSorter.BY_ID, "By ID")
                        .option(ItemSorter.DEFAULT, "By Legacy")
                        .option(ItemSorter.BY_NAME, "By Name"));
    }

    public void open() {
        if (!visible) {
            this.visible = true;
            settings.forEach(it -> it.applyCurrent());
        }
    }

    public void close() {
        if (visible) {
            visible = false;
        }
    }

    public void draw(int mousex, int mousey) {

        int xPos = itemdash.xPos;
        int yPos = itemdash.yPos;

        int lastHeight = 0;
        int pos = yPos;
        for (Setting<?> it : settings) {
            it.setPos(xPos + 4, pos += lastHeight + 6);
            lastHeight = it.height;
        }

        this.settings.forEach(it -> it.draw(mousex, mousey));
    }

    public void mouseClick(int x, int y) {
        this.settings.forEach(it -> it.mouseClick(x, y, 0));
    }

    public void keyTyped(char key, int code) {
        this.settings.forEach(it -> it.keyPush(key, code));
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean isFocused() {
        return focused.test(null);
    }

    public void addFocus(Predicate<Void> pred) {
        if (this.focused == null)
            focused = pred;
        else
            focused = focused.or(pred);
    }

}
