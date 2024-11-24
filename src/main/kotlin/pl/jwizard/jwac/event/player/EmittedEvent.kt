/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.event.player

import pl.jwizard.jwac.event.ClientEvent

/**
 * Base class for events emitted by the player system.
 *
 * This abstract class serves as a foundation for all events related to the player system. It extends [ClientEvent],
 * allowing integration with client-side event handling mechanisms. Each emitted event is associated with a specific
 * guild, identified by its unique ID.
 *
 * @property guildId The unique identifier of the guild where the event was emitted.
 * @author Miłosz Gilga
 */
abstract class EmittedEvent : ClientEvent() {

	/**
	 * The unique identifier of the guild where the event occurred.
	 */
	abstract val guildId: Long
}
