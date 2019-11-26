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
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.java.JavaUQualifiedReferenceExpression

/**
 * Lint check that looks for usages of R.color given instead of R.attr from Kotlin and Java files.
 */
class KotlinAndJavaDirectColorReferencesDetector : Detector(), UastScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement>>? {
        return listOf<Class<out UElement>>(UExpression::class.java)
    }

    override fun createUastHandler(context: JavaContext): UElementHandler? {
        return object : UElementHandler() {
            override fun visitExpression(node: UExpression) {
                val nodeString = node.asSourceString()
                if (node.shouldReport()) {
                    val colorResId = nodeString.removePrefix("R.color.")

                    val isJava = node is JavaUQualifiedReferenceExpression

                    val quickfix = fix().replace()
                        .text(nodeString)
                        .with(
                            if (isJava) {
                                "AttributeExtensionsKt.getColorIntFromAttr(getContext(), R.attr.$colorResId"
                            } else {
                                "context.getColorIntFromAttr(R.attr.$colorResId)"
                            }
                        ).build()

                    context.report(
                        issue = ISSUE,
                        scope = node,
                        location = context.getLocation(node),
                        message = "This code uses R.color.$colorResId which is a direct color reference. Direct color " +
                                "references remove support for theme colors. Please use " +
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

    /**
     * We want to report all usages of R.color in code that are not just `R.color` (that's an evaluated expression by
     * itself so it'd report the same thing twice if we let it be) and that are not selectors. As explained in
     * [XMLDirectColorReferencesDetector.shouldReport], selectors are the only ones allowed to be referenced directly,
     * given it's the colors referenced from within them the ones to be themed, not the selector itself.
     */
    private fun UExpression.shouldReport(): Boolean = asSourceString().let { nodeString ->
        nodeString.startsWith("R.color") &&
                nodeString != "R.color" &&
                !nodeString.removePrefix("R.color.").startsWith("selector_")
    }

    companion object {

        val ISSUE = Issue.create(
            id = "KotlinAndJavaDirectColorReference",
            briefDescription = "Direct color reference in code",
            explanation = "This check forbids any usages of R.color in code (.kt and .java files) since those break " +
                    "multiple theme compatibility. Please use R.attr to ensure the color is themed following the " +
                    "context theme. Use `Context.getColorIntFromAttr(@AttrRes attr: Int): Int` for loading colors by " +
                    "theme attribute.",
            category = Category.CORRECTNESS,
            priority = 8,
            severity = Severity.ERROR,
            implementation = Implementation(
                KotlinAndJavaDirectColorReferencesDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }
}