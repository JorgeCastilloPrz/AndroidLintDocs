package dev.jorgecastillo.lintchecks

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.CURRENT_API

/**
 * Required to register the custom lint checks. Look at build.gradle under this module to find how
 * it's registered:
 *
 * jar {
 *   manifest {
 *       attributes("Lint-Registry-v2": "com.foo.bar.MyIssueRegistry")
 *   }
 * }
 */
@Suppress("unused")
class CustomIssueRegistry : IssueRegistry() {

    override val api = CURRENT_API

    override val issues = listOf(
        EpoxyModelDetector.DATA_CLASS_ISSUE,
        NotThemedGetColorDetector.ISSUE,
        NotThemedColorStateListDetector.ISSUE,
        XMLDirectColorReferencesDetector.ISSUE,
        KotlinAndJavaDirectColorReferencesDetector.ISSUE,
        TextInputLayoutSetErrorDetector.ISSUE
    )
}
