package com.banno.android.lintchecks

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Detector.UastScanner
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.java.JavaUCallExpression
import org.jetbrains.uast.kotlin.KotlinUFunctionCallExpression

/**
 * Lint check that looks for usages of ContextCompat.getColor(context, colorResId) and resources.getColor(colorResId)
 * given those do not support theming for all Android versions we provide support for (API 21+).
 *
 * ContextCompat.getColor() implementation delegates in resources.getColor(resId) for versions older than API 23.
 *
 * resources.getColor(resId) passes null for the context theme (hence it doesn't support theming).
 *
 * The only variant allowed would be resources.getColor(resId, theme), where you can pass context.getTheme(), but that
 * is minSDK 23, so we can't use it for now.
 */
class NotThemedGetColorDetector : Detector(), UastScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement>>? {
        return listOf<Class<out UElement>>(UExpression::class.java)
    }

    override fun createUastHandler(context: JavaContext): UElementHandler? {
        return object : UElementHandler() {
            override fun visitExpression(node: UExpression) {
                val nodeString = node.asSourceString()
                if (node.shouldReport()) {
                    val isJava = node is JavaUCallExpression

                    val colorReference = if (isJava) {
                        (node as JavaUCallExpression).valueArguments[0].asSourceString()
                    } else {
                        (node as KotlinUFunctionCallExpression).valueArguments[0].asSourceString()
                    }

                    val colorResId = colorReference
                        .removePrefix("R.color.")
                        .removePrefix("R.attr.")

                    val quickfix = fix().replace()
                        .text(nodeString)
                        .with(
                            if (isJava) {
                                "AttributeExtensionsKt.getColorIntFromAttr(getContext(), R.attr.$colorResId)"
                            } else {
                                "context.getColorIntFromAttr(R.attr.$colorResId)"
                            }
                        ).build()

                    context.report(
                        issue = ISSUE,
                        scope = node,
                        location = context.getLocation(node),
                        message = "This code uses a getColor variant that does not support theming. Please use " +
                                if (isJava) {
                                    "AttributeExtensionsKt.getColorIntFromAttr(getContext(), R.attr.$colorResId) instead"
                                } else {
                                    "context.getColorIntFromAttr(R.attr.$colorResId) instead."
                                },
                        quickfixData = quickfix
                    )
                }
            }
        }
    }

    private fun UExpression.shouldReport(): Boolean =
        (asSourceString().startsWith("getResources().getColor") && this is JavaUCallExpression) ||
                (asSourceString().startsWith("getColor") && this is KotlinUFunctionCallExpression &&
                        (this.receiver?.asSourceString() == "resources" ||
                                this.receiver?.asSourceString() == "getResources()"))

    companion object {

        val ISSUE = Issue.create(
            id = "NotThemedGetColorCall",
            briefDescription = "Calling not themable getColor",
            explanation = "This checks for calls to resrouces.getColor(colorResId) or " +
                    "ContextCompat.getColor(colorResId), given those pass null for the context theme.",
            category = Category.CORRECTNESS,
            priority = 8,
            severity = Severity.ERROR,
            implementation = Implementation(
                NotThemedGetColorDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }
}
