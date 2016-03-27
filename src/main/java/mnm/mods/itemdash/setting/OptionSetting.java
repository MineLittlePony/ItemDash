package mnm.mods.itemdash.setting;

import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.mumfrey.liteloader.client.gui.GuiCheckbox;

import mnm.mods.itemdash.ItemSorter;

public class OptionSetting extends Setting<ItemSorter> {

    private BiMap<ItemSorter, GuiCheckbox> options = HashBiMap.create(Maps.newEnumMap(ItemSorter.class));

    public OptionSetting(String text, Consumer<ItemSorter> loader, Supplier<ItemSorter> saver) {
        super(text, loader, saver, 120, mc.fontRendererObj.FONT_HEIGHT + 2);
    }

    @Override
    public void mouseClick(int x, int y, int b) {
        this.options.entrySet().stream()
                .filter(it -> it.getValue().mousePressed(mc, x, y))
                .findAny()
                .ifPresent(it -> {
                    set(it.getKey());
                    action();
                });
    }

    @Override
    public void draw(int x, int y) {
        int yPos = this.yPos;
        yPos += mc.fontRendererObj.FONT_HEIGHT + 4;
        mc.fontRendererObj.drawString(this.text, xPos, yPos, -1);
        for (Entry<ItemSorter, GuiCheckbox> e : options.entrySet()) {
            GuiCheckbox s = e.getValue();
            s.checked = e.getKey() == this.get();
            s.drawButton(mc, x, y);
        }
    }

    public OptionSetting option(ItemSorter sorter, String name) {
        height += 16;
        this.options.put(sorter, new GuiCheckbox(height, xPos, yPos, name));
        return this;
    }

    @Override
    public void setPos(int x, int y) {
        super.setPos(x, y);
        this.options.values().forEach(it -> {
            it.xPosition = this.xPos;
            it.yPosition = it.id + this.yPos;
        });
    }
}
