/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.event

import dev.arbjerg.lavalink.protocol.v4.Message.ReadyEvent
import pl.jwizard.jwac.node.AudioNode

/**
 * Event triggered when the audio node is ready or has resumed.
 *
 * This event is emitted when the audio node has successfully initialized or resumed its operation. It contains
 * information about whether the connection was resumed and the session ID used for the connection. This event is
 * typically used to confirm that the audio node is ready to handle further requests or to notify the system about the
 * resumption of a previously interrupted connection.
 *
 * @property audioNode The [AudioNode] that has been initialized or resumed.
 * @property resumed A boolean indicating whether the session was resumed (as opposed to a fresh start).
 * @property sessionId The session ID associated with the audio node.
 * @author Miłosz Gilga
 */
data class KReadyEvent(
	override val audioNode: AudioNode,
	val resumed: Boolean,
	val sessionId: String,
) : ClientEvent() {

	companion object {
		/**
		 * Creates a [KReadyEvent] instance from a protocol-specific [ReadyEvent].
		 *
		 * @param node The [AudioNode] responsible for the readiness or resumption event.
		 * @param event The [ReadyEvent] received from the audio protocol.
		 * @return A new instance of [KReadyEvent] representing the readiness or resumption event.
		 */
		fun fromProtocol(node: AudioNode, event: ReadyEvent) = KReadyEvent(node, event.resumed, event.sessionId)
	}
}
