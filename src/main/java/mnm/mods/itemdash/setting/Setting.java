package mnm.mods.itemdash.setting;

import java.util.function.Consumer;

import mnm.mods.itemdash.LiteModItemDash;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public abstract class Setting<T> extends Gui {

    protected static Minecraft mc = Minecraft.getMinecraft();

    protected String text;
    protected int xPos;
    protected int yPos;
    public int width;
    public int height;

    private T value;

    private Consumer<T> saver;

    public Setting(String text, Consumer<T> save, T load, int w, int h) {
        this.text = text;
        this.width = w;
        this.height = h;

        this.saver = save;
        this.value = load;
    }

    protected void set(T value) {
        this.value = value;
        action();
    }

    protected T get() {
        return value;
    }

    public abstract void draw(int x, int y);

    public void keyPush(char key, int code) {}

    public void mouseClick(int x, int y, int b) {
        if (x > xPos && x < xPos + width && y > yPos && y < yPos + height)
            action();
    }

    private void action() {
        saver.accept(get());
        LiteModItemDash.getInstance().saveConfig();
    }

    public void setPos(int x, int y) {
        this.xPos = x;
        this.yPos = y;
    }

}