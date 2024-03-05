package org.woikaz;

import com.google.inject.Provides;
import javax.inject.Inject;
import javax.swing.*;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import org.woikaz.localstorage.DropDataStorage;
import org.woikaz.localstorage.DroppedItem;
import org.woikaz.ui.DropLogPanel;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@PluginDescriptor(
	name = "Drop Log"
)
public class DropLogPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	public DropLogConfig config;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private PluginManager pluginManager;

	private DropLogPanel panel;

	private NavigationButton navButton;
	private DropDataStorage dropDataStorage = new DropDataStorage();

	private List<Integer> pendingDrops = new ArrayList<Integer>();
	private List<Integer> pendingTakes = new ArrayList<>();

	@Provides
	DropLogConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DropLogConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		panel = new DropLogPanel(this);
		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/woikaz/droplog/droppings.png");
		navButton = NavigationButton.builder()
				.tooltip("Drop Log")
				.icon(icon)
				.priority(6)
				.panel(panel)
				.build();

		clientToolbar.addNavigation(navButton);
		// DropDataStorage dropDataStorage = new DropDataStorage();
		getInjector().injectMembers(dropDataStorage);
		List<DroppedItem> loadedItems = dropDataStorage.loadAllItems();
		panel.populateAllRows(loadedItems);
		getInjector().injectMembers(panel);

		final Optional<Plugin> mainPlugin = pluginManager.getPlugins().stream().filter(p -> p.getName().equals("Drop Log")).findFirst();
		if (mainPlugin.isPresent() && !pluginManager.isPluginEnabled(mainPlugin.get()))
		{
			pluginManager.setPluginEnabled(mainPlugin.get(), true);
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGING_IN) {
			getInjector().injectMembers(dropDataStorage);
			List<DroppedItem> loadedItems = dropDataStorage.loadAllItems();
			for (DroppedItem item : loadedItems) {
				int price = itemManager.getItemPrice(item.getId());
				item.setValue(price);
			}
			panel.populateAllRows(loadedItems);
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event) {
		String menuOption = event.getMenuOption();
		if (!"Drop".equalsIgnoreCase(menuOption) && !"Take".equalsIgnoreCase(menuOption)) {
			return;
		}
		String itemName = client.getItemDefinition(event.getItemId()).getName();
		if (isItemBlacklisted(itemName, config.blackListedItems())) {
			return;
		}
		switch (event.getMenuOption()) {
			case "Drop":
				pendingDrops.add(event.getItemId());
				break;
			case "Take":
				if (!config.removeOnPickUp()) {
					return;
				}
				pendingTakes.add(event.getId());
				break;
		}
	}

	@Subscribe
	public void onItemSpawned(ItemSpawned event) {
		TileItem item = event.getItem();
		if (pendingDrops.contains(item.getId())) {
			DroppedItem itemWithoutValue = new DroppedItem(item.getId(), item.getQuantity(), client.getItemDefinition(item.getId()).getName());
			DroppedItem itemWithValue = new DroppedItem(item.getId(), item.getQuantity(), client.getItemDefinition(item.getId()).getName(), itemManager.getItemPrice(item.getId()));
			SwingUtilities.invokeLater(() -> panel.droppedItem(itemWithValue));
			getInjector().injectMembers(dropDataStorage);
			dropDataStorage.saveItem(itemWithoutValue);

			pendingDrops.remove(Integer.valueOf(item.getId()));
		}
	}

	@Subscribe
	public void onItemDespawned(ItemDespawned event) {
		TileItem item = event.getItem();
		String itemName = client.getItemDefinition(item.getId()).getName();

		if (pendingTakes.contains(item.getId())) {
			getInjector().injectMembers(dropDataStorage);
			dropDataStorage.decreaseItemQuantity(itemName, item.getQuantity());

			DroppedItem itemToUpdate = new DroppedItem(item.getId(), item.getQuantity(), itemName); // Create a DroppedItem with quantity to decrease
			panel.removeDroppedItem(itemToUpdate);

			pendingTakes.remove(Integer.valueOf(item.getId()));
		}
	}

	public boolean isItemBlacklisted(String itemToCheck, String blacklistedItems) {
		String itemToCheckLower = itemToCheck.trim().toLowerCase();

		List<String> blackList = Arrays.stream(blacklistedItems.split(","))
				.map(String::trim)
				.map(String::toLowerCase)
				.collect(Collectors.toList());

		return blackList.contains(itemToCheckLower);
	}
}
