/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.node

/**
 * Represents a pool of nodes in a distributed system.
 *
 * @author Miłosz Gilga
 */
interface NodePool {

	/**
	 * The name of the node pool. Must be unique (for load balancer).
	 */
	val poolName: String
}
