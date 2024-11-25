/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.ws

import dev.arbjerg.lavalink.protocol.v4.Message.*
import dev.arbjerg.lavalink.protocol.v4.Message.EmittedEvent.*
import pl.jwizard.jwac.AudioClient
import pl.jwizard.jwac.link.LinkState
import pl.jwizard.jwac.node.AudioNode
import pl.jwizard.jwac.player.track.Track
import pl.jwizard.jwac.util.isEmpty
import pl.jwizard.jwac.util.logger
import dev.arbjerg.lavalink.protocol.v4.Track as ProtocolTrack

/**
 * A handler for WebSocket events related to audio playback.
 *
 * This class listens for events like node readiness, player updates, track starts, and track ends. It updates the
 * state of the associated players, manages the WebSocket connection, and triggers actions based on the event types
 * received from the audio node.
 *
 * @property audioNode The audio node to which this client is connected.
 * @property audioClient The audio client that manages player states and interactions.
 * @author Miłosz Gilga
 */
internal class AudioWsEventImpl(
	private val audioNode: AudioNode,
	private val audioClient: AudioClient,
) : AudioWsEvent {

	companion object {
		private val log = logger<AudioWsEventImpl>()
	}

	/**
	 * WebSocket ending codes that indicate the connection was closed due to specific errors.
	 */
	private val wsEndingCodes = listOf(4004, 4009)

	/**
	 * Updates the audio node's session ID, sets it as available, and transfers orphaned players to the current node.
	 * Also, ensures that all players connected to the node are in a valid state.
	 *
	 * @param event The [ReadyEvent] received from the WebSocket.
	 */
	override fun onReady(event: ReadyEvent) {
		if (!event.resumed) {
			audioNode.penalties.resetMetrics()
		}
		audioNode.sessionId = event.sessionId
		audioNode.available = true

		log.info("Node: {} is ready with session id: {}.", audioNode, event.sessionId)

		for (player in audioNode.players.values) {
			if (player.voiceState.isEmpty()) {
				continue
			}
			player.stateToBuilder()
				.setNoReplace(false)
				.subscribe()
		}
		audioNode.transferOrphansToSelf()
	}

	/**
	 * Updates the audio node's statistics with the received stats.
	 *
	 * @param event The [StatsEvent] received from the WebSocket.
	 */
	override fun onStats(event: StatsEvent) {
		audioNode.stats = event
	}

	/**
	 * Updates the player's state and the link state (whether the player is connected or disconnected).
	 *
	 * @param event The [PlayerUpdateEvent] received from the WebSocket.
	 */
	override fun onPlayerUpdate(event: PlayerUpdateEvent) {
		val guildIdLong = event.guildId.toLong()

		val player = audioNode.getCachedPlayer(guildIdLong)
		val link = audioClient.getLinkIfCached(guildIdLong)

		val linkState = if (event.state.connected) {
			LinkState.CONNECTED
		} else {
			LinkState.DISCONNECTED
		}
		player?.updateState(event.state)
		link?.updateState(linkState)
	}

	/**
	 * Updates the player's current track based on the received event.
	 *
	 * @param event The [TrackStartEvent] received from the WebSocket.
	 */
	override fun onTrackStart(event: TrackStartEvent) = updateTrack(event, event.track)

	/**
	 * Updates the player's current track to `null` indicating that the track has ended.
	 *
	 * @param event The [TrackEndEvent] received from the WebSocket.
	 */
	override fun onTrackEnd(event: TrackEndEvent) = updateTrack(event, null)

	/**
	 * If the connection was closed due to certain error codes, it destroys the player and link for the corresponding
	 * guild.
	 *
	 * @param event The [WebSocketClosedEvent] received from the WebSocket.
	 */
	override fun onWsClosed(event: WebSocketClosedEvent) {
		log.debug("Close WS connection with code: {}. Cause: {}. By remote: {}.", event.code, event.reason, event.byRemote)
		if (wsEndingCodes.contains(event.code)) {
			log.debug("Node: {} received close code: {} for guild: {}.", audioNode, event.code, event.guildId)
			audioNode.destroyPlayerAndLink(event.guildId.toLong()).subscribe()
		}
	}

	/**
	 * If the track is `null`, it indicates that the track has ended, so the player's track is cleared.
	 *
	 * @param event The emitted event, such as [TrackStartEvent] or [TrackEndEvent].
	 * @param track The track to update, or `null` if the track has ended.
	 */
	private fun updateTrack(event: EmittedEvent, track: ProtocolTrack?) {
		val player = audioNode.getCachedPlayer(event.guildId.toLong())
		player?.updateTrack(if (track == null) null else Track(track))
	}
}
