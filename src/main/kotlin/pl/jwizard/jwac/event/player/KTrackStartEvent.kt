/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.event.player

import dev.arbjerg.lavalink.protocol.v4.Message.EmittedEvent.TrackStartEvent
import pl.jwizard.jwac.node.AudioNode
import pl.jwizard.jwac.player.track.Track

/**
 * Event triggered when a track starts playback.
 *
 * This event is emitted when a track begins playing on the audio node for a specific guild. It provides details about
 * the track being played, the guild where the playback is occurring, and the context of the audio node responsible for
 * playback. It extends [EmittedEvent] for seamless integration with the broader event system.
 *
 * @property audioNode The [AudioNode] managing the track playback.
 * @property guildId The unique identifier of the guild where the track is being played.
 * @property track The [Track] that has started playback.
 * @author Miłosz Gilga
 */
data class KTrackStartEvent(
	override val audioNode: AudioNode,
	override val guildId: Long,
	val track: Track,
) : EmittedEvent() {

	companion object {
		/**
		 * Creates a [KTrackStartEvent] instance from a protocol-specific [TrackStartEvent].
		 *
		 * @param node The [AudioNode] responsible for playback.
		 * @param event The [TrackStartEvent] received from the Lavalink protocol.
		 * @return A new instance of [KTrackStartEvent] representing the track start details.
		 */
		fun fromProtocol(node: AudioNode, event: TrackStartEvent) =
			KTrackStartEvent(node, event.guildId.toLong(), Track(event.track))
	}
}
