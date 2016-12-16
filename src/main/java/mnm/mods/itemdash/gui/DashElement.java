package mnm.mods.itemdash.gui;

import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;

public class DashElement extends Gui {

    protected static final int TOP = 0x1,
            LEFT = 0x2,
            RIGHT = 0x4,
            BOTTOM = 0x8,
            TOP_LEFT = 0x10,
            TOP_RIGHT = 0x20,
            BOTTOM_LEFT = 0x40,
            BOTTOM_RIGHT = 0x80,
            ALL = TOP | LEFT | RIGHT | BOTTOM | TOP_LEFT | TOP_RIGHT | BOTTOM_LEFT | BOTTOM_RIGHT;

    public static final ResourceLocation BG = new ResourceLocation("itemdash", "textures/gui/itemdash.png");

    protected void drawBorders(int xPos, int yPos, int width, int height, int flags) {
        this.drawBorders(xPos, yPos, width, height + 1, 0, 0, 21, 21, flags);
    }

    protected void drawBorders(int xPos, int yPos, int width, int height, int u, int v, int texW, int texH, int flags) {
        final boolean t = getFlag(flags, TOP);
        final boolean l = getFlag(flags, LEFT);
        final boolean r = getFlag(flags, RIGHT);
        final boolean b = getFlag(flags, BOTTOM);
        final boolean tl = getFlag(flags, TOP_LEFT);
        final boolean tr = getFlag(flags, TOP_RIGHT);
        final boolean bl = getFlag(flags, BOTTOM_LEFT);
        final boolean br = getFlag(flags, BOTTOM_RIGHT);

        final int wsize = (texW - u) / 3;
        final int hsize = (texH - v) / 3;
        // top left
        if (tl)
            this.drawTexturedModalRect(xPos, yPos, u, v, wsize, hsize);
        // top right
        if (tr)
            this.drawTexturedModalRect(xPos + width - wsize, yPos, u + wsize * 2, v, wsize, hsize);
        // bottom left
        if (bl)
            this.drawTexturedModalRect(xPos, yPos + height - wsize, u, v + hsize * 2, wsize, hsize);
        // bottom right
        if (br)
            this.drawTexturedModalRect(xPos + width - wsize, yPos + height - hsize, u + wsize * 2, v + hsize * 2, wsize, hsize);

        int remaining;

        // left
        if (l) {
            int height2 = height - (bl ? hsize : 0);
            remaining = height2 - (tl ? hsize : 0);
            while (remaining > hsize) {
                this.drawTexturedModalRect(xPos, yPos + height2 - remaining, u, v + hsize, wsize, hsize);
                remaining -= hsize;
            }
            this.drawTexturedModalRect(xPos, yPos + height2 - remaining, u, v + hsize, wsize, remaining);
        }
        // top
        if (t) {
            int width2 = width - (tr ? wsize : 0);
            remaining = width2 - (tl ? wsize : 0);
            while (remaining > wsize) {
                this.drawTexturedModalRect(xPos + width2 - remaining, yPos, u + wsize, v, wsize, hsize);
                remaining -= wsize;
            }
            this.drawTexturedModalRect(xPos + width2 - remaining, yPos, u + wsize, v, remaining, hsize);
        }
        // bottom
        if (b) {
            int width2 = width - (br ? wsize : 0);
            remaining = width2 - (bl ? wsize : 0);
            while (remaining > wsize) {
                this.drawTexturedModalRect(xPos + width2 - remaining, yPos + height - hsize, u + wsize, v + hsize * 2, wsize, hsize);
                remaining -= wsize;
            }
            this.drawTexturedModalRect(xPos + width2 - remaining, yPos + height - hsize, u + wsize, v + hsize * 2, remaining, hsize);
        }
        // right
        if (r) {
            int height2 = height - (br ? hsize : 0);
            remaining = height - (tr ? hsize : 0);
            while (remaining > hsize) {
                this.drawTexturedModalRect(xPos + width - wsize, yPos + height2 - remaining, u + wsize * 2, v + hsize, wsize, hsize);
                remaining -= hsize;
            }
            this.drawTexturedModalRect(xPos + width - wsize, yPos + height2 - remaining, u + wsize * 2, v + hsize, wsize, remaining);
        }
    }

    private static boolean getFlag(int b, int b2) {
        return (b & b2) == b2;
    }
}
