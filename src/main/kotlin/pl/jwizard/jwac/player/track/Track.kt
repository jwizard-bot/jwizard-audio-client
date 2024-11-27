/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.player.track

import kotlinx.serialization.json.jsonObject
import pl.jwizard.jwac.util.fromJsonElement
import pl.jwizard.jwac.util.toJsonElement
import pl.jwizard.jwac.util.toJsonObject
import dev.arbjerg.lavalink.protocol.v4.Track as ProtocolTrack

/**
 * Represents an audio track used in the system.
 *
 * This class wraps around the [ProtocolTrack] from the Lavalink protocol and provides additional utility methods for
 * managing and interacting with track information, including metadata, user data, and audio-related properties.
 *
 * @property protocolTrack The underlying track data from the Lavalink protocol.
 * @author Miłosz Gilga
 */
class Track(private var protocolTrack: ProtocolTrack) {

	val encoded = protocolTrack.encoded
	val uri = protocolTrack.info.uri
	val thumbnailUrl = protocolTrack.info.artworkUrl
	val duration = protocolTrack.info.length
	val sourceName = protocolTrack.info.sourceName

	internal val userData = protocolTrack.userData

	/**
	 * Extracts the audio sender data from the user data.
	 */
	val audioSender
		get() = fromJsonElement<AudioSender>(protocolTrack.userData)

	/**
	 * Returns the title of the track, optionally normalized.
	 *
	 * If `normalized` is set to `true`, the title will be modified to remove any special characters (like asterisks)
	 * and append the author's name in parentheses.
	 *
	 * @param normalized Whether to normalize the title by removing special characters and appending the author's name.
	 * @return The title of the track, with or without normalization.
	 */
	fun getTitle(normalized: Boolean = false): String {
		var title = protocolTrack.info.title
		if (normalized) {
			title = "${title.replace("*", "")} (${protocolTrack.info.author})"
		}
		return title
	}

	/**
	 * Creates a clone of the track with its position reset to zero.
	 *
	 * @return A new [Track] instance that is a clone of the current track.
	 */
	fun makeClone() = Track(
		protocolTrack.copy(
			info = protocolTrack.info.copy(position = 0L),
			userData = toJsonObject(protocolTrack.userData)
		)
	)

	/**
	 * Sets custom sender data for the track.
	 *
	 * @param userData The custom sender data to be associated with the track.
	 */
	fun setSenderData(userData: AudioSender?) {
		val jsonElement = toJsonElement(userData)
		protocolTrack = protocolTrack.copyWithUserData(jsonElement.jsonObject)
	}
}
