package com.banno.android.lintchecks

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.android.tools.lint.detector.api.TextFormat
import com.intellij.psi.PsiElement
import org.jetbrains.uast.UClass

class EpoxyModelDetector : Detector(), SourceCodeScanner {

    override fun applicableSuperClasses() = listOf("com.airbnb.epoxy.EpoxyModel")

    override fun visitClass(context: JavaContext, declaration: UClass) {
        declaration.modifierList?.text?.let {
            if (!(it.contains("abstract") || it.contains("data"))) {
                val modifiersLength = if (it.isNotEmpty()) it.length + 7 else 6
                context.report(
                    issue = DATA_CLASS_ISSUE,
                    location = context.getRangeLocation(
                        from = declaration.nameIdentifier as PsiElement,
                        fromDelta = -modifiersLength,
                        length = modifiersLength + (declaration.name?.length ?: 0)
                    ),
                    message = DATA_CLASS_ISSUE.getBriefDescription(TextFormat.TEXT),
                    quickfixData = LintFix.create()
                        .replace().text("class ").with("data class ")
                        .name("add 'data' modifier")
                        .build()
                )
            }
        }
    }

    companion object {
        val DATA_CLASS_ISSUE = Issue.create(
            id = "EpoxyModelDataClass",
            briefDescription = "EpoxyModels must be data classes",
            explanation = "Come on, nerd! These need to be data classes. Don't you even know how to program?",
            category = Category.CORRECTNESS,
            priority = 8,
            severity = Severity.ERROR,
            implementation = Implementation(
                EpoxyModelDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }
}
