/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.event.player

import dev.arbjerg.lavalink.protocol.v4.Message.EmittedEvent.WebSocketClosedEvent
import pl.jwizard.jwac.node.AudioNode

/**
 * Event triggered when a WebSocket connection is closed.
 *
 * This event is emitted when the WebSocket connection for a specific audio node is closed. It provides information
 * about the closure, including the reason, code, and whether the closure was initiated remotely. This event is useful
 * for handling WebSocket disconnections or failures within the audio node context. It extends [EmittedEvent] to
 * integrate with the broader event system.
 *
 * @property audioNode The [AudioNode] associated with the WebSocket connection.
 * @property guildId The unique identifier of the guild where the WebSocket was closed.
 * @property code The code representing the reason for the WebSocket closure.
 * @property reason A description of the reason for the WebSocket closure.
 * @property byRemote A boolean indicating whether the WebSocket was closed remotely.
 * @author Miłosz Gilga
 */
data class KWsClosedEvent(
	override val audioNode: AudioNode,
	override val guildId: Long,
	val code: Int,
	val reason: String,
	val byRemote: Boolean,
) : EmittedEvent() {

	companion object {
		/**
		 * Creates a [KWsClosedEvent] instance from a protocol-specific [WebSocketClosedEvent].
		 *
		 * @param node The [AudioNode] responsible for the WebSocket connection.
		 * @param event The [WebSocketClosedEvent] received from the Lavalink protocol.
		 * @return A new instance of [KWsClosedEvent] representing the WebSocket closure details.
		 */
		fun fromProtocol(node: AudioNode, event: WebSocketClosedEvent) =
			KWsClosedEvent(node, event.guildId.toLong(), event.code, event.reason, event.byRemote)
	}
}
