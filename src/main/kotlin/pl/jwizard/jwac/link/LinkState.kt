/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.link

/**
 * Enum class representing the possible states of a link between a guild and an audio node.
 *
 * @author Miłosz Gilga
 */
internal enum class LinkState {

	/**
	 * The link is in the process of being established.
	 */
	CONNECTING,

	/**
	 * The link is successfully connected to an audio node.
	 */
	CONNECTED,

	/**
	 * The link is disconnected from the audio node.
	 */
	DISCONNECTED,
	;
}
