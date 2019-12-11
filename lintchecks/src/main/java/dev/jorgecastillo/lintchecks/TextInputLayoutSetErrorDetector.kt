package dev.jorgecastillo.lintchecks

import com.android.SdkConstants.CLASS_TEXT_INPUT_LAYOUT
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Detector.UastScanner
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMember
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.UReferenceExpression
import org.jetbrains.uast.kotlin.KotlinUBinaryExpression
import org.jetbrains.uast.kotlin.KotlinUQualifiedReferenceExpression

/**
 * Lint check that looks for usages of textInput.error = "something" or "textInput.setError("something") given those
 * cause styling problems due to an animation cancelled. The problem is described here:
 *
 * https://banno-jha.atlassian.net/browse/AN-646
 *
 * We want to enforce callers to use the TextInputLayout.setErrorIfChanged() instead that ensures nothing gets broken
 * visually.
 */
class TextInputLayoutSetErrorDetector : Detector(), UastScanner {

    override fun getApplicableMethodNames(): List<String>? {
        return listOf(METHOD_NAME)
    }

    override fun getApplicableReferenceNames(): List<String>? {
        return listOf(KOTLIN_ERROR_PROPERTY_REFERENCE)
    }

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        if (isTextInputLayoutCall(context, method)) {
            val name = method.name
            val replace = "setErrorIfChanged()"

            context.report(name, replace, "function", node)
        }
    }

    override fun visitReference(context: JavaContext, reference: UReferenceExpression, referenced: PsiElement) {
        if (isTextInputLayoutProperty(context, referenced) && isBeingAssigned(reference)) {
            val name = reference.asSourceString()
            val replace = "setErrorIfChanged()"

            context.report(name, replace, "property assignment", reference)
        }
    }

    /**
     * @return Whether the method belongs to TextInputLayout. Note that newName() returns the new qualified name for it,
     * which is the material components one. oldName() and defaultName() return the old support design one.
     */
    private fun isTextInputLayoutCall(
        context: JavaContext,
        method: PsiMethod
    ): Boolean {
        val evaluator = context.evaluator
        return evaluator.isMemberInSubClassOf(method, CLASS_TEXT_INPUT_LAYOUT.newName(), false)
    }

    /**
     * @return Whether the property is being assigned or not, to avoid reporting for simple read accesses.
     */
    private fun isBeingAssigned(reference: UReferenceExpression): Boolean = reference.uastParent.let { parent ->
        parent is KotlinUQualifiedReferenceExpression && parent.uastParent is KotlinUBinaryExpression
    }

    /**
     * @return Whether the property belongs to TextInputLayout. Note that newName() returns the new qualified name for
     * it, which is the material components one. oldName() and defaultName() return the old support design one.
     */
    private fun isTextInputLayoutProperty(
        context: JavaContext,
        element: PsiElement
    ): Boolean {
        val evaluator = context.evaluator
        return element is PsiMember && evaluator.isMemberInSubClassOf(element, CLASS_TEXT_INPUT_LAYOUT.newName(), false)
    }

    private fun JavaContext.report(name: String, replace: String, whatToReplace: String, expression: UExpression) {
        val message = String.format("Should use `%1\$s` instead of `%2\$s` $whatToReplace.", replace, name)
        val fix = fix().name("Replace with $replace()")
            .replace()
            .text(name)
            .with(replace)
            .build()

        report(ISSUE, expression, getLocation(expression), message, fix)
    }

    companion object {
        const val METHOD_NAME = "setError"
        const val KOTLIN_ERROR_PROPERTY_REFERENCE = "error"

        val ISSUE = Issue.create(
            id = "TextInputLayoutProblematicSetErrorCall",
            briefDescription = "Calling TextInputLayout.setError() is problematic",
            explanation = "This variant was causing problems to error hint rendering in TextInputLayout. Problem is " +
                    "described here https://banno-jha.atlassian.net/browse/AN-646. Please, use the " +
                    "TextInputLayout.setErrorIfChanged extension function instead to ensure it looks fine.",
            category = Category.CORRECTNESS,
            priority = 8,
            severity = Severity.ERROR,
            implementation = Implementation(
                TextInputLayoutSetErrorDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        ).setAndroidSpecific(true)
    }
}
