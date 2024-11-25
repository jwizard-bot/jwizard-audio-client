/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.gateway

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel

/**
 * Interface to intercept and manage voice state changes for a member within a guild. This interface is meant to
 * provide methods to check if a member is in an audio channel, initiate a connection to an audio channel, and
 * disconnect from an audio channel.
 *
 * @author Miłosz Gilga
 */
interface GatewayVoiceStateInterceptor {

	/**
	 * Checks if the given member is currently in an audio channel.
	 *
	 * @param member The member whose voice state is being checked.
	 * @return A boolean indicating if the member is in an audio channel (`true` if they are, `false` if they are not,
	 *         or `null` if the information is unavailable).
	 */
	fun inAudioChannel(member: Member): Boolean?

	/**
	 * Initiates a connection for the specified guild and audio channel. This method is used to connect a bot or client
	 * to a particular voice channel in a specified guild.
	 *
	 * @param guild The guild where the connection should be made.
	 * @param channel The audio channel that the bot or client should connect to.
	 */
	fun makeConnect(guild: Guild, channel: AudioChannel)

	/**
	 * Disconnects the bot or client from the specified guild's audio channel. This method is used to disconnect from
	 * the voice channel in the given guild.
	 *
	 * @param guild The guild where the bot or client should disconnect from its voice channel.
	 */
	fun disconnect(guild: Guild)
}
