package org.jetbrains.research.code.submissions.clustering.load.context.builder

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

class IdentifierFactoryImpl(private var counter: Int = 1) : IdentifierFactory {
    override fun uniqueIdentifier(): Identifier = counter++
}
