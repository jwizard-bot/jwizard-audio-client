/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.event.onload

/**
 * Event triggered when a track loading operation fails to find any matching tracks.
 *
 * This class represents a specific type of loading result where the provided search criteria or identifiers do not
 * match any tracks in the source. It implements the [KLoadResult] interface to integrate with the unified result
 * handling system.
 *
 * @author Miłosz Gilga
 */
class KNoMatchesEvent : KLoadResult
