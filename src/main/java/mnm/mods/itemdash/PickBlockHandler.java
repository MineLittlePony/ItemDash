package mnm.mods.itemdash;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLeashKnot;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
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
        if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit != RayTraceResult.Type.MISS) {
            ItemStack itemstack;

            if (mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
                BlockPos blockpos = mc.objectMouseOver.getBlockPos();
                IBlockState state = mc.world.getBlockState(blockpos);

                if (state.getMaterial() == Material.AIR) {
                    return;
                }

                itemstack = state.getBlock().getItem(mc.world, blockpos, state);

                if (itemstack.isEmpty()) {
                    return;
                }

            } else {
                if (mc.objectMouseOver.typeOfHit != RayTraceResult.Type.ENTITY || mc.objectMouseOver.entityHit == null) {
                    return;
                }

                if (mc.objectMouseOver.entityHit instanceof EntityPainting) {
                    itemstack = new ItemStack(Items.PAINTING);
                } else if (mc.objectMouseOver.entityHit instanceof EntityLeashKnot) {
                    itemstack = new ItemStack(Items.LEAD);
                } else if (mc.objectMouseOver.entityHit instanceof EntityItemFrame) {
                    EntityItemFrame itemFrame = (EntityItemFrame) mc.objectMouseOver.entityHit;
                    ItemStack displayedItem = itemFrame.getDisplayedItem();

                    if (displayedItem.isEmpty()) {
                        itemstack = new ItemStack(Items.ITEM_FRAME);
                    } else {
                        itemstack = displayedItem.copy();
                    }
                } else if (mc.objectMouseOver.entityHit instanceof EntityMinecart) {
                    EntityMinecart minecart = (EntityMinecart) mc.objectMouseOver.entityHit;
                    Item item;

                    switch (minecart.getType()) {
                        case FURNACE:
                            item = Items.FURNACE_MINECART;
                            break;
                        case CHEST:
                            item = Items.CHEST_MINECART;
                            break;
                        case TNT:
                            item = Items.TNT_MINECART;
                            break;
                        case HOPPER:
                            item = Items.HOPPER_MINECART;
                            break;
                        case COMMAND_BLOCK:
                            item = Items.COMMAND_BLOCK_MINECART;
                            break;
                        default:
                            item = Items.MINECART;
                    }

                    itemstack = new ItemStack(item);
                } else if (mc.objectMouseOver.entityHit instanceof EntityBoat) {
                    itemstack = new ItemStack(((EntityBoat) mc.objectMouseOver.entityHit).getItemBoat());
                } else if (mc.objectMouseOver.entityHit instanceof EntityArmorStand) {
                    itemstack = new ItemStack(Items.ARMOR_STAND);
                } else if (mc.objectMouseOver.entityHit instanceof EntityEnderCrystal) {
                    itemstack = new ItemStack(Items.END_CRYSTAL);
                } else {
                    ResourceLocation resourcelocation = EntityList.getKey(mc.objectMouseOver.entityHit);

                    if (resourcelocation == null || !EntityList.ENTITY_EGGS.containsKey(resourcelocation)) {
                        return;
                    }

                    itemstack = new ItemStack(Items.SPAWN_EGG);
                    ItemMonsterPlacer.applyEntityIdToItemStack(itemstack, resourcelocation);
                }
            }

            if (itemstack.isEmpty()) {
                String s = "";

                if (mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
                    s = Block.REGISTRY.getNameForObject(mc.world.getBlockState(mc.objectMouseOver.getBlockPos()).getBlock()).toString();
                } else if (mc.objectMouseOver.typeOfHit == RayTraceResult.Type.ENTITY) {
                    s = EntityList.getKey(mc.objectMouseOver.entityHit).toString();
                }

                LiteModItemDash.LOGGER.warn("Picking on: [{}] {} gave null item", mc.objectMouseOver.typeOfHit, s);

            } else if (mc.player.isSneaking() || !mc.player.inventory.hasItemStack(itemstack)) {
                ItemStack stack = itemstack.copy();
                stack.setCount(stack.getMaxStackSize());
                LiteModItemDash.getInstance().giveItem(stack);
            }

        }
    }

}
