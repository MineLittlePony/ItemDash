package mnm.mods.itemdash.access;

import com.mumfrey.liteloader.transformers.access.Accessor;
import com.mumfrey.liteloader.transformers.access.ObfTableClass;

@ObfTableClass(ObfTable.class)
@Accessor("GuiContainer")
public interface IGuiContainer {

    @Accessor("xSize")
    int getXSize();

    @Accessor("guiLeft")
    int getGuiLeft();

    @Accessor("guiLeft")
    void setGuiLeft(int i);
}
