/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.event.player

import dev.arbjerg.lavalink.protocol.v4.Message.EmittedEvent.TrackStuckEvent
import pl.jwizard.jwac.node.AudioNode
import pl.jwizard.jwac.player.track.Track

/**
 * Event triggered when a track gets stuck during playback.
 *
 * This event is emitted when the track playback gets stuck, typically when there is no audio being received for a
 * specified period. It provides information about the track, the audio node, the guild where the event occurred, and
 * the threshold duration in milliseconds. It extends [EmittedEvent] for integration with the broader event system.
 *
 * @property audioNode The [AudioNode] managing the track playback.
 * @property guildId The unique identifier of the guild where the track got stuck.
 * @property track The [Track] that is stuck during playback.
 * @property thresholdMs The duration in milliseconds after which the track is considered stuck.
 * @author Miłosz Gilga
 */
data class KTrackStuckEvent(
	override val audioNode: AudioNode,
	override val guildId: Long,
	val track: Track,
	val thresholdMs: Long,
) : EmittedEvent() {

	companion object {
		/**
		 * Creates a [KTrackStuckEvent] instance from a protocol-specific [TrackStuckEvent].
		 *
		 * @param node The [AudioNode] responsible for playback.
		 * @param event The [TrackStuckEvent] received from the audio protocol.
		 * @return A new instance of [KTrackStuckEvent] representing the track stuck event details.
		 */
		fun fromProtocol(node: AudioNode, event: TrackStuckEvent) =
			KTrackStuckEvent(node, event.guildId.toLong(), Track(event.track), event.thresholdMs)
	}
}
