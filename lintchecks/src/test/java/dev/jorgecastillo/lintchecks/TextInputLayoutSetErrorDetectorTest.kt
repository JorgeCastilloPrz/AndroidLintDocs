package com.banno.android.lintchecks

import com.android.tools.lint.checks.infrastructure.LintDetectorTest.java
import com.android.tools.lint.checks.infrastructure.LintDetectorTest.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class TextInputLayoutSetErrorDetectorTest {

    @Test
    fun `setError call reports in Java`() {
        lint().files(
            java(
                """
                package com.google.android.material.textfield;
                
                import android.content.Context;
                import androidx.appcompat.widget.AppCompatTextView;
                
                public class TextInputLayout extends AppCompatTextView {
                    public TextInputLayout(Context context) {
                        super(context);
                    }
                    
                    public void setError(CharSequence input) {
                    }
                }
                """.trimIndent()
            ),
            java(
                """
                package test.pkg;

                import android.content.Context;
                import androidx.appcompat.widget.AppCompatTextView;

                import com.google.android.material.textfield.TextInputLayout;

                public class CustomTextView extends AppCompatTextView {

                    public CustomTextView(Context context) {
                        super(context);
                        init();
                    }

                    public void init() {
                        TextInputLayout input = new TextInputLayout(getContext());
                        input.setError("Some error");
                    }
                }
                """.trimIndent()
            )
        )
            .issues(TextInputLayoutSetErrorDetector.ISSUE)
            .run()
            .expect(
                """
                src/test/pkg/CustomTextView.java:17: Error: Should use setErrorIfChanged() instead of setError function. [TextInputLayoutProblematicSetErrorCall]
                        input.setError("Some error");
                        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """.trimIndent()
            )
    }

    @Test
    fun `setErrorIfChanged does not report in Java`() {
        lint().files(
            java(
                """
                package com.google.android.material.textfield;
                
                import android.content.Context;
                import androidx.appcompat.widget.AppCompatTextView;
                
                public class TextInputLayout extends AppCompatTextView {
                    public TextInputLayout(Context context) {
                        super(context);
                    }
                    
                    public void setError(CharSequence input) {
                    }
                }
                """.trimIndent()
            ),
            java(
                """
                package test.pkg;

                import android.content.Context;
                import androidx.appcompat.widget.AppCompatTextView;
                import com.banno.grip.util.EditTextUtilKt;
                import com.google.android.material.textfield.TextInputLayout;

                public class CustomTextView extends AppCompatTextView {

                    public CustomTextView(Context context) {
                        super(context);
                        init();
                    }

                    public void init() {
                        TextInputLayout input = new TextInputLayout(getContext());
                        EditTextUtilKt.setErrorIfChanged(input, "Some error");
                    }
                }
                """.trimIndent()
            )
        )
            .issues(TextInputLayoutSetErrorDetector.ISSUE)
            .run()
            .expectClean()
    }

    @Test
    fun `setError call reports in Kotlin with explicit setError call instead of property assignment`() {
        lint().files(
            kotlin(
                """
                package com.google.android.material.textfield;
                
                import android.content.Context
                import androidx.appcompat.widget.AppCompatTextView
                
                class TextInputLayout : AppCompatTextView {
                    constructor(context: Context) : this(context, null)
                    
                    fun setError(input: CharSequence) {
                    }
                }
                """.trimIndent()
            ),
            kotlin(
                """
                package test.pkg;

                import android.content.Context
                import androidx.appcompat.widget.AppCompatTextView
                import com.google.android.material.textfield.TextInputLayout

                class CustomTextView : AppCompatTextView {

                    constructor(context: Context) : this(context, null)

                    init {
                        val input = TextInputLayout(context)
                        input.setError("Some error")
                    }
                }
                """.trimIndent()
            )
        )
            .issues(TextInputLayoutSetErrorDetector.ISSUE)
            .run()
            .expect(
                """
                src/test/pkg/CustomTextView.kt:13: Error: Should use setErrorIfChanged() instead of setError function. [TextInputLayoutProblematicSetErrorCall]
                        input.setError("Some error")
                        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """.trimIndent()
            )
    }

    @Test
    fun `input error property assignment reports in Kotlin`() {
        lint().files(
            kotlin(
                """
                package com.google.android.material.textfield;
                
                import android.content.Context
                import androidx.appcompat.widget.AppCompatTextView
                
                class TextInputLayout : AppCompatTextView {
                    constructor(context: Context) : this(context, null)
                    
                    var error: CharSequence = "Default error"
                }
                """.trimIndent()
            ),
            kotlin(
                """
                package test.pkg;

                import android.content.Context
                import androidx.appcompat.widget.AppCompatTextView
                import com.google.android.material.textfield.TextInputLayout

                class CustomTextView : AppCompatTextView {

                    constructor(context: Context) : this(context, null)

                    init {
                        val input = TextInputLayout(context)
                        input.error = "Some error"
                    }
                }
                """.trimIndent()
            )
        )
            .issues(TextInputLayoutSetErrorDetector.ISSUE)
            .run()
            .expect(
                """
                src/test/pkg/CustomTextView.kt:13: Error: Should use setErrorIfChanged() instead of error property assignment. [TextInputLayoutProblematicSetErrorCall]
                        input.error = "Some error"
                              ~~~~~
                1 errors, 0 warnings
                """.trimIndent()
            )
    }
}
