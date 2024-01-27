package org.woikaz.ui;

import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.PluginPanel;
import org.woikaz.ExamplePlugin;

public class DropLogPanel  extends PluginPanel {

    private final ItemManager itemManager;
    private final ExamplePlugin plugin;

    public DropLogPanel(ItemManager itemManager, ExamplePlugin plugin) {
        this.itemManager = itemManager;
        this.plugin = plugin;
    }
}
