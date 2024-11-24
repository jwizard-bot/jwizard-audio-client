/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.event.onload

import dev.arbjerg.lavalink.protocol.v4.LoadResult.SearchResult
import pl.jwizard.jwac.player.track.Track

/**
 * Event triggered when a search operation returns matching tracks.
 *
 * This event encapsulates the list of tracks that match the search query provided to the track loading system. It
 * implements the [KLoadResult] interface to allow for unified handling of different loading outcomes.
 *
 * @property tracks A list of tracks that matched the search query.
 * @author Miłosz Gilga
 */
data class KSearchResultEvent(val tracks: List<Track>) : KLoadResult {

	companion object {
		/**
		 * Creates a [KSearchResultEvent] instance from a protocol-specific [SearchResult] event.
		 *
		 * @param event The [SearchResult] event received from the audio protocol.
		 * @return A new instance of [KSearchResultEvent] containing the list of matching tracks.
		 */
		fun fromProtocol(event: SearchResult) = KSearchResultEvent(event.data.tracks.map { Track(it) })
	}
}
