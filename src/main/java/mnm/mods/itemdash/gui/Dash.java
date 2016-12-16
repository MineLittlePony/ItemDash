package mnm.mods.itemdash.gui;

import mnm.mods.itemdash.gui.dash.ItemDash;

public class Dash extends DashElement {

    protected final ItemDash itemdash;

    public Dash(ItemDash dash) {
        this.itemdash = dash;
    }

    public void onClose() {}

    public void update(int xPos, int yPos, int width, int height) {}

    public void keyTyped(char key, int code) {}

    public void mouseClickMove(int x, int y) {}

    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {}

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {}

    public void postRender(int mousex, int mousey) {}

    public void preRender(int mousex, int mousey) {}

    public void onTick() {}

    public void scroll(int i) {}

    public boolean isFocused() {
        return false;
    }

}
