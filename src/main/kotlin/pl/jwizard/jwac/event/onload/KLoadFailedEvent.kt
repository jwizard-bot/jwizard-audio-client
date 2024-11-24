/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.event.onload

import dev.arbjerg.lavalink.protocol.v4.LoadResult.LoadFailed
import pl.jwizard.jwac.player.track.TrackException

/**
 * Represents an event triggered when a track loading operation fails.
 *
 * This event is used to encapsulate details about the failure, including the specific exception that occurred during
 * the loading process.
 *
 * @property exception The exception detailing the reason for the loading failure.
 * @author Miłosz Gilga
 */
data class KLoadFailedEvent(val exception: TrackException) : KLoadResult {

	companion object {
		/**
		 * Creates a [KLoadFailedEvent] instance from a protocol-specific LoadFailed event.
		 *
		 * @param event The LoadFailed event from the Lavalink protocol.
		 * @return A new instance of [KLoadFailedEvent] containing the converted exception.
		 */
		fun fromProtocol(event: LoadFailed) = KLoadFailedEvent(TrackException.fromProtocol(event.data))
	}
}
