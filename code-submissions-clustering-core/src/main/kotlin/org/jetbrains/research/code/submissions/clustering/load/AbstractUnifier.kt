package org.jetbrains.research.code.submissions.clustering.load

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.jetbrains.python.PythonFileType
import org.jetbrains.research.code.submissions.clustering.model.Submission
import org.jetbrains.research.code.submissions.clustering.util.addFileToProject
import org.jetbrains.research.code.submissions.clustering.util.getTmpProjectDir
import org.jetbrains.research.code.submissions.clustering.util.reformatInWriteAction
import org.jetbrains.research.ml.ast.transformations.Transformation
import java.util.logging.Logger

/**
 * Enum class for possible code submissions' languages.
 * @property extension corresponding file extension
 */
enum class Language(val extension: String) {
    PYTHON(PythonFileType.INSTANCE.defaultExtension),
    ;
}

/**
 * Abstract unifier producing unifying transformations over code submissions.
 * @property project project to use
 * @property psiManager PSI manager to use
 */
abstract class AbstractUnifier(
    private val project: Project,
    private val psiManager: PsiManager,
    private val anonymization: Transformation? = null
) {
    private val logger = Logger.getLogger(javaClass.name)
    abstract val language: Language
    abstract val transformations: List<Transformation>

    private fun String.createPsiFile(id: Int): PsiFile = ApplicationManager.getApplication().runWriteAction<PsiFile> {
        val basePath = getTmpProjectDir(toCreateFolder = false)
        val fileName = "dummy$id.${language.extension}"
        val file = addFileToProject(basePath, fileName, fileContent = this)
        val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file) ?: throw NoSuchFileException(
            file, reason = "Virtual file cannot be created because file was not found in the local file system"
        )
        psiManager.findFile(virtualFile)
    }

    @Suppress("TooGenericExceptionCaught")
    private fun PsiFile.applyTransformations(transformations: List<Transformation>, previousTree: PsiElement? = null) {
        logger.fine { "Tree Started: ${this.text}" }
        anonymization?.forwardApply(this)

        val psiDocumentManager = this.project.service<PsiDocumentManager>()
        val document = this.let { psiDocumentManager.getDocument(it) }
        try {
            transformations.forEach {
                document?.let {
                    psiDocumentManager.commitDocument(document)
                }
                logger.finer { "Transformation Started: ${it.key}" }
                it.forwardApply(this)
                logger.finer { "Transformation Ended: ${it.key}" }
            }
        } catch (e: Throwable) {
            logger.severe {
                """Transformation error {$e}: 
                        |Previous Code=${previousTree?.text}
                        |Current Code=${this.text}
                        |""".trimMargin()
            }
        }
    }

    fun Submission.unify(): Submission {
        val psi = this.code.createPsiFile(this.id)
        ApplicationManager.getApplication().invokeAndWait {
            ApplicationManager.getApplication().runWriteAction {
                var iterationNumber = 0
                do {
                    ++iterationNumber
                    val previousTree = psi.copy()
                    psi.applyTransformations(transformations, previousTree)
                    logger.finer { "Previous text[$iterationNumber]:\n${previousTree.text}\n" }
                    logger.finer { "Current text[$iterationNumber]:\n${psi.text}\n\n" }
                } while (!previousTree.textMatches(psi.text) || iterationNumber <= MAX_ITERATIONS)
                logger.fine { "Tree Ended[[$iterationNumber]]: ${psi.text}\n\n\n" }
            }
        }
        return this.copy(code = psi.reformatInWriteAction().text)
    }

    companion object {
        const val MAX_ITERATIONS = 100
    }
}

fun createTempProject(): Project = ProjectUtil.openOrImport(getTmpProjectDir(), null, true)
    ?: error("Internal error: the temp project was not created")
