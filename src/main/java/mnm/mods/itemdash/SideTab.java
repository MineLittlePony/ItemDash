package mnm.mods.itemdash;

public class SideTab extends DashElement {

    private final ItemDash itemDash;

    public final int id;
    public boolean visible = true;
    public int xPosition;
    public int yPosition;
    public int width;
    public int height;

    protected int texU;
    protected int texV;

    public SideTab(int id, int yPos, int texU, int texV, ItemDash itemDash) {
        this.id = id;
        this.xPosition = 0;
        this.yPosition = yPos * 20;
        this.width = 20;
        this.height = 20;
        this.texU = texU;
        this.texV = texV;
        this.itemDash = itemDash;
    }

    public void drawTab() {
        this.xPosition = this.itemDash.xPos - 20;
        if (this.visible) {
            this.zLevel = 299;
            drawBorders(this.xPosition, yPosition, width, height, TOP | TOP_LEFT | LEFT | BOTTOM_LEFT | BOTTOM);

            this.drawTexturedModalRect(this.xPosition, yPosition, this.texU, this.texV, this.width, this.height);
            this.zLevel = 0;
        }
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        return this.visible && mouseX > xPosition && mouseX < xPosition + width && mouseY > yPosition && mouseY < yPosition + height;
    }
}