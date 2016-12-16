package mnm.mods.itemdash.gui;

import mnm.mods.itemdash.easing.EasingType;
import mnm.mods.itemdash.easing.Easings;
import mnm.mods.itemdash.easing.EasingsFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class Rainblower extends Gui implements Runnable {

    static final Object YES_THIS_IS_AN_EASTER_EGG = null;
    private static final ResourceLocation DASHIE = new ResourceLocation("itemdash", "textures/gui/rainbow.png");

    private Minecraft mc = Minecraft.getMinecraft();
    private boolean activated;
    private int timer;

    @Override
    public void run() {
        this.activated = true;
        this.timer = mc.ingameGUI.getUpdateCounter();
    }

    public void draw() {
        if (activated) {
            final int TIME = 60;
            int currTime = mc.ingameGUI.getUpdateCounter() - timer;
            if (currTime < TIME) {
                Easings factory = EasingsFactory.getInstance();
                EasingType linear = factory.sinusoidal();
                EasingType quadratic = factory.quartic();

                GuiScreen screen = Minecraft.getMinecraft().currentScreen;
                if (screen == null) return;
                int xPos = (int) quadratic.in().ease(currTime, -256, screen.width + 256 * 3, TIME);
                int yPos = (int) linear.in().ease(currTime, screen.height / 8, screen.height + 100, TIME);
                GlStateManager.pushMatrix();
                GlStateManager.scale(.5, .5, .5);

                this.zLevel = 600;
                GlStateManager.color(1, 1, 1);
                mc.getTextureManager().bindTexture(DASHIE);
                this.drawTexturedModalRect(xPos, yPos, 0, 0, 256, 100);
                GlStateManager.popMatrix();
                this.zLevel = 0;
            } else {
                this.activated = false;
            }

        }
    }
}
