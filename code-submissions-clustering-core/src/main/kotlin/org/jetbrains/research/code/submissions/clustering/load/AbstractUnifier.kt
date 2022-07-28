package org.jetbrains.research.code.submissions.clustering.load

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.jetbrains.python.PythonFileType
import org.jetbrains.research.code.submissions.clustering.model.Submission
import org.jetbrains.research.code.submissions.clustering.util.addFileToProject
import org.jetbrains.research.code.submissions.clustering.util.getTmpProjectDir
import org.jetbrains.research.ml.ast.transformations.Transformation

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
abstract class AbstractUnifier(val project: Project, val psiManager: PsiManager) {
    abstract val language: Language
    abstract val transformations: List<Transformation>

    private fun String.createPsiFile(id: Int): PsiFile = ApplicationManager.getApplication().runWriteAction<PsiFile> {
        val basePath = getTmpProjectDir(toCreateFolder = false)
        val fileName = "dummy$id.${language.extension}"
        val file = addFileToProject(basePath, fileName, fileContext = this)
        val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file) ?: throw NoSuchFileException(
            file, reason = "Virtual file cannot be created because file was not found in the local file system"
        )
        psiManager.findFile(virtualFile)
    }

    private fun PsiFile.applyTransformations(transformations: List<Transformation>) {
        ApplicationManager.getApplication().invokeAndWait {
            ApplicationManager.getApplication().runWriteAction {
                transformations.forEach {
                    it.forwardApply(this)
                }
            }
        }
    }

    private fun PsiFile.hasChangedAfterTransformations(transformations: List<Transformation>): Boolean {
        val prevPsiText = this.text
        this.applyTransformations(transformations)
        return prevPsiText != this.text
    }

    fun Submission.unify(): Submission {
        val psi = this.code.createPsiFile(this.id)
        var wasChanged: Boolean
        var iterationsCounter = 0
        do {
            wasChanged = psi.hasChangedAfterTransformations(transformations)
            iterationsCounter++
        } while (wasChanged || iterationsCounter <= MAX_ITERATIONS)
        return this.copy(code = psi.text)
    }

    companion object {
        const val MAX_ITERATIONS = 5
    }
}

fun createTempProject(): Project = ProjectUtil.openOrImport(getTmpProjectDir(), null, true)
    ?: error("Internal error: the temp project was not created")
