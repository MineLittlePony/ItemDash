package mnm.mods.itemdash;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;

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

            if (mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                int meta = 0;
                Item item;
                BlockPos blockpos = mc.objectMouseOver.getBlockPos();
                Block block = mc.theWorld.getBlockState(blockpos).getBlock();
                meta = block.getDamageValue(mc.theWorld, blockpos);
                if (block.getMaterial() == Material.air) {
                    return;
                }

                item = block.getItem(mc.theWorld, blockpos);

                if (item == null) {
                    return;
                }
                if (mc.thePlayer.isSneaking() || !inventoryHas(item, meta)) {
                    LiteModItemDash.getInstance().giveItem(new ItemStack(item, item.getItemStackLimit(), meta));
                }
            }
        }
    }

    private boolean inventoryHas(Item item, int meta) {
        for (ItemStack s : mc.thePlayer.inventory.mainInventory) {
            if (s == null)
                continue;
            Item item1 = s.getItem();
            int meta1 = s.getMetadata();
            if (item1 == item && meta1 == meta)
                return true;
        }
        return false;
    }
}
