package mnm.mods.itemdash.setting;

import java.util.Map.Entry;
import java.util.function.Consumer;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mumfrey.liteloader.client.gui.GuiCheckbox;

public class OptionSetting<E extends Enum<E>> extends Setting<E> {

    private BiMap<E, GuiCheckbox> options = HashBiMap.create();

    public OptionSetting(String text, Consumer<E> loader, E saver) {
        super(text, loader, saver, 120, mc.fontRendererObj.FONT_HEIGHT + 2);
    }

    @Override
    public void mouseClick(int x, int y, int b) {
        this.options.entrySet().stream()
                .filter(e -> e.getValue().mousePressed(mc, x, y))
                .findAny()
                .map(Entry::getKey)
                .ifPresent(this::set);
    }

    @Override
    public void draw(int x, int y) {
        int yPos = this.yPos;
        yPos += mc.fontRendererObj.FONT_HEIGHT + 4;
        mc.fontRendererObj.drawString(this.text, xPos, yPos, -1);
        for (Entry<E, GuiCheckbox> e : options.entrySet()) {
            GuiCheckbox s = e.getValue();
            s.checked = e.getKey() == this.get();
            s.drawButton(mc, x, y);
        }
    }

    public OptionSetting<E> option(E sorter, String name) {
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
