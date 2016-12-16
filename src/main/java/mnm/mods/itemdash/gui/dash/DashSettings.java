package mnm.mods.itemdash.gui.dash;

import com.google.common.collect.Lists;
import com.mumfrey.liteloader.core.LiteLoader;
import mnm.mods.itemdash.gui.Dash;
import mnm.mods.itemdash.ItemSorter;
import mnm.mods.itemdash.LiteModItemDash;
import mnm.mods.itemdash.setting.BoolSetting;
import mnm.mods.itemdash.setting.OptionSetting;
import mnm.mods.itemdash.setting.Setting;
import mnm.mods.itemdash.setting.StringSetting;

import java.util.List;
import java.util.function.Supplier;

public class DashSettings extends Dash {

    private final LiteModItemDash litemod = LiteModItemDash.getInstance();
    private List<Supplier<Boolean>> focused = Lists.newArrayList();

    private List<Setting<?>> settings = Lists.newArrayList();

    public DashSettings(ItemDash itemdash) {
        super(itemdash);
        this.settings.add(new BoolSetting("Legacy IDs",
                it -> litemod.numIds = it, litemod.numIds));
        this.settings.add(new StringSetting(this, "Give Command",
                it -> litemod.giveCommand = it, litemod.giveCommand)
                        .preset("Vanilla", "/give {0} {1} {2} {3}")
                        .preset("Essentials", "/i {1}:{3} {2}"));
        this.settings.add(new OptionSetting<>("Sorting",
                it -> litemod.sort = it, litemod.sort)
                        .option(ItemSorter.BY_ID, "By ID")
                        .option(ItemSorter.DEFAULT, "By Legacy")
                        .option(ItemSorter.BY_NAME, "By Name"));
    }

    @Override
    public void preRender(int mousex, int mousey) {

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

    @Override
    public void mouseClicked(int x, int y, int button) {
        this.settings.forEach(it -> it.mouseClick(x, y, button));
    }

    @Override
    public void keyTyped(char key, int code) {
        this.settings.forEach(it -> it.keyPush(key, code));
    }

    @Override
    public void onClose() {
        LiteLoader.getInstance().writeConfig(litemod);
        this.itemdash.dirty = true;
    }

    @Override
    public boolean isFocused() {
        return this.focused.stream().anyMatch(Supplier::get);
    }

    public void addFocus(Supplier<Boolean> focus) {
        this.focused.add(focus);
    }

}
