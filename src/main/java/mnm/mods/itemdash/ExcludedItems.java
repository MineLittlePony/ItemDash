package mnm.mods.itemdash;

import com.google.common.collect.Sets;
import com.google.gson.annotations.Expose;
import com.mumfrey.liteloader.modconfig.ConfigStrategy;
import com.mumfrey.liteloader.modconfig.Exposable;
import com.mumfrey.liteloader.modconfig.ExposableOptions;

import java.util.Set;

@ExposableOptions(strategy = ConfigStrategy.Versioned, filename = "itemdash.excluded.json")
public class ExcludedItems implements Exposable {

    @Expose
    private Set<String> ignored = Sets.newHashSet(
            "minecraft:air",
            "minecraft:farmland",
            "minecraft:lit_furnace",
            "minecraft:map",
            "minecraft:enchanted_book",
            "minecraft:end_crystal");

    public Set<String> getIgnored() {
        return ignored;
    }
}
