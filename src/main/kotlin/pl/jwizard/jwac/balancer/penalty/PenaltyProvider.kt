/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.balancer.penalty

import pl.jwizard.jwac.balancer.region.VoiceRegion
import pl.jwizard.jwac.node.AudioNode

/**
 * Interface representing a provider of penalties. This interface is used to calculate and provide penalty values based
 * on the given audio node and voice region.
 *
 * @author Miłosz Gilga
 */
interface PenaltyProvider {

	/**
	 * Retrieves the penalty associated with a specific audio node and voice region. This penalty value may be used for
	 * balancing purposes in audio node selection.
	 *
	 * @param audioNode The audio node for which the penalty is to be calculated.
	 * @param region The voice region in which the audio node resides, or null if no specific region is considered.
	 * @return The calculated penalty for the given audio node and region.
	 */
	fun getPenalty(audioNode: AudioNode, region: VoiceRegion?): Int
}
