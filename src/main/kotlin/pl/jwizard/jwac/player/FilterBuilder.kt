/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.player

import dev.arbjerg.lavalink.protocol.v4.*
import kotlinx.serialization.json.JsonElement

/**
 * A builder for constructing audio filter settings, which can be applied to the audio player.
 *
 * @author Miłosz Gilga
 */
class FilterBuilder {
	private var volume: Omissible<Float> = Omissible.Omitted()
	private var equalizer: Omissible<List<Band>> = Omissible.Omitted()
	private var karaoke: Omissible<Karaoke?> = Omissible.Omitted()
	private var timescale: Omissible<Timescale?> = Omissible.Omitted()
	private var tremolo: Omissible<Tremolo?> = Omissible.Omitted()
	private var vibrato: Omissible<Vibrato?> = Omissible.Omitted()
	private var distortion: Omissible<Distortion?> = Omissible.Omitted()
	private var rotation: Omissible<Rotation?> = Omissible.Omitted()
	private var channelMix: Omissible<ChannelMix?> = Omissible.Omitted()
	private var lowPass: Omissible<LowPass?> = Omissible.Omitted()
	private var pluginFilters: MutableMap<String, JsonElement> = mutableMapOf()

	/**
	 * Sets the volume for the audio player.
	 *
	 * @param volume The volume level to set, where 1.0 is the default.
	 * @return The updated builder instance, allowing method chaining.
	 */
	fun setVolume(volume: Float) = apply { this.volume = volume.toOmissible() }

	/**
	 * Sets the equalizer bands for the audio player.
	 *
	 * @param equalizer A list of equalizer bands to be applied.
	 * @return The updated builder instance, allowing method chaining.
	 */
	fun setEqualizer(equalizer: List<Band>) = apply { this.equalizer = equalizer.toOmissible() }

	/**
	 * Sets the gain for a specific equalizer band.
	 *
	 * @param band The equalizer band (index 0-14).
	 * @param gain The gain for the band, where 1.0 is the default.
	 * @return The updated builder instance, allowing method chaining.
	 */
	fun setEqualizer(band: Int, gain: Float = 1.0F) = apply {
		val bandInstance = Band(band, gain)
		val currentEqualizer = this.equalizer

		this.equalizer = if (currentEqualizer.isPresent()) {
			val mutableEqualizer = currentEqualizer.value.toMutableList()
			val bandIndex = mutableEqualizer.indexOfFirst { it.band == band }
			if (bandIndex > -1) {
				mutableEqualizer[bandIndex] = bandInstance
			} else {
				mutableEqualizer.add(bandInstance)
			}
			mutableEqualizer.toOmissible()
		} else {
			listOf(bandInstance).toOmissible()
		}
	}

	/**
	 * Sets the karaoke filter for the audio player.
	 *
	 * @param karaoke The karaoke filter settings to apply.
	 * @return The updated builder instance, allowing method chaining.
	 */
	fun setKaraoke(karaoke: Karaoke?) = apply { this.karaoke = karaoke.toOmissible() }

	/**
	 * Sets the timescale filter for adjusting playback speed and pitch.
	 *
	 * @param timescale The timescale filter settings to apply.
	 * @return The updated builder instance, allowing method chaining.
	 */
	fun setTimescale(timescale: Timescale?) = apply { this.timescale = Omissible.of(timescale) }

	/**
	 * Sets the tremolo effect for the audio player.
	 *
	 * @param tremolo The tremolo effect settings to apply.
	 * @return The updated builder instance, allowing method chaining.
	 */
	fun setTremolo(tremolo: Tremolo?) = apply { this.tremolo = Omissible.of(tremolo) }

	/**
	 * Sets the vibrato effect for the audio player.
	 *
	 * @param vibrato The vibrato effect settings to apply.
	 * @return The updated builder instance, allowing method chaining.
	 */
	fun setVibrato(vibrato: Vibrato?) = apply { this.vibrato = Omissible.of(vibrato) }

	/**
	 * Sets the distortion effect for the audio player.
	 *
	 * @param distortion The distortion effect settings to apply.
	 * @return The updated builder instance, allowing method chaining.
	 */
	fun setDistortion(distortion: Distortion?) = apply { this.distortion = Omissible.of(distortion) }

	/**
	 * Sets the rotation effect for the audio player.
	 *
	 * @param rotation The rotation effect settings to apply.
	 * @return The updated builder instance, allowing method chaining.
	 */
	fun setRotation(rotation: Rotation?) = apply { this.rotation = Omissible.of(rotation) }

	/**
	 * Sets the channel mix filter for the audio player.
	 *
	 * @param channelMix The channel mix settings to apply.
	 * @return The updated builder instance, allowing method chaining.
	 */
	fun setChannelMix(channelMix: ChannelMix?) = apply { this.channelMix = Omissible.of(channelMix) }

	/**
	 * Sets the low pass filter for the audio player.
	 *
	 * @param lowPass The low pass filter settings to apply.
	 * @return The updated builder instance, allowing method chaining.
	 */
	fun setLowPass(lowPass: LowPass?) = apply { this.lowPass = Omissible.of(lowPass) }

	/**
	 * Sets a custom plugin filter for the audio player.
	 *
	 * @param name The name of the plugin filter.
	 * @param filter The filter settings in JSON format.
	 * @return The updated builder instance, allowing method chaining.
	 */
	fun setPluginFilter(name: String, filter: JsonElement) = apply { pluginFilters[name] = filter }

	/**
	 * Builds and returns a [Filters] object representing the accumulated audio filter settings.
	 *
	 * @return A [Filters] object containing all the set filter properties.
	 */
	fun build() = Filters(
		volume,
		equalizer,
		karaoke,
		timescale,
		tremolo,
		vibrato,
		distortion,
		rotation,
		channelMix,
		lowPass,
		pluginFilters,
	)
}
