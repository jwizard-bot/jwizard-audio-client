/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.player

import dev.arbjerg.lavalink.protocol.v4.*
import pl.jwizard.jwac.node.AudioNode
import pl.jwizard.jwac.player.track.Track
import pl.jwizard.jwac.util.toJsonObject
import reactor.core.CoreSubscriber
import reactor.core.publisher.Mono

/**
 * A builder for constructing and sending updates to the audio player in a specific guild.
 *
 * @property guildId The ID of the guild for which the audio player update is intended.
 * @property audioNode The audio node responsible for processing and sending the update.
 * @author Miłosz Gilga
 */
class AudioPlayerUpdateBuilder(
	private val guildId: Long,
	private val audioNode: AudioNode,
) : Mono<AudioPlayer>(), UpdatableAudioPlayer {

	private var trackUpdate: Omissible<PlayerUpdateTrack> = Omissible.omitted()
	private var position: Omissible<Long> = Omissible.omitted()
	private var endTime: Omissible<Long?> = Omissible.omitted()
	private var volume: Omissible<Int> = Omissible.omitted()
	private var paused: Omissible<Boolean> = Omissible.omitted()
	private var filters: Omissible<Filters> = Omissible.omitted()
	private var voiceState: Omissible<VoiceState> = Omissible.omitted()
	private var noReplace = false

	/**
	 * Sets the track to be played by the audio player.
	 *
	 * @param track The track to set, or `null` to stop playback.
	 * @return The updated builder instance, allowing method chaining.
	 */
	override fun setTrack(track: Track?) = apply {
		trackUpdate = PlayerUpdateTrack(
			encoded = Omissible.of(track?.encoded),
			userData = toJsonObject(track?.userData).toOmissible(),
		).toOmissible()
	}

	/**
	 * Updates the current track with the specified [PlayerUpdateTrack].
	 *
	 * @param track The update containing information to modify the current track.
	 * @return The updated builder instance, allowing method chaining.
	 */
	override fun updateTrack(track: PlayerUpdateTrack) = apply { track.toOmissible() }

	/**
	 * Stops the current track from playing.
	 *
	 * @return The updated builder instance, allowing method chaining.
	 */
	override fun stopTrack() = apply {
		trackUpdate = PlayerUpdateTrack(encoded = Omissible.of(null)).toOmissible()
	}

	/**
	 * Sets the playback position for the current track.
	 *
	 * @param position The position to set, in milliseconds, or `null` to leave it unchanged.
	 * @return The updated builder instance, allowing method chaining.
	 */
	override fun setPosition(position: Long?) = apply { this.position = position.toOmissible() }

	/**
	 * Sets the end time for the current track.
	 *
	 * @param endTime The end time to set, in milliseconds, or `null` to leave it unchanged.
	 * @return The updated builder instance, allowing method chaining.
	 */
	override fun setEndTime(endTime: Long?) = apply { this.endTime = endTime.toOmissible() }

	/**
	 * Omits the end time for the current track, making it indefinite.
	 *
	 * @return The updated builder instance, allowing method chaining.
	 */
	override fun omitEndTime() = apply { this.endTime = Omissible.omitted() }

	/**
	 * Sets the volume for the player.
	 *
	 * @param volume The volume level to set, between 0 and 1000.
	 * @return The updated builder instance, allowing method chaining.
	 */
	override fun setVolume(volume: Int) = apply { this.volume = volume.toOmissible() }

	/**
	 * Pauses or unpauses the player.
	 *
	 * @param paused Whether to pause the player (`true`) or not paused it (`false`).
	 * @return The updated builder instance, allowing method chaining.
	 */
	override fun setPaused(paused: Boolean) = apply { this.paused = paused.toOmissible() }

	/**
	 * Sets the audio filters for the player.
	 *
	 * @param filters The filters to apply to the player.
	 * @return The updated builder instance, allowing method chaining.
	 */
	override fun setFilters(filters: Filters) = apply { this.filters = filters.toOmissible() }

	/**
	 * Sets the voice state for the player, which determines how the bot interacts with the voice channel.
	 *
	 * @param voiceState The `VoiceState` to apply.
	 * @return The updated builder instance, allowing method chaining.
	 */
	override fun setVoiceState(voiceState: VoiceState) = apply { this.voiceState = voiceState.toOmissible() }

	/**
	 * Specifies whether the update should replace the existing player state.
	 *
	 * @param noReplace Whether to prevent replacing the current player state (`true` to not replace).
	 * @return The updated builder instance, allowing method chaining.
	 */
	fun setNoReplace(noReplace: Boolean) = apply { this.noReplace = noReplace }

	/**
	 * Applies the settings from another [AudioPlayerUpdateBuilder] to this builder.
	 *
	 * @param builder The builder whose settings will be copied to this builder.
	 * @return The updated builder instance, allowing method chaining.
	 */
	fun applyBuilder(builder: AudioPlayerUpdateBuilder) = apply {
		this.trackUpdate = builder.trackUpdate
		this.position = builder.position
		this.endTime = builder.endTime
		this.volume = builder.volume
		this.paused = builder.paused
		this.filters = builder.filters
		this.voiceState = builder.voiceState
		this.noReplace = builder.noReplace
	}

	/**
	 * Constructs a [PlayerUpdate] object from the current settings in the builder.
	 *
	 * @return A [PlayerUpdate] representing the accumulated changes to be applied to the player.
	 */
	fun build() = PlayerUpdate(
		track = trackUpdate,
		position = position,
		endTime = endTime,
		volume = volume,
		paused = paused,
		filters = filters,
		voice = voiceState,
	)

	/**
	 * Subscribes to the update and sends it to the audio node for processing.
	 *
	 * @param observer The subscriber to handle the update response.
	 */
	override fun subscribe(observer: CoreSubscriber<in AudioPlayer>) {
		audioNode.updatePlayer(guildId, build(), noReplace).subscribe(observer)
	}
}
