/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.event

import pl.jwizard.jwac.node.AudioNode

/**
 * Base class for client events related to audio node actions.
 *
 * This is an abstract class that serves as a base for all client events that are related to actions involving an
 * [AudioNode]. It provides a common interface for events that carry information about the audio node where the event
 * occurred. Subclasses should define specific event details.
 *
 * @property audioNode The [AudioNode] associated with the event. Represents node where the event has been emitted.
 * @author Miłosz Gilga
 */
abstract class ClientEvent {

	/**
	 * The audio node associated with the event.
	 */
	abstract val audioNode: AudioNode
}
