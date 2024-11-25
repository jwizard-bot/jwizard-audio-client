/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.ws

import dev.arbjerg.lavalink.protocol.v4.Message.*
import dev.arbjerg.lavalink.protocol.v4.Message.EmittedEvent.*

/**
 * An interface to handle audio WebSocket events for a bot system.
 *
 * This interface is used to define the methods that will be invoked when certain events occur over the WebSocket
 * connection, such as track updates, player state changes, and WebSocket connection lifecycle events.
 *
 * @author Miłosz Gilga
 */
internal interface AudioWsEvent {

	/**
	 * Handles the event when the WebSocket connection is ready.
	 *
	 * @param event The ready event data containing the status of the WebSocket connection.
	 */
	fun onReady(event: ReadyEvent)

	/**
	 * Handles the event containing the statistics of the audio node.
	 *
	 * @param event The statistics event data containing the audio node's statistics.
	 */
	fun onStats(event: StatsEvent)

	/**
	 * Handles the event when there is an update to the player's state.
	 *
	 * @param event The player update event containing the new state of the player.
	 */
	fun onPlayerUpdate(event: PlayerUpdateEvent)

	/**
	 * Handles the event when a track starts playing.
	 *
	 * @param event The track start event containing the track's details.
	 */
	fun onTrackStart(event: TrackStartEvent)

	/**
	 * Handles the event when a track ends.
	 *
	 * @param event The track end event containing details of the track that has ended.
	 */
	fun onTrackEnd(event: TrackEndEvent)

	/**
	 * Handles the event when the WebSocket connection is closed.
	 *
	 * @param event The WebSocket closed event containing the reason for the closure.
	 */
	fun onWsClosed(event: WebSocketClosedEvent)
}
