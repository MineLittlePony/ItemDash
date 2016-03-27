package mnm.mods.itemdash;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;
import com.mumfrey.liteloader.InitCompleteListener;
import com.mumfrey.liteloader.PacketHandler;
import com.mumfrey.liteloader.core.LiteLoader;
import com.mumfrey.liteloader.transformers.event.EventInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S2FPacketSetSlot;

public class LiteModItemDash implements InitCompleteListener, PacketHandler {

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
        return ImmutableList.of(S2FPacketSetSlot.class);
    }

    @Override
    public boolean handlePacket(INetHandler netHandler, Packet<?> packet) {
        if (this.lastRequestedStack != null && packet instanceof S2FPacketSetSlot) {
            S2FPacketSetSlot setslot = (S2FPacketSetSlot) packet;
            ItemStack stack = setslot.func_149174_e();
            Item lastItem = this.lastRequestedStack.getItem();
            int lastMeta = this.lastRequestedStack.getMetadata();
            if (lastItem == stack.getItem() && lastMeta == stack.getMetadata()) {
                this.lastRequestedStack = null;
                int slot = setslot.func_149173_d() - 9 * 4;
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
            EntityPlayer player = mc.getIntegratedServer().getConfigurationManager().getPlayerByUUID(uuid);
            player.inventory.addItemStackToInventory(stack);
        } else {
            String message = formatGiveCommand(stack);
            mc.thePlayer.sendChatMessage(message);
        }
    }

    private String formatGiveCommand(ItemStack stack) {
        String give = instance.giveCommand;
        int numId = Item.getIdFromItem(stack.getItem());
        String strId = Item.itemRegistry.getNameForObject(stack.getItem()).toString();
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

    public static void handleMouseInput(EventInfo<GuiScreen> e) {
        if (e.getSource() instanceof InventoryEffectRenderer) {
            if (instance.handleMouseInput(e.getSource()))
                e.cancel();
        }
    }

    private int touchValue;
    private int lastButton;
    private long lastButtonTime;

    private boolean handleMouseInput(GuiScreen screen) {
        int x = Mouse.getEventX() * screen.width / this.mc.displayWidth;
        int y = screen.height - Mouse.getEventY() * screen.height / this.mc.displayHeight - 1;
        int butt = Mouse.getEventButton();

        if (Mouse.getEventButtonState()) {
            if (this.mc.gameSettings.touchscreen && this.touchValue++ > 0) {
                return false;
            }
            lastButton = butt;
            lastButtonTime = Minecraft.getSystemTime();
            itemdash.mouseClicked(x, y, butt);
        } else if (butt != -1) {
            if (this.mc.gameSettings.touchscreen && --this.touchValue > 0) {
                return false;
            }
            lastButton = -1;
            itemdash.mouseReleased(x, y, butt);
        } else if (lastButton != -1 && lastButtonTime > 0) {
            long buttonTime = Minecraft.getSystemTime() - this.lastButtonTime;
            itemdash.mouseClickMove(x, y, this.lastButton, buttonTime);
        }

        if (x > itemdash.xPos) {
            int scroll = Mouse.getEventDWheel();
            if (scroll != 0) {
                if (scroll > 1) {
                    scroll = 1;
                }
                if (scroll < -1) {
                    scroll = -1;
                }
                if (GuiScreen.isShiftKeyDown()) {
                    scroll *= itemdash.height / ItemDash.DASH_ICON_W;
                }
                itemdash.scroll(-scroll);
            }
            return true;
        }
        return x > itemdash.xPos - 20;
    }

    public static void handleKeyboardInput(EventInfo<GuiScreen> e) {
        if (e.getSource() instanceof InventoryEffectRenderer) {
            if (instance.handleKeyboardInput())
                e.cancel();
        }
    }

    private boolean handleKeyboardInput() {
        if (Keyboard.getEventKeyState()) {
            boolean cancel = itemdash.isSearching();
            itemdash.keyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey());
            konamiCode.onKey(Keyboard.getEventKey());
            return cancel;
        }
        return false;
    }

    public static void onPostRenderScreen(EventInfo<EntityRenderer> e, float ticks, long nanoTime) {
        Minecraft mc = Minecraft.getMinecraft();
        GuiScreen screen = mc.currentScreen;
        if (screen instanceof InventoryEffectRenderer) {
            final ScaledResolution scaledresolution = new ScaledResolution(mc);
            int j = scaledresolution.getScaledWidth();
            int k = scaledresolution.getScaledHeight();
            final int x = Mouse.getX() * j / mc.displayWidth;
            final int y = k - Mouse.getY() * k / mc.displayHeight - 1;
            instance.itemdash.postRender((InventoryEffectRenderer) screen, x, y);
            instance.rd.draw();
        }
    }

    public static void onPreRenderScreen(EventInfo<EntityRenderer> e, float ticks, long nanoTime) {
        Minecraft mc = Minecraft.getMinecraft();
        GuiScreen screen = mc.currentScreen;
        if (screen instanceof InventoryEffectRenderer) {
            final ScaledResolution scaledresolution = new ScaledResolution(mc);
            int j = scaledresolution.getScaledWidth();
            int k = scaledresolution.getScaledHeight();
            final int x = Mouse.getX() * j / mc.displayWidth;
            final int y = k - Mouse.getY() * k / mc.displayHeight - 1;
            instance.itemdash.preRender((InventoryEffectRenderer) screen, x, y);
        }
    }

    public static LiteModItemDash getInstance() {
        return instance;
    }

}
