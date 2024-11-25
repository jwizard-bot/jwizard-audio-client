/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.ws

/**
 * Enum representing WebSocket closure codes.
 *
 * @property code The integer value of the WebSocket closure code.
 * @author Miłosz Gilga
 */
internal enum class WsCode(val code: Int) {
	NORMAL(1000),
	;

	/**
	 * Checks if the given WebSocket closure code is equal to this enum value's code.
	 *
	 * @param deliveredCode The WebSocket closure code that was received.
	 * @return True if the delivered code matches this enum's code, otherwise false.
	 */
	fun isEqual(deliveredCode: Int) = code == deliveredCode
}
