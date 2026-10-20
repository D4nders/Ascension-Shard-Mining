package com.ascensionshardmining;

import com.google.inject.Provides;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Ascension Shard Mining",
	description = "Highlights active Ascension rocks and sprites for Ascension shard mining",
	tags = {"mining", "ascension", "shard", "rock", "sprite"}
)
public class AscensionShardMiningPlugin extends Plugin
{
	// Gameval constants are not yet available in net.runelite.api.gameval for Ascension Shard Mining
	private static final int ASCENSION_ROCK_ACTIVE_1 = 63266;
	private static final int ASCENSION_ROCK_ACTIVE_2 = 63267;
	private static final int ASCENSION_ROCK_ACTIVE_3 = 63268;

	private static final int ASCENSION_ROCK_DEPLETED_1 = 63269;
	private static final int ASCENSION_ROCK_DEPLETED_2 = 63270;
	private static final int ASCENSION_ROCK_DEPLETED_3 = 63271;

	private static final int SPRITE_ACTIVE_ID = 16691;

	private static final Set<Integer> ACTIVE_ROCK_IDS = Set.of(
		ASCENSION_ROCK_ACTIVE_1,
		ASCENSION_ROCK_ACTIVE_2,
		ASCENSION_ROCK_ACTIVE_3
	);

	private static final Set<Integer> DEPLETED_ROCK_IDS = Set.of(
		ASCENSION_ROCK_DEPLETED_1,
		ASCENSION_ROCK_DEPLETED_2,
		ASCENSION_ROCK_DEPLETED_3
	);

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private AscensionShardMiningConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private AscensionShardMiningOverlay overlay;

	@Inject
	private Notifier notifier;

	@Getter(AccessLevel.PACKAGE)
	private final Set<GameObject> activeRocks = Collections.newSetFromMap(new ConcurrentHashMap<>());

	@Getter(AccessLevel.PACKAGE)
	private final Set<NPC> sprites = Collections.newSetFromMap(new ConcurrentHashMap<>());

	private WorldPoint targetRockLocation;

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);

		if (client.getGameState() == GameState.LOGGED_IN)
		{
			clientThread.invokeLater(() ->
			{
				WorldView worldView = client.getTopLevelWorldView();
				if (worldView != null)
				{
					for (NPC npc : worldView.npcs())
					{
						if (isSprite(npc))
						{
							sprites.add(npc);
						}
					}
				}
			});
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		activeRocks.clear();
		sprites.clear();
		targetRockLocation = null;
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		int id = event.getId();
		if (ACTIVE_ROCK_IDS.contains(id))
		{
			WorldView wv = client.getTopLevelWorldView();
			if (wv != null)
			{
				targetRockLocation = WorldPoint.fromScene(wv, event.getParam0(), event.getParam1(), wv.getPlane());
				log.debug("Set target rock at {}", targetRockLocation);
			}
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		GameObject gameObject = event.getGameObject();
		if (gameObject == null)
		{
			return;
		}

		int id = gameObject.getId();
		if (ACTIVE_ROCK_IDS.contains(id))
		{
			activeRocks.add(gameObject);
		}
		else if (DEPLETED_ROCK_IDS.contains(id))
		{
			activeRocks.removeIf(rock ->
				rock.getWorldLocation().equals(gameObject.getWorldLocation())
				&& rock.getPlane() == gameObject.getPlane());

			checkRockDepleted(gameObject.getWorldLocation());
		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		GameObject gameObject = event.getGameObject();
		if (gameObject == null)
		{
			return;
		}

		activeRocks.remove(gameObject);

		if (ACTIVE_ROCK_IDS.contains(gameObject.getId()))
		{
			checkRockDepleted(gameObject.getWorldLocation());
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		NPC npc = event.getNpc();
		if (isSprite(npc))
		{
			sprites.add(npc);
			log.debug("Ascension sprite spawned: {}", npc);
			notifier.notify(config.notifySpriteAppeared(), "A Sprite has appeared!");
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		NPC npc = event.getNpc();
		if (npc != null)
		{
			sprites.remove(npc);
		}
	}

	@Subscribe
	public void onNpcChanged(NpcChanged event)
	{
		NPC npc = event.getNpc();
		if (isSprite(npc))
		{
			sprites.add(npc);

			if (event.getOld() == null || event.getOld().getId() != SPRITE_ACTIVE_ID)
			{
				log.debug("Ascension sprite spawned: {}", npc);
				notifier.notify(config.notifySpriteAppeared(), "A Sprite has appeared!");
			}
		}
		else
		{
			sprites.remove(npc);
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		GameState gameState = event.getGameState();
		if (gameState == GameState.LOADING
			|| gameState == GameState.LOGIN_SCREEN
			|| gameState == GameState.HOPPING)
		{
			activeRocks.clear();
			sprites.clear();
			targetRockLocation = null;
		}
	}

	private void checkRockDepleted(WorldPoint location)
	{
		if (targetRockLocation != null && targetRockLocation.equals(location))
		{
			log.debug("Target rock depleted at {}", location);
			targetRockLocation = null;
			notifier.notify(config.notifyRockDepleted(), "Your Ascension rock has depleted");
		}
	}

	private static boolean isSprite(NPC npc)
	{
		return npc != null && npc.getId() == SPRITE_ACTIVE_ID;
	}

	@Provides
	AscensionShardMiningConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AscensionShardMiningConfig.class);
	}
}
