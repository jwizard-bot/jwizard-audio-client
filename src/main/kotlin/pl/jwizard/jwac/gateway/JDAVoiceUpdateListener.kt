/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.gateway

import dev.arbjerg.lavalink.protocol.v4.VoiceState
import net.dv8tion.jda.api.hooks.VoiceDispatchInterceptor
import pl.jwizard.jwac.AudioClient
import pl.jwizard.jwac.link.LinkState

/**
 * This class listens for voice server and voice state updates from Discord's Gateway. It updates the corresponding
 * voice state information and maintains the connection state for audio.
 *
 * @property audioClient The [AudioClient] instance responsible for managing the audio connections.
 * @author Miłosz Gilga
 */
class JDAVoiceUpdateListener(private val audioClient: AudioClient) : VoiceDispatchInterceptor {

	/**
	 * Handles updates related to the voice server information.
	 *
	 * This method is called when Discord sends a voice server update. It triggers an update to the audio client’s voice
	 * connection state for the specified guild.
	 *
	 * @param update Contains the voice server update information including the token, endpoint, and session ID.
	 */
	override fun onVoiceServerUpdate(update: VoiceDispatchInterceptor.VoiceServerUpdate) {
		val state = VoiceState(
			update.token,
			update.endpoint,
			update.sessionId
		)
		val link = audioClient.getOrCreateLink(update.guildIdLong)
		audioClient.voiceGatewayUpdateTrigger?.complete(null)
		link.updateNodeVoiceState(state)
	}

	/**
	 * Handles updates related to the voice state of a member.
	 *
	 * This method is called when the voice state of a member changes (e.g., joining or leaving a channel). It updates
	 * the connection state of the audio player, and if the member leaves the channel, it checks whether the player
	 * should be disconnected.
	 *
	 * @param update Contains the voice state update information, including the member's channel and connection state.
	 * @return A boolean indicating whether the player is still connected to a voice channel.
	 */
	override fun onVoiceStateUpdate(update: VoiceDispatchInterceptor.VoiceStateUpdate): Boolean {
		val channel = update.channel
		val link = audioClient.getLinkIfCached(update.guildIdLong) ?: return false
		val player = link.selectedNode.getCachedPlayer(update.guildIdLong) ?: return false
		val playerState = player.state

		if (channel == null) {
			val updatedLinkState = if (playerState.connected) {
				LinkState.CONNECTED
			} else {
				link.destroy().subscribe()
				LinkState.DISCONNECTED
			}
			link.updateState(updatedLinkState)
		}
		return playerState.connected
	}
}
