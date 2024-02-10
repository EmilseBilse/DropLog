package org.woikaz;

import com.google.inject.Provides;
import javax.inject.Inject;
import javax.swing.*;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemContainerChanged;
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
import org.woikaz.localstorage.CachedItem;
import org.woikaz.ui.DropLogPanel;

import java.awt.image.BufferedImage;
import java.util.*;

@Slf4j
@PluginDescriptor(
	name = "Drop Log"
)
public class ExamplePlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ExampleConfig config;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private PluginManager pluginManager;

	private DropLogPanel panel;

	private NavigationButton navButton;

	private List<CachedItem> initialInventory = new ArrayList<CachedItem>();

	@Override
	protected void startUp() throws Exception
	{
		log.info("Example started!");
		panel = new DropLogPanel(this);
		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/woikaz/droplog/delete-white.png");
		navButton = NavigationButton.builder()
				.tooltip("Drop Log")
				.icon(icon)
				.priority(6)
				.panel(panel)
				.build();

		clientToolbar.addNavigation(navButton);

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
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			// client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.greeting(), null);
		}
		// client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", gameStateChanged.getGameState().toString(), null);
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (!Objects.equals(event.getMenuOption(), "Drop"))
		{
			return;
		}
		Optional<CachedItem> foundItem = initialInventory.stream()
				.filter(item -> item.getId() == event.getItemId())
				.findFirst();
		if (foundItem.isPresent()) {
			CachedItem item = foundItem.get();
			// item.setId(itemManager.canonicalize(foundItem.get().getId()));
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Item id: " + item.getId() + " Item name: " + item.getName() + " Item quantity: " + item.getQuantity(), "");
			SwingUtilities.invokeLater(() -> panel.droppedItem(item));
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() != InventoryID.INVENTORY.getId()) {
			return;
		}
		initialInventory.clear();
		for (Item item : event.getItemContainer().getItems()) {
			CachedItem invItem = new CachedItem(item.getId(), item.getQuantity(), client.getItemDefinition(item.getId()).getName(), itemManager.getItemPrice(item.getId()));
			initialInventory.add(invItem);
		}

		// client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", event.getItemContainer().getItem(0).getQuantity() + "", null);
		// client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Item id: " + event.getContainerId() + Arrays.toString(event.getItemContainer().getItems()), null);
		// client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "", null);
		// log.info(Arrays.toString(event.getItemContainer().getItems()));
	}

	@Provides
	ExampleConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExampleConfig.class);
	}
}
