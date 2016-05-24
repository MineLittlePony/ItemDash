package mnm.mods.itemdash;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

public class PickBlockHandler {

    private Minecraft mc = Minecraft.getMinecraft();
    private boolean cooldown;

    public void handleMouse() {
        if (!mc.thePlayer.capabilities.isCreativeMode) {
            if (mc.gameSettings.keyBindPickBlock.isKeyDown()) {
                if (!cooldown) {
                    middleClickMouse();
                    cooldown = true;
                }
            } else {
                cooldown = false;
            }
        }
    }

    private void middleClickMouse() {
        if (mc.objectMouseOver != null) {

            if (mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
                ItemStack itemstack;
                BlockPos blockpos = mc.objectMouseOver.getBlockPos();
                IBlockState state = mc.theWorld.getBlockState(blockpos);
                if (state.getMaterial() == Material.AIR) {
                    return;
                }

                itemstack = state.getBlock().getItem(mc.theWorld, blockpos, state);

                if (itemstack == null) {
                    return;
                }
                if (mc.thePlayer.isSneaking() || !inventoryHas(itemstack)) {
                    Item item = itemstack.getItem();
                    LiteModItemDash.getInstance().giveItem(new ItemStack(item, item.getItemStackLimit(), itemstack.getMetadata()));
                }
            }
        }
    }

    private boolean inventoryHas(ItemStack item) {
        for (ItemStack s : mc.thePlayer.inventory.mainInventory) {
            if (item.isItemEqual(s))
                return true;

        }
        return false;
    }
}
