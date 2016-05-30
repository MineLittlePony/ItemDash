package mnm.mods.itemdash.mixin;

import java.io.IOException;

import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mnm.mods.itemdash.LiteModItemDash;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.inventory.Container;

@Mixin(GuiInventory.class)
public abstract class MixinGuiInventory extends InventoryEffectRenderer {

    public MixinGuiInventory(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }

    @Inject(method = "drawGuiContainerBackgroundLayer(FII)V", at = @At("HEAD"))
    private void onDrawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY, CallbackInfo ci) {
        LiteModItemDash.onPreRenderScreen(this, mouseX, mouseY, partialTicks);
    }

    @Inject(method = "drawGuiContainerForegroundLayer(II)V", at = @At("RETURN"))
    private void onDrawGuiContainerForegroundLayer(int mouseX, int mouseY, CallbackInfo ci) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(-guiLeft, -this.guiTop, 0);
        LiteModItemDash.onPostRenderScreen(this, mouseX, mouseY);
        GlStateManager.popMatrix();
    }

    @Inject(method = "updateScreen()V", at = @At("HEAD"))
    private void onUpdateScreen(CallbackInfo ci) {
        LiteModItemDash.onUpdateScreen(this);
    }

    @Override
    public void handleMouseInput() throws IOException {
        int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        LiteModItemDash.onHandleMouseInput(x, y);
        super.handleMouseInput();

    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (!LiteModItemDash.onHandleKeyboardInput(typedChar, keyCode))
            // not searching
            super.keyTyped(typedChar, keyCode);
    }
}
