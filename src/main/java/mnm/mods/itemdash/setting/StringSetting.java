package mnm.mods.itemdash.setting;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mumfrey.liteloader.client.gui.GuiCheckbox;

import mnm.mods.itemdash.GuiSettings;
import net.minecraft.client.gui.GuiTextField;

public class StringSetting extends Setting<String> {

    private static final int W = 120;
    private static final int H = 15;

    private GuiTextField textBox;
    private BiMap<String, GuiCheckbox> presets = HashBiMap.create();

    public StringSetting(GuiSettings settings, String name, Consumer<String> apply, Supplier<String> current) {
        super(name, apply, current, W, H);
        this.textBox = new GuiTextField(0, mc.fontRendererObj, 0, 0, W, H);

        settings.addFocus(nul -> textBox.isFocused());
    }

    public StringSetting preset(String name, String cmd) {
        this.height += 16;
        this.presets.put(cmd, new GuiCheckbox(height, xPos, yPos + height, name));

        return this;
    }

    @Override
    protected String get() {
        return textBox.getText();
    }

    @Override
    protected void set(String value) {
        this.textBox.setText(value);
        this.textBox.setCursorPositionZero();
    }

    @Override
    public void setPos(int x, int y) {
        super.setPos(x, y);
        textBox.xPosition = x;
        textBox.yPosition = y + mc.fontRendererObj.FONT_HEIGHT + 4;

        this.presets.values().forEach(it -> {
            it.yPosition = it.id + this.yPos;
            it.xPosition = xPos;
        });
    }

    @Override
    public void mouseClick(int x, int y, int b) {
        textBox.mouseClicked(x, y, b);
        this.presets.values().stream()
                .filter(it -> it.mousePressed(mc, x, y))
                .findAny()
                .ifPresent(this::onPresetPressed);
        this.action();
    }

    private void onPresetPressed(GuiCheckbox chk) {
        this.presets.values().forEach(it -> it.checked = false);
        chk.checked = true;
        String cmd = this.presets.inverse().get(chk);
        this.set(cmd);
    }

    @Override
    public void keyPush(char key, int code) {
        if (textBox.textboxKeyTyped(key, code)) {
            action();
            this.presets.values().forEach(it -> it.checked = false);
            this.presets.entrySet().stream()
                    .filter(e -> e.getKey().equals(get()))
                    .findFirst()
                    .ifPresent(e -> e.getValue().checked = true);
        }
    }

    @Override
    public void draw(int x, int y) {
        mc.fontRendererObj.drawString(text, xPos, yPos, -1);
        this.textBox.drawTextBox();
        this.presets.values().forEach(it -> it.drawButton(mc, x, y));
    }
}
