/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac

import pl.jwizard.jwac.event.player.*

/**
 * Interface defining event listeners for audio node-related events. These events provide callbacks for key actions
 * such as track start, track end, connection issues, and exceptions during track playback.
 *
 * @author Miłosz Gilga
 */
interface AudioNodeListener {

	/**
	 * Called when a track starts playing on an audio node.
	 *
	 * @param event The event containing information about the started track.
	 */
	fun onTrackStart(event: KTrackStartEvent)

	/**
	 * Called when a track ends playing on an audio node.
	 *
	 * @param event The event containing information about the finished track, including the reason.
	 */
	fun onTrackEnd(event: KTrackEndEvent)

	/**
	 * Called when a track gets stuck due to a playback issue on an audio node.
	 *
	 * @param event The event containing information about the track that got stuck.
	 */
	fun onTrackStuck(event: KTrackStuckEvent)

	/**
	 * Called when an exception occurs while playing a track on an audio node.
	 *
	 * @param event The event containing details about the exception encountered.
	 */
	fun onTrackException(event: KTrackExceptionEvent)

	/**
	 * Called when the WebSocket connection to an audio node is closed.
	 *
	 * @param event The event containing details about the WebSocket closure, such as the close code and reason.
	 */
	fun onCloseWsConnection(event: KWsClosedEvent)
}
