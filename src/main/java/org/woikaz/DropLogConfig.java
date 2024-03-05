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
    default String blackListedItems() {
        return "";
    }

    @ConfigItem(
            keyName = "removeOnPickUp",
            name = "Remove on pickup",
            description = "If toggled, whenever the player picks up an item, the given items quantity gets decreased in the droplog"
    )
    default boolean removeOnPickUp() {
        return false;
    }
}
