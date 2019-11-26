package dev.jorgecastillo.lintchecks

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Detector.UastScanner
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.getUCallExpression
import org.jetbrains.uast.kotlin.KotlinUFunctionCallExpression

/**
 * Lint check that looks for usages of resources.getColorStateList(colorResId) given those break multi-theme
 * compatibility. Suggests AppCompatResources.getColorStateList(context, colorResId) as an alternative.
 */
class NotThemedColorStateListDetector : Detector(), UastScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement>>? {
        return listOf<Class<out UElement>>(UExpression::class.java)
    }

    override fun createUastHandler(context: JavaContext): UElementHandler? {
        return object : UElementHandler() {
            override fun visitExpression(node: UExpression) {
                val methodName =
                    (node as? UCallExpression)?.methodName ?: (node as? KotlinUFunctionCallExpression)?.methodName

                val receiver = (node as? UCallExpression)?.receiver?.asSourceString() ?: ""

                if (methodName == "getColorStateList" &&
                    (receiver.endsWith("resources") || receiver.endsWith("getResources()")) &&
                    node.getUCallExpression()?.valueArgumentCount ?: 0 < 2
                ) {
                    context.report(
                        ISSUE, node, context.getLocation(node),
                        "This code uses resources.getColorStateList(colorResId) which " +
                                "removes support for multiple themes. Please use " +
                                "AppCompatResources.getColorStateList(context, colorResId)."
                    )
                }
            }
        }
    }

    companion object {

        val ISSUE = Issue.create(
            id = "NotThemedColorStateList",
            briefDescription = "Not themed color state list",
            explanation = "This check forbids any usages of " +
                    "resources.getColorStateList(colorResId) in code " +
                    "since those break multiple theme compatibility (it passes null for the theme under the hood). " +
                    "Please use AppCompatResources.getColorStateList(context, colorResId) to ensure the " +
                    "selector is properly themed following the context theme.",
            category = Category.CORRECTNESS,
            priority = 8,
            severity = Severity.ERROR,
            implementation = Implementation(
                NotThemedColorStateListDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }
}