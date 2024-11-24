/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.event.onload

import dev.arbjerg.lavalink.protocol.v4.LoadResult.PlaylistLoaded
import dev.arbjerg.lavalink.protocol.v4.PlaylistInfo
import kotlinx.serialization.json.JsonObject
import pl.jwizard.jwac.player.track.Track

/**
 * Event triggered when a playlist is successfully loaded.
 *
 * This event encapsulates information about the loaded playlist, including metadata, plugin-specific details, and the
 * list of tracks contained in the playlist. It implements  the [KLoadResult] interface to provide a unified result type
 * for handling track loading outcomes.
 *
 * @property info Metadata about the loaded playlist, such as its name or type.
 * @property pluginInfo Additional plugin-specific information as a JSON object.
 * @property tracks A list of tracks included in the loaded playlist.
 * @author Miłosz Gilga
 */
data class KPlaylistLoadedEvent(
	val info: PlaylistInfo,
	val pluginInfo: JsonObject,
	val tracks: List<Track>,
) : KLoadResult {

	companion object {
		/**
		 * Creates a [KPlaylistLoadedEvent] instance from a protocol-specific [PlaylistLoaded] event.
		 *
		 * @param event The [PlaylistLoaded] event received from the Lavalink protocol.
		 * @return A new instance of [KPlaylistLoadedEvent] containing the playlist data.
		 */
		fun fromProtocol(event: PlaylistLoaded) =
			KPlaylistLoadedEvent(event.data.info, event.data.pluginInfo, event.data.tracks.map { Track(it) })
	}
}
