package mnm.mods.itemdash.access;

import com.mumfrey.liteloader.core.runtime.Obf;

public class ObfTable extends Obf {

    public static final Obf GuiContainer = new ObfTable("net.minecraft.client.gui.inventory.GuiContainer", "ayl");
    public static final Obf GuiContainerCreative = new ObfTable("net.minecraft.client.gui.inventory.GuiContainerCreative", "ayu");
    public static final Obf xSize = new ObfTable("field_146999_f", "f", "xSize");
    public static final Obf guiLeft = new ObfTable("field_147003_i", "i", "guiLeft");
    public static final Obf searchField = new ObfTable("field_147062_A", "A", "searchField");

    protected ObfTable(String seargeName, String obfName, String mcpName) {
        super(seargeName, obfName, mcpName);
    }

    protected ObfTable(String srgName, String obfName) {
        super(srgName, obfName);
    }
}
