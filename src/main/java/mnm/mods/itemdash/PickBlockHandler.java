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
        if (!mc.player.capabilities.isCreativeMode) {
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
                BlockPos blockpos = mc.objectMouseOver.getBlockPos();
                IBlockState state = mc.world.getBlockState(blockpos);
                if (state.getMaterial() == Material.AIR) {
                    return;
                }

                ItemStack itemstack = state.getBlock().getItem(mc.world, blockpos, state);

                if (itemstack.isEmpty()) {
                    return;
                }
                if (mc.player.isSneaking() || !inventoryHas(itemstack)) {
                    Item item = itemstack.getItem();
                    LiteModItemDash.getInstance().giveItem(new ItemStack(item, item.getItemStackLimit(), itemstack.getMetadata()));
                }
            }
        }
    }

    private boolean inventoryHas(ItemStack item) {
        for (ItemStack s : mc.player.inventory.mainInventory) {
            if (item.isItemEqual(s))
                return true;

        }
        return false;
    }
}
