package mnm.mods.itemdash;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.lwjgl.input.Mouse;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
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
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.util.math.MathHelper;

public class LiteModItemDash implements Tickable, InitCompleteListener, PacketHandler {

    private static LiteModItemDash instance;

    private File dataFile;

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
    public Set<String> ignored = Sets.newHashSet(
            "minecraft:farmland",
            "minecraft:lit_furnace",
            "minecraft:map",
            "minecraft:enchanted_book",
            "minecraft:end_crystal");

    public Favorites favorites;

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
        this.favorites = new Favorites();
        LiteLoader.getInstance().registerExposable(this, "itemdash.json");
        this.dataFile = new File(configPath, "itemdash.dat");
    }

    @Override
    public void onInitCompleted(Minecraft minecraft, LiteLoader loader) {
        // init later so I catch other mods and their items/blocks
        this.readDataFile();
        this.itemdash = new ItemDash(Sets.newHashSet(ignored), favorites);
    }

    public void readDataFile() {
        try {
            File data = this.dataFile;
            if (data.exists()) {
                NBTTagCompound tag = CompressedStreamTools.read(data);

                this.favorites.readFromNbt(tag);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void writeDataFile() {
        try {
            File data = this.dataFile;
            NBTTagCompound tag = new NBTTagCompound();

            this.favorites.writeToNbt(tag);

            data.getParentFile().mkdirs();
            CompressedStreamTools.write(tag, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            boolean added = player.inventory.addItemStackToInventory(stack);
            if (added)
                player.inventoryContainer.detectAndSendChanges();
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
        boolean cancel = itemdash.isFocused();
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

    public static void onUpdateScreen(InventoryEffectRenderer screen) {
        instance.itemdash.updateDash(screen);

    }

}
