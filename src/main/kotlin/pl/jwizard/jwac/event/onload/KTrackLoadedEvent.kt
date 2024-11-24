/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.event.onload

import dev.arbjerg.lavalink.protocol.v4.LoadResult.TrackLoaded
import pl.jwizard.jwac.player.track.Track

/**
 * Event triggered when a single track is successfully loaded.
 *
 * This event encapsulates the details of a track that has been loaded as part of a track loading operation. It
 * implements the [KLoadResult] interface to provide a unified type for handling track loading outcomes.
 *
 * @property track The loaded track containing metadata and playback information.
 * @author Miłosz Gilga
 */
data class KTrackLoadedEvent(val track: Track) : KLoadResult {

	companion object {
		/**
		 * Creates a [KTrackLoadedEvent] instance from a protocol-specific [TrackLoaded] result.
		 *
		 * @param result The [TrackLoaded] result received from the audio protocol.
		 * @return A new instance of [KTrackLoadedEvent] containing the loaded track.
		 */
		fun fromProtocol(result: TrackLoaded) = KTrackLoadedEvent(Track(result.data))
	}
}
