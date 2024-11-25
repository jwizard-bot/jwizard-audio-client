/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.player

import dev.arbjerg.lavalink.protocol.v4.*
import pl.jwizard.jwac.balancer.region.VoiceRegion
import pl.jwizard.jwac.node.AudioNode
import pl.jwizard.jwac.player.track.Track
import kotlin.math.min

/**
 * Represents an audio player that controls the playback of a track in a specific guild.
 *
 * @property audioNode The audio node that this player is associated with.
 * @property player The underlying player object, which contains the actual playback state.
 * @author Miłosz Gilga
 */
class AudioPlayer(
	private val audioNode: AudioNode,
	private val player: Player,
) : UpdatableAudioPlayer {

	val guildId = player.guildId.toLong()
	val volume = player.volume
	val voiceState = player.voice
	val filters = player.filters
	val paused = player.paused

	/**
	 * The current state of the player.
	 */
	var state = player.state
		private set

	/**
	 * The current track being played by the player, if any.
	 */
	var track = player.track?.let { Track(it) }
		private set

	/**
	 * The current position of the track being played, in milliseconds. If the player is paused, the position will
	 * reflect the time when it was paused.
	 */
	val position
		get() = when {
			player.track == null -> 0
			player.paused -> state.position
			else -> min(state.position + (System.currentTimeMillis() - state.time), player.track?.info!!.length)
		}

	/**
	 * The voice region of the player, derived from the endpoint of the voice state.
	 */
	val voiceRegion
		get() = if (voiceState.endpoint.isNotBlank()) {
			VoiceRegion.fromEndpoint(voiceState.endpoint)
		} else {
			null
		}

	/**
	 * Updates the state of the player.
	 *
	 * @param state The new state to be applied to the player.
	 */
	internal fun updateState(state: PlayerState) {
		this.state = state
	}

	/**
	 * Updates the track that the player is currently playing.
	 *
	 * @param track The new track to be played, or `null` to stop playback.
	 */
	internal fun updateTrack(track: Track?) {
		this.track = track
	}

	/**
	 * Sets a new track for the player.
	 *
	 * @param track The new track to be set, or `null` to stop playback.
	 * @return An [AudioPlayerUpdateBuilder] to apply the change.
	 */
	override fun setTrack(track: Track?) = AudioPlayerUpdateBuilder(guildId, audioNode)
		.setTrack(track)

	/**
	 * Updates the current track of the player with the provided [PlayerUpdateTrack].
	 *
	 * @param track The [PlayerUpdateTrack] containing information to update the current track.
	 * @return An [AudioPlayerUpdateBuilder] to apply the update.
	 */
	override fun updateTrack(track: PlayerUpdateTrack) = AudioPlayerUpdateBuilder(guildId, audioNode)
		.updateTrack(track)

	/**
	 * Stops the current track from playing.
	 *
	 * @return An [AudioPlayerUpdateBuilder] to apply the stop action.
	 */
	override fun stopTrack() = AudioPlayerUpdateBuilder(guildId, audioNode)
		.stopTrack()

	/**
	 * Sets the position of the current track.
	 *
	 * @param position The position (in milliseconds) to set the track to, or `null` to leave it unchanged.
	 * @return An [AudioPlayerUpdateBuilder] to apply the change.
	 */
	override fun setPosition(position: Long?) = AudioPlayerUpdateBuilder(guildId, audioNode)
		.setPosition(position)

	/**
	 * Sets the end time of the current track.
	 *
	 * @param endTime The end time (in milliseconds) to set the track to, or `null` to omit it.
	 * @return An [AudioPlayerUpdateBuilder] to apply the change.
	 */
	override fun setEndTime(endTime: Long?) = AudioPlayerUpdateBuilder(guildId, audioNode)
		.setEndTime(endTime)

	/**
	 * Omits the end time for the current track.
	 *
	 * @return An [AudioPlayerUpdateBuilder] to apply the change.
	 */
	override fun omitEndTime() = AudioPlayerUpdateBuilder(guildId, audioNode)
		.omitEndTime()

	/**
	 * Sets the volume for the current player.
	 *
	 * @param volume The volume level to set, between 0 and 1000.
	 * @return An [AudioPlayerUpdateBuilder] to apply the change.
	 */
	override fun setVolume(volume: Int) = AudioPlayerUpdateBuilder(guildId, audioNode)
		.setVolume(volume)

	/**
	 * Pauses or unpauses the current track.
	 *
	 * @param paused Whether the player should be paused (`true`) or not paused (`false`).
	 * @return An [AudioPlayerUpdateBuilder] to apply the change.
	 */
	override fun setPaused(paused: Boolean) = AudioPlayerUpdateBuilder(guildId, audioNode)
		.setPaused(paused)

	/**
	 * Sets the filters to apply to the current track.
	 *
	 * @param filters The [Filters] to apply.
	 * @return An [AudioPlayerUpdateBuilder] to apply the change.
	 */
	override fun setFilters(filters: Filters) = AudioPlayerUpdateBuilder(guildId, audioNode)
		.setFilters(filters)

	/**
	 * Sets the voice state for the current player.
	 *
	 * @param voiceState The [VoiceState] to apply.
	 * @return An [AudioPlayerUpdateBuilder] to apply the change.
	 */
	override fun setVoiceState(voiceState: VoiceState) = AudioPlayerUpdateBuilder(guildId, audioNode)
		.setVoiceState(voiceState)

	/**
	 * Converts the current state of the player to an update builder.
	 *
	 * @return An [AudioPlayerUpdateBuilder] initialized with the current player's state.
	 */
	fun stateToBuilder() = AudioPlayerUpdateBuilder(guildId, audioNode)
		.apply { player.track?.let { setTrack(Track(it)) } }
		.setPosition(position)
		.setEndTime(null)
		.setVolume(player.volume)
		.setPaused(player.paused)
		.setFilters(player.filters)
		.setVoiceState(player.voice)
}
