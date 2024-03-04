package org.woikaz;

import com.google.inject.Provides;
import javax.inject.Inject;
import javax.swing.*;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.events.MenuOptionClicked;
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

@Slf4j
@PluginDescriptor(
	name = "Drop Log"
)
public class DropLogPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private PluginManager pluginManager;

	private DropLogPanel panel;

	private NavigationButton navButton;

	private List<DroppedItem> initialInventory = new ArrayList<DroppedItem>();

	private boolean isPricesSet = false;

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
		DropDataStorage dropDataStorage = new DropDataStorage();
		getInjector().injectMembers(dropDataStorage);
		List<DroppedItem> loadedItems = dropDataStorage.loadAllItems();
		panel.populateAllRows(loadedItems);

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
		if (!isPricesSet && gameStateChanged.getGameState() == GameState.LOGGING_IN) {
			isPricesSet = true;
			DropDataStorage dropDataStorage = new DropDataStorage();
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
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (!Objects.equals(event.getMenuOption(), "Drop"))
		{
			return;
		}
		Optional<DroppedItem> foundItem = initialInventory.stream()
				.filter(item -> item.getId() == event.getItemId())
				.findFirst();
		if (foundItem.isPresent()) {
			DroppedItem item = foundItem.get();
			DroppedItem itemWithoutValue = new DroppedItem(item.getId(), item.getQuantity(), item.getName());
			SwingUtilities.invokeLater(() -> panel.droppedItem(item));
			DropDataStorage dropDataStorage = new DropDataStorage();
			getInjector().injectMembers(dropDataStorage);
			dropDataStorage.saveItem(itemWithoutValue);
		}
	}

	@Subscribe
	public void onItemSpawned(ItemSpawned event)
	{
		TileItem item = event.getItem();
		log.info(item.getId() + "");
		log.info(item.getQuantity() + "");
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() != InventoryID.INVENTORY.getId()) {
			return;
		}
		initialInventory.clear();
		for (Item item : event.getItemContainer().getItems()) {
			DroppedItem invItem = new DroppedItem(item.getId(), item.getQuantity(), client.getItemDefinition(item.getId()).getName(), itemManager.getItemPrice(item.getId()));
			initialInventory.add(invItem);
		}
	}
}
