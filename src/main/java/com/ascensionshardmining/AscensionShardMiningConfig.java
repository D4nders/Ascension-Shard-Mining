package com.ascensionshardmining;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Notification;
import net.runelite.client.config.Range;

@ConfigGroup("ascension-shard-mining")
public interface AscensionShardMiningConfig extends Config
{
	@ConfigSection(
		name = "Rocks",
		description = "Options for Ascension rock highlights",
		position = 0
	)
	String rocksSection = "rocksSection";

	@ConfigItem(
		keyName = "highlightRocks",
		name = "Highlight rocks",
		description = "Configures whether or not to highlight active Ascension rocks",
		position = 1,
		section = rocksSection
	)
	default boolean highlightRocks()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "rockColor",
		name = "Rock color",
		description = "Color to highlight active Ascension rocks",
		position = 2,
		section = rocksSection
	)
	default Color rockColor()
	{
		return Color.CYAN;
	}

	@ConfigSection(
		name = "Sprite",
		description = "Options for Ascension sprite highlights",
		position = 10
	)
	String spriteSection = "spriteSection";

	@ConfigItem(
		keyName = "highlightSprite",
		name = "Highlight sprite",
		description = "Configures whether or not to highlight active Ascension sprites",
		position = 11,
		section = spriteSection
	)
	default boolean highlightSprite()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "spriteColor",
		name = "Sprite color",
		description = "Color to highlight active Ascension sprites",
		position = 12,
		section = spriteSection
	)
	default Color spriteColor()
	{
		return Color.MAGENTA;
	}

	@ConfigSection(
		name = "Rendering",
		description = "Options for rendering highlights",
		position = 20
	)
	String renderingSection = "renderingSection";

	@ConfigItem(
		keyName = "highlightStyle",
		name = "Highlight style",
		description = "Highlight style for rocks and sprites (Outline, Hull, or Tile)",
		position = 21,
		section = renderingSection
	)
	default HighlightStyle highlightStyle()
	{
		return HighlightStyle.OUTLINE;
	}

	@Range(min = 1, max = 10)
	@ConfigItem(
		keyName = "outlineWidth",
		name = "Outline width",
		description = "Width of the highlight outline or border stroke",
		position = 22,
		section = renderingSection
	)
	default int outlineWidth()
	{
		return 2;
	}

	@ConfigSection(
		name = "Notifications",
		description = "Options for tray notifications",
		position = 30
	)
	String notificationsSection = "notificationsSection";

	@ConfigItem(
		keyName = "notifyRockDepleted",
		name = "Rock depleted",
		description = "Sends a notification when your Ascension rock has depleted",
		position = 31,
		section = notificationsSection
	)
	default Notification notifyRockDepleted()
	{
		return Notification.OFF;
	}

	@ConfigItem(
		keyName = "notifySpriteAppeared",
		name = "Sprite appeared",
		description = "Sends a notification when an Ascension sprite appears",
		position = 33,
		section = notificationsSection
	)
	default Notification notifySpriteAppeared()
	{
		return Notification.OFF;
	}
}
