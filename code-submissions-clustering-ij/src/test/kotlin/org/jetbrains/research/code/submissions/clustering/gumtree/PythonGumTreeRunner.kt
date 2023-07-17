package org.jetbrains.research.code.submissions.clustering.gumtree

import org.jetbrains.research.code.submissions.clustering.impl.context.gumtree.GumTreeParserUtil
import org.junit.Test
import org.junit.runner.Description
import org.junit.runner.Runner
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunNotifier
import java.io.File


class PythonGumTreeRunner(private val testClass: Class<*>) : Runner() {
    override fun getDescription(): Description = Description
        .createTestDescription(testClass, "Python GumTree Runner")

    override fun run(notifier: RunNotifier) {
        val testObject = testClass.constructors.single().newInstance()
        for (method in testClass.methods) {
            if (method.isAnnotationPresent(Test::class.java)) {
                val description = Description
                    .createTestDescription(testClass, method.name)

                notifier.fireTestStarted(description)
                try {
                    setUp()
                    method.invoke(testObject)
                    tearDown()
                    notifier.fireTestFinished(description)
                } catch (e: Throwable) {
                    notifier.fireTestFailure(Failure(description, e.cause))
                }
            }
        }
    }

    private fun setUp() {
        GumTreeParserUtil.checkSetup()
    }

    private fun tearDown() {
        listOf(GumTreeParserUtil.parserZipPath, GumTreeParserUtil.targetParserPath, GumTreeParserUtil.parserRepoPath).forEach { path ->
            File(path).deleteRecursively()
        }
    }
}
