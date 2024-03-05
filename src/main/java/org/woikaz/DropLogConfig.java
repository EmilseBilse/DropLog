package org.woikaz;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("droplog")
public interface DropLogConfig extends Config {
    @ConfigItem(
            keyName = "itemBlackList",
            name = "Black listed items",
            description = "Items that should not be added to the drop log"
    )
    default String blackListedItems()
    {
        return "";
    }
}
