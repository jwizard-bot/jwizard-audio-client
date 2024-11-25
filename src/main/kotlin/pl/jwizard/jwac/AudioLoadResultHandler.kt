/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac

import dev.arbjerg.lavalink.protocol.v4.LoadResult
import pl.jwizard.jwac.event.onload.*
import java.util.function.Consumer

/**
 * A handler that processes different types of audio load results received from a distributed audio server. This handler
 * consumes the [LoadResult] and triggers specific actions based on the type of result.
 *
 * @author Miłosz Gilga
 */
abstract class AudioLoadResultHandler : Consumer<LoadResult> {

	/**
	 * Accepts a [LoadResult] and processes it based on its type.
	 *
	 * This method is called when a load result from the distributed audio server is received. The result is dispatched
	 * to specific handling methods based on the type of load result (ex. track loaded, playlist loaded, etc.).
	 *
	 * @param loadResult The result of a load request.
	 */
	override fun accept(loadResult: LoadResult) {
		when (loadResult) {
			is LoadResult.TrackLoaded -> onTrackLoaded(KTrackLoadedEvent.fromProtocol(loadResult))
			is LoadResult.LoadFailed -> loadFailed(KLoadFailedEvent.fromProtocol(loadResult))
			is LoadResult.NoMatches -> noMatches(KNoMatchesEvent())
			is LoadResult.PlaylistLoaded -> onPlaylistLoaded(KPlaylistLoadedEvent.fromProtocol(loadResult))
			is LoadResult.SearchResult -> onSearchResultLoaded(KSearchResultEvent.fromProtocol(loadResult))
		}
	}

	/**
	 * Called when a track is successfully loaded from the distributed audio server.
	 *
	 * @param result The event object containing information about the loaded track.
	 */
	protected abstract fun onTrackLoaded(result: KTrackLoadedEvent)

	/**
	 * Called when a playlist is successfully loaded from the distributed audio server.
	 *
	 * @param result The event object containing information about the loaded playlist.
	 */
	protected abstract fun onPlaylistLoaded(result: KPlaylistLoadedEvent)

	/**
	 * Called when search results are successfully loaded from the distributed audio server.
	 *
	 * @param result The event object containing the search results.
	 */
	protected abstract fun onSearchResultLoaded(result: KSearchResultEvent)

	/**
	 * Called when no matches are found for the load request.
	 *
	 * @param result The event object indicating that no matches were found.
	 */
	protected abstract fun noMatches(result: KNoMatchesEvent)

	/**
	 * Called when the load request fails (ex. invalid track or playlist).
	 *
	 * @param result The event object containing details of the failed load request.
	 */
	protected abstract fun loadFailed(result: KLoadFailedEvent)
}
