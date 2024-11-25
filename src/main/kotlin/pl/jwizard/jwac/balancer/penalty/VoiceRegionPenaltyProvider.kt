/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.balancer.penalty

import pl.jwizard.jwac.balancer.region.RegionGroup
import pl.jwizard.jwac.balancer.region.VoiceRegion
import pl.jwizard.jwac.node.AudioNode

/**
 * A class that implements the [PenaltyProvider] interface to calculate penalties based on voice region groups. It
 * applies a penalty if the [AudioNode]'s region group does not match the given voice region.
 *
 * @author Miłosz Gilga
 */
internal class VoiceRegionPenaltyProvider : PenaltyProvider {

	/**
	 * Calculates and returns the penalty for the given audio node and region. If the region group of the audio node
	 * matches the provided region group, no penalty is applied. Otherwise, a soft block penalty is applied. If the
	 * region is unknown or not provided, no penalty is applied.
	 *
	 * @param audioNode The audio node for which the penalty is to be calculated.
	 * @param region The voice region to be compared with the audio node's region.
	 * @return The penalty for the given audio node and region.
	 */
	override fun getPenalty(audioNode: AudioNode, region: VoiceRegion?): Int {
		val filter = audioNode.config.regionGroup
		if (region == null || region == VoiceRegion.UNKNOWN || filter == RegionGroup.UNKNOWN) {
			return 0
		}
		val penaltyResult = if (region.group == filter) {
			PenaltyFilteringResult.PASS
		} else {
			PenaltyFilteringResult.SOFT_BLOCK
		}
		return penaltyResult.penalty
	}
}
