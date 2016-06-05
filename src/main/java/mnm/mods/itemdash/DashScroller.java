package mnm.mods.itemdash;

import net.minecraft.client.Minecraft;

public class DashScroller extends DashElement {

    private static final int MIN_GRIP = 10;
    private static final int MAX_GRIP = 50;
    private static final int BUTTON_SIZE = 15;

    private final Scrollable scrollable;

    private boolean visible = true;

    private int xPos;
    private int yPos;
    private int height;
    private int width = 15;

    private boolean scrolling;

    private float scrollArea;
    private float scrollSize;

    public DashScroller(Scrollable itemdash) {
        this.scrollable = itemdash;
    }

    public void drawScrollbar() {
        this.zLevel = 300;
        Minecraft.getMinecraft().getTextureManager().bindTexture(BG);
        this.xPos = scrollable.getX() + scrollable.getWidth();

        float contentSize = scrollable.getContentHeight();
        float windowSize = scrollable.getWindowHeight();
        float trackSize = windowSize - BUTTON_SIZE * 2;

        float contentRatio = windowSize / contentSize;
        float gripSize = trackSize * contentRatio;

        gripSize = Math.min(gripSize, MAX_GRIP);
        gripSize = Math.max(gripSize, MIN_GRIP);

        scrollArea = contentSize - windowSize;
        if (!(visible = !(scrollArea <= 0)))
            return;
        float windowPos = scrollable.getScroll() + scrollable.getY();
        float windowRatio = windowPos / scrollArea;
        scrollSize = trackSize - gripSize;
        float gripPos = scrollSize * windowRatio;

        this.yPos = scrollable.getY() + (int) gripPos + BUTTON_SIZE;
        height = (int) gripSize;

        // top
        this.drawBorders(xPos, scrollable.getY(), width, width, 0, 0, 18, 18, BOTTOM);
        this.drawTexturedModalRect(xPos, scrollable.getY(), 40, 15, 15, 10);
        // bottom
        this.drawBorders(xPos, scrollable.getY() + scrollable.getWindowHeight() - width, width, width, 0, 0, 18, 18, TOP);
        this.drawTexturedModalRect(xPos, scrollable.getY() + scrollable.getWindowHeight() - width, 40, 25, 15, 10);

        this.drawBorders(xPos, scrollable.getY(), width, scrollable.getWindowHeight(), 0, 0, 18, 18, ALL);
        this.drawBorders(xPos, this.yPos, width, height, 0, 0, 18, 18, ALL);
        this.zLevel = 0;
    }

    public void mouseClick(int mousex, int mousey, int button) {
        if (visible && mousex > xPos && button == 0) {

            if (mousey < scrollable.getY() + width) {
                scrollable.scroll(-1);
                return;
            } else if (mousey > scrollable.getY() + scrollable.getWindowHeight() - width) {
                scrollable.scroll(1);
                return;
            }
            this.scrolling = true;
            if (mousey < yPos || mousey > yPos + height) {
                scroll(mousey);
            }
        }

    }

    public void mouseRelease(int mousex, int mousey, int button) {
        if (visible)
            this.scrolling = false;
    }

    public void mouseDrag(int mousex, int mousey) {
        if (visible && scrolling)
            scroll(mousey);

    }

    private void scroll(int mousey) {
        mousey -= scrollable.getY() + BUTTON_SIZE;
        mousey -= height / 2;
        float scrollRatio = mousey / scrollSize;
        float scroll = scrollRatio * scrollArea;

        scrollable.setScroll((int) scroll);
    }
}
