package mnm.mods.itemdash;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.lwjgl.input.Mouse;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;
import com.mumfrey.liteloader.InitCompleteListener;
import com.mumfrey.liteloader.PacketHandler;
import com.mumfrey.liteloader.Tickable;
import com.mumfrey.liteloader.core.LiteLoader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.util.math.MathHelper;

public class LiteModItemDash implements Tickable, InitCompleteListener, PacketHandler {

    private static LiteModItemDash instance;

    private Minecraft mc;
    private ItemDash itemdash;
    private PickBlockHandler pbh;
    private Rainblower rd;
    private Konami konamiCode;

    private ItemStack lastRequestedStack;

    @Expose
    public boolean enabled = true;
    @Expose
    public String giveCommand = "/give {0} {1} {2} {3}";
    @Expose
    public boolean numIds = false;
    @Expose
    public ItemSorter sort = ItemSorter.DEFAULT;
    @Expose
    public String[] ignored = {
            "minecraft:farmland", "minecraft:lit_furnace", "minecraft:map", "minecraft:enchanted_book"
    };

    @Override
    public String getName() {
        return "ItemDash";
    }

    @Override
    public String getVersion() {
        return "@VERSION@";
    }

    @Override
    public void init(File configPath) {
        instance = this;
        this.mc = Minecraft.getMinecraft();
        this.rd = new Rainblower();
        this.pbh = new PickBlockHandler();
        this.konamiCode = new Konami(rd);
        LiteLoader.getInstance().registerExposable(this, "itemdash.json");
    }

    @Override
    public void onInitCompleted(Minecraft minecraft, LiteLoader loader) {
        // init later so I catch other mods and their items/blocks
        this.itemdash = new ItemDash(Lists.newArrayList(ignored));
        this.itemdash.sort(sort);
    }

    @Override
    public void onTick(Minecraft minecraft, float partialTicks, boolean inGame, boolean clock) {
        GuiScreen screen = minecraft.currentScreen;
        if (screen instanceof InventoryEffectRenderer) {
            this.lastRequestedStack = null;
            itemdash.onTick();
        }
        if (inGame) {
            pbh.handleMouse();
        }
    }

    @Override
    public List<Class<? extends Packet<?>>> getHandledPackets() {
        return ImmutableList.of(SPacketSetSlot.class);
    }

    @Override
    public boolean handlePacket(INetHandler netHandler, Packet<?> packet) {
        if (this.lastRequestedStack != null && packet instanceof SPacketSetSlot) {
            SPacketSetSlot setslot = (SPacketSetSlot) packet;
            ItemStack stack = setslot.getStack();
            Item lastItem = this.lastRequestedStack.getItem();
            int lastMeta = this.lastRequestedStack.getMetadata();
            if (lastItem == stack.getItem() && lastMeta == stack.getMetadata()) {
                this.lastRequestedStack = null;
                int slot = setslot.getSlot() - 9 * 4;
                if (slot >= 0)
                    mc.thePlayer.inventory.currentItem = slot;
                // TODO reorganize inventory
            }
        }
        return true;
    }

    @Override
    public void upgradeSettings(String version, File configPath, File oldConfigPath) {}

    public void giveItem(ItemStack stack) {
        Minecraft mc = Minecraft.getMinecraft();
        this.lastRequestedStack = stack;
        if (mc.isSingleplayer()) {
            UUID uuid = mc.thePlayer.getGameProfile().getId();
            EntityPlayer player = mc.getIntegratedServer().getPlayerList().getPlayerByUUID(uuid);
            player.inventory.addItemStackToInventory(stack);
        } else {
            String message = formatGiveCommand(stack);
            mc.thePlayer.sendChatMessage(message);
        }
    }

    private String formatGiveCommand(ItemStack stack) {
        String give = instance.giveCommand;
        int numId = Item.getIdFromItem(stack.getItem());
        String strId = Item.REGISTRY.getNameForObject(stack.getItem()).toString();
        give = give
                .replace("{0}", mc.thePlayer.getName())
                .replace("{1}", instance.numIds ? Integer.toString(numId) : strId)
                .replace("{2}", Integer.toString(stack.stackSize))
                .replace("{3}", Integer.toString(stack.getMetadata()));
        return give;
    }

    public void saveConfig() {
        LiteLoader.getInstance().writeConfig(this);
    }

    public static void onMouseClicked(int mouseX, int mouseY, int mouseButton) {
        instance.itemdash.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public static void onMouseReleased(int mouseX, int mouseY, int mouseButton) {
        instance.itemdash.mouseReleased(mouseX, mouseY, mouseButton);
    }

    public static void onMouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        instance.itemdash.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    public static boolean onHandleMouseInput(int mouseX, int mouseY) {
        return instance.handleMouseInput(mouseX, mouseY);
    }

    private boolean handleMouseInput(int x, int y) {
        if (x > itemdash.xPos) {
            int scroll = Mouse.getEventDWheel();
            if (scroll != 0) {

                scroll = MathHelper.clamp_int(scroll, -1, 1);

                if (GuiScreen.isShiftKeyDown()) {
                    scroll *= itemdash.height / ItemDash.DASH_ICON_W;
                }
                itemdash.scroll(-scroll);
                return true;
            }
            return false;
        }
        return x > itemdash.xPos - 20;
    }

    public static boolean onHandleKeyboardInput(char typedChar, int keyCode) {
        return instance.handleKeyboardInput(typedChar, keyCode);
    }

    private boolean handleKeyboardInput(char typedChar, int keycode) {
        boolean cancel = itemdash.isSearching();
        this.itemdash.keyTyped(typedChar, keycode);
        this.konamiCode.onKey(keycode);
        return cancel;
    }

    public static void onPostRenderScreen(InventoryEffectRenderer screen, int x, int y) {
        instance.itemdash.postRender(screen, x, y);
        instance.rd.draw();
    }

    public static void onPreRenderScreen(InventoryEffectRenderer screen, int x, int y, float ticks) {
        instance.itemdash.preRender(screen, x, y);
    }

    public static LiteModItemDash getInstance() {
        return instance;
    }

}
