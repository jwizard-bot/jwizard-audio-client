/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.event

import dev.arbjerg.lavalink.protocol.v4.Message.PlayerUpdateEvent
import dev.arbjerg.lavalink.protocol.v4.PlayerState
import pl.jwizard.jwac.node.AudioNode

/**
 * Event triggered when the player's state is updated.
 *
 * This event is emitted when there is an update to the player's state on the audio node. It carries information about
 * the guild and the updated player state. This event is used to notify other parts of the system about changes in the
 * player's playback state, such as whether the player is playing, paused, or stopped.
 *
 * @property audioNode The [AudioNode] associated with the player whose state was updated.
 * @property guildId The unique identifier of the guild where the player update occurred.
 * @property state The updated [PlayerState] representing the current state of the player.
 * @author Miłosz Gilga
 */
data class KPlayerUpdateEvent(
	override val audioNode: AudioNode,
	val guildId: Long,
	val state: PlayerState,
) : ClientEvent() {

	companion object {
		/**
		 * Creates a [KPlayerUpdateEvent] instance from a protocol-specific [PlayerUpdateEvent].
		 *
		 * @param node The [AudioNode] responsible for the player.
		 * @param event The [PlayerUpdateEvent] received from the Lavalink protocol.
		 * @return A new instance of [KPlayerUpdateEvent] representing the player update event.
		 */
		fun fromProtocol(node: AudioNode, event: PlayerUpdateEvent) =
			KPlayerUpdateEvent(node, event.guildId.toLong(), event.state)
	}
}
