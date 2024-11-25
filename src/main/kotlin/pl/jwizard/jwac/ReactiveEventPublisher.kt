/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac

import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks

/**
 * A class that facilitates reactive event publishing. It allows publishing events to multiple subscribers in a
 * backpressure-aware manner.
 *
 * @param T The type of events that will be published.
 * @author Miłosz Gilga
 */
class ReactiveEventPublisher<T> {

	/**
	 * A multicast sink that can emit events to multiple subscribers. The sink is backpressure-aware and buffers events
	 * when necessary.
	 */
	private val sink: Sinks.Many<T> = Sinks.many().multicast().onBackpressureBuffer()

	/**
	 * The Flux representing the stream of events. Subscribers can listen to this stream.
	 */
	private val flux: Flux<T> = sink.asFlux()

	/**
	 * A reference to the Flux subscription. Keeps the event stream alive for as long as needed.
	 */
	private val reference = flux.subscribe()

	/**
	 * Publishes an event with the possibility of throwing exceptions. This method emits the event to all subscribers.
	 * If there is an error during emission, it will be handled by the error handler.
	 *
	 * @param value The event of type T to be published to subscribers.
	 */
	fun publishWithException(value: T & Any) {
		try {
			sink.tryEmitNext(value)
		} catch (ex: Exception) {
			sink.emitError(ex, Sinks.EmitFailureHandler.FAIL_FAST)
		}
	}

	/**
	 * Filters the Flux stream to only emit events of a specific type. This method creates a new Flux stream that will
	 * emit only items of type V.
	 *
	 * @param V The type of events to be filtered from the original Flux stream.
	 * @return A new Flux of type V that emits only events of that type.
	 */
	internal inline fun <reified V : Any> ofType(): Flux<V> = flux.ofType(V::class.java)

	/**
	 * Disposes of the subscription to the event stream, stopping further emissions.
	 * This will cancel the subscription and prevent any further events from being received.
	 */
	fun dispose() = reference.dispose()
}
