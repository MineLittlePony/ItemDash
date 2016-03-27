package mnm.mods.itemdash.access;

import com.mumfrey.liteloader.transformers.access.Accessor;
import com.mumfrey.liteloader.transformers.access.ObfTableClass;

import net.minecraft.client.gui.GuiTextField;

@ObfTableClass(ObfTable.class)
@Accessor("GuiContainerCreative")
public interface IGuiCreativeContainer {

    @Accessor("searchField")
    GuiTextField getSearchField();
}
