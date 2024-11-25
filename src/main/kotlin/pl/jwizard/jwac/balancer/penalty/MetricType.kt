/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.balancer.penalty

/**
 * Represents the types of metrics used for tracking and penalizing certain events in the audio balancer logic.
 *
 * @author Miłosz Gilga
 */
internal enum class MetricType {

	/**
	 * Metric type for tracking when a track gets stuck during playback.
	 */
	TRACK_STUCK,

	/**
	 * Metric type for tracking when an exception occurs while playing a track.
	 */
	TRACK_EXCEPTION,

	/**
	 * Metric type for tracking when loading a track fails completely.
	 */
	LOAD_FAILED,

	/**
	 * Metric type for tracking the number of attempts made to load a track.
	 */
	LOAD_ATTEMPT,
	;
}
