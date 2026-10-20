package com.ascensionshardmining;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.WorldView;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

public class AscensionShardMiningOverlay extends Overlay
{
	private final Client client;
	private final AscensionShardMiningPlugin plugin;
	private final AscensionShardMiningConfig config;
	private final ModelOutlineRenderer modelOutlineRenderer;

	@Inject
	public AscensionShardMiningOverlay(
		Client client,
		AscensionShardMiningPlugin plugin,
		AscensionShardMiningConfig config,
		ModelOutlineRenderer modelOutlineRenderer)
	{
		super(plugin);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		this.modelOutlineRenderer = modelOutlineRenderer;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		final boolean highlightRocks = config.highlightRocks();
		final boolean highlightSprite = config.highlightSprite();

		if (!highlightRocks && !highlightSprite)
		{
			return null;
		}

		final Set<GameObject> activeRocks = plugin.getActiveRocks();
		final Set<NPC> sprites = plugin.getSprites();

		if ((!highlightRocks || activeRocks.isEmpty()) && (!highlightSprite || sprites.isEmpty()))
		{
			return null;
		}

		final WorldView worldView = client.getTopLevelWorldView();
		if (worldView == null)
		{
			return null;
		}

		final HighlightStyle style = config.highlightStyle();
		final int outlineWidth = config.outlineWidth();
		final Stroke stroke = (style != HighlightStyle.OUTLINE) ? new BasicStroke((float) outlineWidth) : null;
		final int currentPlane = worldView.getPlane();

		if (highlightRocks && !activeRocks.isEmpty())
		{
			final Color rockColor = config.rockColor();
			for (GameObject rock : activeRocks)
			{
				if (rock == null || rock.getPlane() != currentPlane)
				{
					continue;
				}

				renderGameObject(graphics, rock, rockColor, style, outlineWidth, stroke);
			}
		}

		if (highlightSprite && !sprites.isEmpty())
		{
			final Color spriteColor = config.spriteColor();

			for (NPC sprite : sprites)
			{
				if (sprite == null || sprite.isDead() || sprite.getWorldLocation().getPlane() != currentPlane)
				{
					continue;
				}

				renderNpc(graphics, sprite, spriteColor, style, outlineWidth, stroke);
			}
		}

		return null;
	}

	private void renderGameObject(Graphics2D graphics, GameObject object, Color color, HighlightStyle style, int outlineWidth, Stroke stroke)
	{
		switch (style)
		{
			case OUTLINE:
				modelOutlineRenderer.drawOutline(object, outlineWidth, color, 0);
				break;
			case HULL:
				Shape hull = object.getConvexHull();
				if (hull == null)
				{
					hull = object.getClickbox();
				}
				if (hull != null)
				{
					OverlayUtil.renderPolygon(graphics, hull, color, stroke);
				}
				break;
			case TILE:
				Polygon tilePoly = object.getCanvasTilePoly();
				if (tilePoly == null && object.getLocalLocation() != null)
				{
					tilePoly = Perspective.getCanvasTilePoly(client, object.getLocalLocation());
				}
				if (tilePoly != null)
				{
					OverlayUtil.renderPolygon(graphics, tilePoly, color, stroke);
				}
				break;
		}
	}

	private void renderNpc(Graphics2D graphics, NPC npc, Color color, HighlightStyle style, int outlineWidth, Stroke stroke)
	{
		switch (style)
		{
			case OUTLINE:
				modelOutlineRenderer.drawOutline(npc, outlineWidth, color, 0);
				break;
			case HULL:
				Shape hull = npc.getConvexHull();
				if (hull != null)
				{
					OverlayUtil.renderPolygon(graphics, hull, color, stroke);
				}
				break;
			case TILE:
				Polygon tilePoly = npc.getCanvasTilePoly();
				if (tilePoly == null && npc.getLocalLocation() != null)
				{
					tilePoly = Perspective.getCanvasTilePoly(client, npc.getLocalLocation());
				}
				if (tilePoly != null)
				{
					OverlayUtil.renderPolygon(graphics, tilePoly, color, stroke);
				}
				break;
		}
	}
}
