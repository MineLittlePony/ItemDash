package mnm.mods.itemdash.mixin;

import mnm.mods.itemdash.LiteModItemDash;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.inventory.Container;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(GuiInventory.class)
public abstract class MixinGuiInventory extends InventoryEffectRenderer {

    public MixinGuiInventory(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }

    @Inject(method = "drawGuiContainerBackgroundLayer(FII)V", at = @At("HEAD"))
    private void onDrawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY, CallbackInfo ci) {
        LiteModItemDash.onPreRenderScreen(mouseX, mouseY);
    }

    @Inject(method = "drawGuiContainerForegroundLayer(II)V", at = @At("RETURN"))
    private void onDrawGuiContainerForegroundLayer(int mouseX, int mouseY, CallbackInfo ci) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(-guiLeft, -this.guiTop, 0);
        LiteModItemDash.onPostRenderScreen(mouseX, mouseY);
        GlStateManager.popMatrix();
    }

    @Inject(method = "updateScreen()V", at = @At("HEAD"))
    private void onUpdateScreen(CallbackInfo ci) {
        LiteModItemDash.onUpdateScreen(this);
    }

    @Override
    public void handleMouseInput() throws IOException {
        int x = Mouse.getEventX() * this.width / this.mc.displayWidth;

        LiteModItemDash.onHandleMouseInput(x);
        super.handleMouseInput();

    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (!LiteModItemDash.onHandleKeyboardInput(typedChar, keyCode))
            // not searching
            super.keyTyped(typedChar, keyCode);
    }
}
