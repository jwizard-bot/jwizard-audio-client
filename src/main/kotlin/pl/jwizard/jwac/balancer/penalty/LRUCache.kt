/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.balancer.penalty

/**
 * The Least Recently Used (LRU) cache implementation using [LinkedHashMap]. Automatically removes the eldest entry
 * when the size exceeds the specified limit.
 *
 * @param K the type of keys maintained by this cache.
 * @param V the type of mapped values stored in this cache.
 * @property limit the maximum number of entries the cache can hold.
 * @author Miłosz Gilga
 */
internal class LRUCache<K, V>(private val limit: Int) : LinkedHashMap<K, V>(limit, 0.75f, true) {

	/**
	 * Determines whether the eldest entry in the cache should be removed. This is invoked automatically by the
	 * [LinkedHashMap] implementation when adding new entries.
	 *
	 * @param eldest the entry that would be removed if the cache size exceeds the limit.
	 * @return `true` if the eldest entry should be removed, `false` otherwise.
	 */
	override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?) = size > limit
}
