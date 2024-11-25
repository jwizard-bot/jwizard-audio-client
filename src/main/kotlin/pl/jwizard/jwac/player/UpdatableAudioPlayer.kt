/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.player

import dev.arbjerg.lavalink.protocol.v4.Filters
import dev.arbjerg.lavalink.protocol.v4.PlayerUpdateTrack
import dev.arbjerg.lavalink.protocol.v4.VoiceState
import pl.jwizard.jwac.player.track.Track

/**
 * Interface representing an audio player that can be updated with new configurations or actions.
 *
 * @author Miłosz Gilga
 */
interface UpdatableAudioPlayer {

	/**
	 * Sets a new track to be played.
	 *
	 * @param track The new track to play. If the track is null, it will remove the current track.
	 * @return An [AudioPlayerUpdateBuilder] to build the player update with this track.
	 */
	fun setTrack(track: Track?): AudioPlayerUpdateBuilder

	/**
	 * Updates the current track with a new [PlayerUpdateTrack] object.
	 *
	 * @param track The updated track information.
	 * @return An [AudioPlayerUpdateBuilder] to build the player update with the new track.
	 */
	fun updateTrack(track: PlayerUpdateTrack): AudioPlayerUpdateBuilder

	/**
	 * Stops the current track, removing it from the player.
	 *
	 * @return An [AudioPlayerUpdateBuilder] to build the player update indicating the track has stopped.
	 */
	fun stopTrack(): AudioPlayerUpdateBuilder

	/**
	 * Sets the playback position of the current track.
	 *
	 * @param position The position (in milliseconds) to seek to in the current track. If `null`, the position will not
	 *        be updated.
	 * @return An [AudioPlayerUpdateBuilder] to build the player update with the new position.
	 */
	fun setPosition(position: Long?): AudioPlayerUpdateBuilder

	/**
	 * Sets the end time of the current track.
	 *
	 * @param endTime The end time (in milliseconds) to stop the track. If `null`, the end time will not be set.
	 * @return An [AudioPlayerUpdateBuilder] to build the player update with the new end time.
	 */
	fun setEndTime(endTime: Long?): AudioPlayerUpdateBuilder

	/**
	 * Removes the end time of the current track.
	 *
	 * @return An [AudioPlayerUpdateBuilder] to build the player update removing the end time.
	 */
	fun omitEndTime(): AudioPlayerUpdateBuilder

	/**
	 * Sets the volume of the audio player.
	 *
	 * @param volume The volume level to set. Valid values range from 0 to 1000.
	 * @return An [AudioPlayerUpdateBuilder] to build the player update with the new volume.
	 */
	fun setVolume(volume: Int): AudioPlayerUpdateBuilder

	/**
	 * Sets the paused state of the audio player.
	 *
	 * @param paused If `true`, the audio will be paused; if `false`, it will resume playing.
	 * @return An [AudioPlayerUpdateBuilder] to build the player update with the new paused state.
	 */
	fun setPaused(paused: Boolean): AudioPlayerUpdateBuilder

	/**
	 * Sets the filters for the audio player.
	 *
	 * @param filters The filters to apply, such as equalizer settings or special audio effects.
	 * @return An [AudioPlayerUpdateBuilder] to build the player update with the new filters.
	 */
	fun setFilters(filters: Filters): AudioPlayerUpdateBuilder

	/**
	 * Sets the voice state for the audio player, typically related to the user’s voice channel state.
	 *
	 * @param voiceState The current voice state of the user (ex. whether they are connected to a voice channel).
	 * @return An [AudioPlayerUpdateBuilder] to build the player update with the new voice state.
	 */
	fun setVoiceState(voiceState: VoiceState): AudioPlayerUpdateBuilder
}
