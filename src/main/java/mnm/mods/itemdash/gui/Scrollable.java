package mnm.mods.itemdash.gui;

public interface Scrollable {

    int getX();
    int getY();
    int getWidth();
    int getWindowHeight();
    int getContentHeight();
    int getScroll();
    void setScroll(int scr);
    void scroll(int scr);
}
