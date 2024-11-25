/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.player.track

import dev.arbjerg.lavalink.protocol.v4.Exception
import dev.arbjerg.lavalink.protocol.v4.Exception.Severity

/**
 * Represents an exception related to a track in the audio system.
 *
 * @property message A description of the error message.
 * @property severity The severity of the exception, indicating the level of impact on the system.
 * @property cause The cause of the exception, typically providing more context or error codes.
 * @author Miłosz Gilga
 */
data class TrackException(
	val message: String?,
	val severity: Severity,
	val cause: String,
) {

	companion object {
		/**
		 * Creates a [TrackException] instance from a distributed audio protocol exception.
		 *
		 * @param exception The [Exception] object from the distributed audio protocol.
		 * @return A new [TrackException] instance containing the error message, severity, and cause.
		 */
		fun fromProtocol(exception: Exception) = TrackException(exception.message, exception.severity, exception.cause)
	}
}
