package me.kangarko.kagameapi.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import lombok.Getter;
import lombok.Setter;
import me.kangarko.kagameapi.Arena;
import me.kangarko.kagameapi.ArenaMessenger;
import me.kangarko.kagameapi.type.ArenaSound;
import me.kangarko.kagameapi.type.MessengerTarget;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public final class BasicMessenger implements ArenaMessenger {

	@Getter
	@Setter
	private MessengerTarget target = MessengerTarget.ARENA;

	private final Arena arena;

	public BasicMessenger(Arena arena) {
		this.arena = arena;
	}

	/**
	 * Only tells the directed players, with the player and other variables
	 */
	@Override
	public final void tell(Player player, String message) {
		player.sendMessage(replaceVariables(message.replace("{player}", player.getName())) );
	}

	public final void broadcastAndLog(String message) {
		broadcast0(getRecipients(), message, true);
	}

	public final void broadcastExcept(Player player, Player exception, String message) {
		final List<Player> receivers = new ArrayList<>(getRecipients());
		receivers.remove(exception);

		broadcast(receivers, message.replace("{player}", player.getName()));
	}

	/**
	 * Tells all players in the arena, replaces variables
	 */
	@Override
	public final void broadcast(String message) {
		broadcast0(getRecipients(), message, false);
	}

	public final void broadcast(Iterable<? extends CommandSender> toWhom, String message) {
		broadcast0(toWhom, message, false);
	}

	private final void broadcast0(Iterable<? extends CommandSender> toWhom, String message, boolean log) {
		message = replaceVariables(message);

		for (final CommandSender sender : toWhom)
			sender.sendMessage(message.replace("{player}", sender.getName()));

		if (log)
			Bukkit.getConsoleSender().sendMessage(message);
	}

	@Override
	public final void broadcastBar(String message) {
		final BaseComponent[] comp = TextComponent.fromLegacyText(replaceVariables(message));

		for (final Player pl : getRecipients())
			pl.spigot().sendMessage(ChatMessageType.ACTION_BAR, comp);
	}

	@Override
	public void playSound(ArenaSound sound, float pitch) {
		for (final Player pl : getRecipients())
			playSound(pl, sound, pitch);
	}

	@Override
	public void playSound(Player player, ArenaSound sound, float pitch) {
		player.playSound(player.getLocation(), Sound.valueOf(sound.toString()), 1F, pitch);
	}

	@Override
	public final String replaceVariables(String message) {
		return ChatColor.translateAlternateColorCodes('&', message
				.replace("{arena}", arena.getName())
				.replace("{state}", arena.getState().toString().toLowerCase())
				.replace("{phase}", arena.getPhase().getPhase() + "")
				.replace("{players}", getRecipients().size() + "")
				.replace("{maxPlayers}", arena.getSettings().getMaximumPlayers() + "")
				.replace("{minPlayers}", arena.getSettings().getMinimumPlayers() + "")
				);
	}

	private final Collection<? extends Player> getRecipients() {
		switch (target) {
			case ARENA:
				return arena.getPlayers();

			case WORLD:
				return arena.getData().getRegion().getCenter().getWorld().getPlayers();

			case GLOBAL:
				return Bukkit.getOnlinePlayers();

			default: throw new RuntimeException("Unhandled target " + target);
		}
	}
}