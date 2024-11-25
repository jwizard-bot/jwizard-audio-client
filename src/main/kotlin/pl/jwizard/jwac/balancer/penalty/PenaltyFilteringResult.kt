/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.balancer.penalty

/**
 * Represents the filtering results for penalty calculations, with each result associated with a specific penalty value.
 *
 * @property penalty The numeric penalty value associated with the filtering result.
 * @author Miłosz Gilga
 */
internal enum class PenaltyFilteringResult(val penalty: Int) {

	/**
	 * Represents a passing result with no penalty applied.
	 */
	PASS(0),

	/**
	 * Represents a soft block result with a moderate penalty applied.
	 */
	SOFT_BLOCK(1000),

	/**
	 * Represents a full block result with a very high penalty applied.
	 */
	BLOCK(10000000),
	;
}
