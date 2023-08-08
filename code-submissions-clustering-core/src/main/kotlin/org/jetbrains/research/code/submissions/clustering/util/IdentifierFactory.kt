package org.jetbrains.research.code.submissions.clustering.util

import java.util.concurrent.atomic.AtomicInteger

typealias Identifier = Int

/**
 * Factory to create an identifier unique to factory instance
 */
interface IdentifierFactory {
    /**
     * Creates unique solution space vertex identifier
     *
     * @return unique identifier
     */
    fun uniqueIdentifier(): Identifier
}

class IdentifierFactoryImpl(private var counter: AtomicInteger = AtomicInteger(1)) : IdentifierFactory {
    constructor(intCounter: Int) : this(AtomicInteger(intCounter))
    override fun uniqueIdentifier(): Identifier = counter.getAndIncrement()
}
