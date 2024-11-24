/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Creates a logger instance for the specified class type.
 *
 * This inline function utilizes Kotlin's reified type parameters to allow for type-safe logging, creating a logger
 * that is associated with the class type provided as the generic type parameter.
 *
 * @param T The type of the class for which the logger is being created.
 * @return A [Logger] instance associated with the specified class type.
 * @author Miłosz Gilga
 */
inline fun <reified T : Any> logger(): Logger = LoggerFactory.getLogger(T::class.java)
