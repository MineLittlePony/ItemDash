package mnm.mods.itemdash.setting;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.mumfrey.liteloader.client.gui.GuiCheckbox;

public class BoolSetting extends Setting<Boolean> {

    private GuiCheckbox chkbox;

    public BoolSetting(String name, Consumer<Boolean> consumer, Supplier<Boolean> supplier) {
        super(name, consumer, supplier, 30, 14);
        this.chkbox = new GuiCheckbox(0, 0, 0, name);
    }

    @Override
    public void mouseClick(int x, int y, int b) {
        if (chkbox.mousePressed(mc, x, y))
            action();
    }

    @Override
    protected void action() {
        set(!get());
        super.action();
    }

    @Override
    protected Boolean get() {
        return chkbox.checked;
    }

    @Override
    protected void set(Boolean value) {
        chkbox.checked = value;
    }

    @Override
    public void setPos(int x, int y) {
        super.setPos(x, y);
        chkbox.xPosition = x;
        chkbox.yPosition = y;
    }

    @Override
    public void draw(int x, int y) {
        this.chkbox.drawButton(mc, x, y);
    }

}
