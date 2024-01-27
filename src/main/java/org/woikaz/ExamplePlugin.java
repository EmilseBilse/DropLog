package org.woikaz;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
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
import org.woikaz.ui.DropLogPanel;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

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

	@Override
	protected void startUp() throws Exception
	{
		log.info("Example started!");
		panel = new DropLogPanel(itemManager, this);
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
		log.info("Example stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.greeting(), null);
		}
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", gameStateChanged.getGameState().toString(), null);
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (!Objects.equals(event.getMenuOption(), "Drop"))
		{
			return;
		}
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Item id: " + event.getItemId(), null);
		// client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "client inv 0  " + client.getItemContainer(0), null);
		// client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "client inv?  " + client.getItemContainer(event.getParam0()), null);
		// client.getItemContainer(event.getParam0());
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		// client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Item id: " + event.getContainerId() + Arrays.toString(event.getItemContainer().getItems()), null);
		// log.info(Arrays.toString(event.getItemContainer().getItems()));
	}

	@Provides
	ExampleConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExampleConfig.class);
	}
}
