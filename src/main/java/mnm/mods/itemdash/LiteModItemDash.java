package mnm.mods.itemdash;

import com.google.common.collect.ImmutableList;
import com.google.gson.annotations.Expose;
import com.mumfrey.liteloader.InitCompleteListener;
import com.mumfrey.liteloader.PacketHandler;
import com.mumfrey.liteloader.Tickable;
import com.mumfrey.liteloader.core.LiteLoader;
import com.mumfrey.liteloader.modconfig.ExposableOptions;
import mnm.mods.itemdash.gui.Rainblower;
import mnm.mods.itemdash.gui.dash.ItemDash;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@ExposableOptions(filename = "itemdash.json")
public class LiteModItemDash implements Tickable, InitCompleteListener, PacketHandler {

    public static final Logger LOGGER = LogManager.getLogger("ItemDash");
    private static LiteModItemDash instance;

    private File dataFile;

    private Minecraft mc;
    private ItemDash itemdash;
    private PickBlockHandler pbh;
    private Rainblower rd;
    private Konami konamiCode;

    private boolean pickSlot;

    @Nonnull
    private ItemStack lastRequestedStack = ItemStack.EMPTY;

    @Expose
    private boolean visible = true;
    @Expose
    private boolean survivalPick = true;
    @Expose
    private String giveCommand = "/give {0} {1} {2} {3}";
    @Expose
    private boolean numIds = false;
    @Expose
    private ItemSorter sort = ItemSorter.DEFAULT;

    private ExcludedItems ignored = new ExcludedItems();

    Favorites favorites;

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
        this.rd = new Rainblower();
        this.konamiCode = new Konami(rd);
        this.pbh = new PickBlockHandler();
        this.favorites = new Favorites();

        LiteLoader.getInstance().registerExposable(this, null);
        LiteLoader.getInstance().registerExposable(this.ignored, null);

        this.dataFile = new File(configPath, "itemdash.dat");
    }

    @Override
    public void onInitCompleted(Minecraft minecraft, LiteLoader loader) {
        this.mc = minecraft;
        // init later so I catch other mods and their items/blocks
        this.readDataFile();
        this.itemdash = new ItemDash(ignored.getIgnored(), favorites);
    }

    private void readDataFile() {
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
            this.lastRequestedStack = ItemStack.EMPTY;
            itemdash.onTick();
        }
        if (inGame) {
            pbh.handleMouse(this.survivalPick);
        }
        if (this.pickSlot) {
            InventoryPlayer inv = minecraft.player.inventory;
            // inv.setPickedItemStack but doesn't give the item (just in case)
            int slot = inv.getSlotFor(this.lastRequestedStack);

            if (InventoryPlayer.isHotbar(slot)) {
                inv.currentItem = slot;
            } else if (slot != -1) {
                mc.playerController.pickItem(slot);
            } else {
                // happens with items on the server that had nbt.
                LOGGER.warn("Inventory didn't have {}?", this.lastRequestedStack);
            }
            this.lastRequestedStack = ItemStack.EMPTY;
            this.pickSlot = false;
        }
    }

    @Override
    public List<Class<? extends Packet<?>>> getHandledPackets() {
        return ImmutableList.of(SPacketSetSlot.class);
    }

    @Override
    public boolean handlePacket(INetHandler netHandler, Packet<?> packet) {
        if (!this.lastRequestedStack.isEmpty() && packet instanceof SPacketSetSlot) {
            SPacketSetSlot setslot = (SPacketSetSlot) packet;
            ItemStack stack = setslot.getStack();
            if (this.lastRequestedStack.isItemEqual(stack)) {
                this.pickSlot = true;
            }
        }
        return true;
    }

    @Override
    public void upgradeSettings(String version, File configPath, File oldConfigPath) {
    }

    public void giveItem(ItemStack stack) {
        Minecraft mc = Minecraft.getMinecraft();
        this.lastRequestedStack = stack;
        if (mc.player.isCreative()) {
            stack.setCount(1);
            mc.player.inventory.setPickedItemStack(stack);
            mc.playerController.sendSlotPacket(mc.player.getHeldItem(EnumHand.MAIN_HAND), 36 + mc.player.inventory.currentItem);

        } else if (mc.isSingleplayer() && mc.world.getWorldInfo().areCommandsAllowed()) {
            UUID uuid = mc.player.getGameProfile().getId();
            MinecraftServer server = mc.getIntegratedServer();
            assert server != null;

            EntityPlayer player = server.getPlayerList().getPlayerByUUID(uuid);
            if (player.inventory.addItemStackToInventory(stack))
                player.inventoryContainer.detectAndSendChanges();
        } else {
            String message = formatGiveCommand(stack);
            mc.player.sendChatMessage(message);
        }
    }

    private String formatGiveCommand(ItemStack stack) {
        int numId = Item.getIdFromItem(stack.getItem());
        ResourceLocation item = Item.REGISTRY.getNameForObject(stack.getItem());
        if (item == null) {
            throw new NullPointerException(stack + " is not a registered item.");
        }
        String strId = item.toString();
        return instance.giveCommand
                .replace("{0}", mc.player.getName())
                .replace("{1}", instance.numIds ? Integer.toString(numId) : strId)
                .replace("{2}", Integer.toString(stack.getCount()))
                .replace("{3}", Integer.toString(stack.getMetadata()));
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

    public static void onMouseClickMove(int mouseX, int mouseY) {
        instance.itemdash.mouseClickMove(mouseX, mouseY);
    }

    public static boolean onHandleMouseInput(int mouseX) {
        return instance.handleMouseInput(mouseX);
    }

    private boolean handleMouseInput(int x) {
        if (x > itemdash.xPos) {
            int scroll = Mouse.getEventDWheel();
            if (scroll != 0) {

                scroll = MathHelper.clamp(scroll, -1, 1);

                if (GuiScreen.isShiftKeyDown()) {
                    scroll *= itemdash.height / ItemDash.DASH_ICON_W;
                }
                itemdash.scroll(-scroll);
                return true;
            }
        }
        return false;
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

    public static void onPostRenderScreen(int x, int y) {
        instance.itemdash.postRender(x, y);
        instance.rd.draw();
    }

    public static void onPreRenderScreen(int x, int y) {
        instance.itemdash.preRender(x, y);
    }

    public static LiteModItemDash getInstance() {
        return instance;
    }

    public static void onUpdateScreen(GuiContainer screen) {
        instance.itemdash.updateDash(screen);

    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isSurvivalPick() {
        return survivalPick;
    }

    public void setSurvivalPick(boolean pick) {
        this.survivalPick = pick;
    }

    public String getGiveCommand() {
        return giveCommand;
    }

    public void setGiveCommand(String giveCommand) {
        this.giveCommand = giveCommand;
    }

    public boolean isNumIds() {
        return numIds;
    }

    public void setNumIds(boolean numIds) {
        this.numIds = numIds;
    }

    public ItemSorter getSort() {
        return sort;
    }

    public void setSort(ItemSorter sort) {
        this.sort = sort;
    }

}
