package dev.jorgecastillo.lintchecks

import com.android.SdkConstants.TAG_RESOURCES
import com.android.SdkConstants.TAG_STYLE
import com.android.resources.ResourceFolderType
import com.android.tools.lint.detector.api.BinaryResourceScanner
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.LayoutDetector
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.XmlContext
import com.android.tools.lint.detector.api.XmlScannerConstants
import com.android.utils.XmlUtils
import org.w3c.dom.Attr
import org.w3c.dom.Element
import org.w3c.dom.Node

/**
 * Scans for direct color references (@color/) within XML files. Suggests moving to (?attr/) so colors are imposed by
 * the application theme.
 */
class XMLDirectColorReferencesDetector : LayoutDetector(), BinaryResourceScanner {

    /**
     * We're interested on scanning all attributes for safety, not just a subset since it's a bit unpredictable to know
     * the exact attributes where colors can be found. There are many variants (including custom attributes from
     * styleables).
     */
    override fun getApplicableAttributes(): Collection<String>? {
        return XmlScannerConstants.ALL
    }

    /**
     * Styles and color definitions are visited following the value returned by this method.
     */
    override fun getApplicableElements(): Collection<String>? {
        return listOf(TAG_RESOURCES, TAG_STYLE)
    }

    /**
     * Resources directories that can potentially contain direct color references in any form.
     */
    override fun appliesTo(folderType: ResourceFolderType): Boolean {
        return (folderType == ResourceFolderType.LAYOUT ||
                folderType == ResourceFolderType.MENU ||
                folderType == ResourceFolderType.DRAWABLE ||
                folderType == ResourceFolderType.VALUES ||
                folderType == ResourceFolderType.COLOR)
    }

    /**
     * Visits each attribute used in XML files in layout/, menu/, drawable/, values/, and color/ resource folders.
     * It just contemplates attributes, so things like color definitions in color files are not covered with this
     * method.
     */
    override fun visitAttribute(context: XmlContext, attribute: Attr) {
        val value = attribute.value
        if (value.isNotEmpty() && value.shouldReport()) {
            val quickfix = fix().set(
                attribute.namespaceURI,
                attribute.localName,
                attribute.value.replace("@color/", "?attr/")
            ).build()

            context.report(attribute, attribute, quickfix)
        }
    }

    /**
     * Visits styles and color definitions.
     */
    override fun visitElement(context: XmlContext, element: Element) {
        for (item in XmlUtils.getSubTags(element)) {
            val firstChild = item.firstChild
            if (firstChild != null && firstChild.nodeValue.shouldReport()) {
                val quickfix = fix().replace()
                    .text(firstChild.nodeValue)
                    .with(firstChild.nodeValue.replace("@color/", "?attr/"))
                    .build()

                context.report(firstChild, firstChild, quickfix)
            }
        }
    }

    private fun XmlContext.report(
        scope: Node,
        elementToReportAbout: Node,
        quickfix: LintFix
    ) {
        this.report(
            issue = ISSUE,
            scope = scope,
            location = this.getLocation(elementToReportAbout),
            message = String.format(
                "Direct color reference \"%1\$s\", should use `?attr/` prefix for theme support",
                elementToReportAbout.nodeValue
            ),
            quickfixData = quickfix
        )
    }

    /**
     * Selectors are allowed since we reference selectors with direct references (i.e: @color/selector_whatever) and
     * those reference theme attributes within themselves. So the selector itself is fine to stay as is. This exception
     * also helps a lot on not needing to add a bunch more hundreds of Lint ignores to all those occurrences.
     */
    private fun String.shouldReport() = this.startsWith("@color/") &&
            !this.removePrefix("@color/").startsWith("selector_")

    companion object {

        @JvmField
        val ISSUE = Issue.create(
            id = "XMLDirectColorReference",
            briefDescription = "Direct color reference in XML file",
            explanation = """
                Direct color references like \"@color/\" are not supported in XML files given those break multi-theme 
                support. Please use one of the theme color attributes using the \"?attr/\" prefix.
                """,
            category = Category.CORRECTNESS,
            priority = 10,
            severity = Severity.ERROR,
            implementation = Implementation(
                XMLDirectColorReferencesDetector::class.java,
                Scope.RESOURCE_FILE_SCOPE
            )
        )
    }
}
