/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.event.player

import dev.arbjerg.lavalink.protocol.v4.Message.EmittedEvent.TrackExceptionEvent
import pl.jwizard.jwac.node.AudioNode
import pl.jwizard.jwac.player.track.Track
import pl.jwizard.jwac.player.track.TrackException

/**
 * Event triggered when a track encounters an exception during playback.
 *
 * This event is emitted when an error occurs during track playback, providing details about the affected track, the
 * nature of the exception, and the context in which the error occurred. It extends [EmittedEvent] to integrate with the
 * broader event-handling system.
 *
 * @property audioNode The [AudioNode] responsible for handling the track playback.
 * @property guildId The unique identifier of the guild where the exception occurred.
 * @property track The [Track] associated with the exception.
 * @property exception The [TrackException] describing the error that occurred during playback.
 * @author Miłosz Gilga
 */
data class KTrackExceptionEvent(
	override val audioNode: AudioNode,
	override val guildId: Long,
	val track: Track,
	val exception: TrackException,
) : EmittedEvent() {

	companion object {
		/**
		 * Creates a [KTrackExceptionEvent] instance from a protocol-specific [TrackExceptionEvent].
		 *
		 * @param node The [AudioNode] responsible for playback.
		 * @param event The [TrackExceptionEvent] received from the audio protocol.
		 * @return A new instance of [KTrackExceptionEvent] representing the playback error details.
		 */
		fun fromProtocol(node: AudioNode, event: TrackExceptionEvent) = KTrackExceptionEvent(
			node,
			guildId = event.guildId.toLong(),
			track = Track(event.track),
			exception = TrackException.fromProtocol(event.exception),
		)
	}
}
