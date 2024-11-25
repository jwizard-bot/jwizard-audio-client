/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.balancer.region

/**
 * Enum representing different regions or geographical groups used for audio node balancing. Each region is associated
 * with a specific geographical area.
 *
 * @author Miłosz Gilga
 */
enum class RegionGroup {
	ASIA,
	EUROPE,
	US,
	SOUTH_AMERICA,
	AFRICA,
	MIDDLE_EAST,
	UNKNOWN,
	;

	companion object {
		/**
		 * Converts a raw string value representing a region into a corresponding [RegionGroup] enum value. If the string
		 * does not match any known region, the [UNKNOWN] value is returned.
		 *
		 * @param region A string representing a region, typically in uppercase.
		 * @return The corresponding [RegionGroup] enum value or [UNKNOWN] if the string does not match.
		 */
		fun fromRawValue(region: String) = when (region.uppercase()) {
			"AFRICA" -> AFRICA
			"ASIA" -> ASIA
			"EUROPE" -> EUROPE
			"MIDDLE_EAST" -> MIDDLE_EAST
			"SOUTH_AMERICA" -> SOUTH_AMERICA
			"US" -> US
			else -> UNKNOWN
		}
	}
}
