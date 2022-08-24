package org.jetbrains.research.code.submissions.clustering.gumtree

import org.jetbrains.research.code.submissions.clustering.load.context.builder.gumtree.GumTreeParserUtil
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import java.util.logging.Logger

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class PythonGumTreeBaseTest {
    protected val logger = Logger.getLogger(javaClass.name)

    @BeforeAll
    internal fun beforeAll() {
        logger.info("beforeAll called")
        GumTreeParserUtil.checkSetup()
    }
}
