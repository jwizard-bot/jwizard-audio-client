/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.event.player

import dev.arbjerg.lavalink.protocol.v4.Message.EmittedEvent.TrackEndEvent
import dev.arbjerg.lavalink.protocol.v4.Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason
import pl.jwizard.jwac.node.AudioNode
import pl.jwizard.jwac.player.track.Track

/**
 * Event triggered when a track finishes playback.
 *
 * This event is emitted when a track ends, either naturally or due to a specific stop reason. It includes details about
 * the track, the reason for its termination, and the context of the audio node and guild where it occurred. It extends
 * [EmittedEvent] to allow integration with the broader event system.
 *
 * @property audioNode The [AudioNode] responsible for handling the track playback.
 * @property guildId The unique identifier of the guild where the track ended.
 * @property track The [Track] that has finished playback.
 * @property endReason The [AudioTrackEndReason] specifying why the track ended.
 * @author Miłosz Gilga
 */
data class KTrackEndEvent(
	override val audioNode: AudioNode,
	override val guildId: Long,
	val track: Track,
	val endReason: AudioTrackEndReason,
) : EmittedEvent() {

	companion object {
		/**
		 * Creates a [KTrackEndEvent] instance from a protocol-specific [TrackEndEvent].
		 *
		 * @param node The [AudioNode] responsible for playback.
		 * @param event The [TrackEndEvent] received from the Lavalink protocol.
		 * @return A new instance of [KTrackEndEvent] representing the track end details.
		 */
		fun fromProtocol(node: AudioNode, event: TrackEndEvent) =
			KTrackEndEvent(node, event.guildId.toLong(), Track(event.track), event.reason)
	}
}
