package mnm.mods.itemdash.mixin;

import java.io.IOException;

import org.spongepowered.asm.mixin.Mixin;

import mnm.mods.itemdash.LiteModItemDash;
import mnm.mods.itemdash.ducks.IGuiContainer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.inventory.Container;

@Mixin(InventoryEffectRenderer.class)
public abstract class MixinInventoryEffectRenderer extends GuiContainer implements IGuiContainer {

    public MixinInventoryEffectRenderer(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }

    @Override
    public void initGui() {
        super.initGui();
        LiteModItemDash.onUpdateScreen((InventoryEffectRenderer) (Object) this);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        LiteModItemDash.onMouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        LiteModItemDash.onMouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        LiteModItemDash.onMouseReleased(mouseX, mouseY, mouseButton);
        super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public int getXSize() {
        return this.xSize;
    }

    @Override
    public void setGuiLeft(int i) {
        this.guiLeft = i;
    }

}
