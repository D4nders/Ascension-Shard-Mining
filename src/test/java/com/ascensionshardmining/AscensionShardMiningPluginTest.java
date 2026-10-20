package com.ascensionshardmining;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class AscensionShardMiningPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(AscensionShardMiningPlugin.class);
		RuneLite.main(args);
	}
}
