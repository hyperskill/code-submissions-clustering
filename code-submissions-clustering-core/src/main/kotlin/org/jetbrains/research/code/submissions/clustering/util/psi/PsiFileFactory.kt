package org.jetbrains.research.code.submissions.clustering.util.psi

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import org.jetbrains.research.code.submissions.clustering.load.context.builder.IdentifierFactoryImpl
import org.jetbrains.research.code.submissions.clustering.model.Language
import org.jetbrains.research.code.submissions.clustering.util.addFileToProject
import org.jetbrains.research.code.submissions.clustering.util.getTmpProjectDir
import java.io.File

class PsiFileFactory(
    private val language: Language,
    private val psiManager: PsiManager,
) {
    private val idFactory = IdentifierFactoryImpl()
    private val basePath = getTmpProjectDir(toCreateFolder = false)
    private val files = mutableListOf<File>()
    private val availablePsiFiles = mutableSetOf<PsiFile>()

    fun clearFactory() = availablePsiFiles.clear()

    fun getPsiFile(text: String): PsiFile = (availablePsiFiles.firstOrNull()?.also { psiFile ->
        availablePsiFiles.remove(psiFile)
    } ?: run {
        val fileName = "dummy${idFactory.uniqueIdentifier()}.${language.extension}"
        val file = addFileToProject(basePath, fileName).also { files.add(it) }
        val psiFile = ApplicationManager.getApplication().runWriteAction<PsiFile> {
            val virtualFile =
                LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file) ?: throw NoSuchFileException(
                    file, reason = "Virtual file cannot be created because file was not found in the local file system"
                )
            psiManager.findFile(virtualFile)
        }
        psiFile
    }).also { it.setText(text) }

    private fun PsiFile.setText(text: String) {
        val document = viewProvider.document
            ?: error("No document for file found")
        val psiDocumentManager = PsiDocumentManager.getInstance(project)

        psiDocumentManager.doPostponedOperationsAndUnblockDocument(document)
        WriteAction.run<Throwable> {
            document.setText(text)
        }
        psiDocumentManager.commitDocument(document)
    }

    fun releasePsiFile(psiFile: PsiFile) = availablePsiFiles.add(psiFile)
}
