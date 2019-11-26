package com.banno.android.lintchecks

import com.android.tools.lint.checks.infrastructure.TestFiles.java
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class NotThemedGetColorDetectorTest {

    @Test
    fun resourcesGetColorDoesReportJava() {
        lint().files(
            java(
                """
                package test.pkg;
                
                import android.content.Context;
                import android.util.AttributeSet;
                import androidx.annotation.Nullable;
                import androidx.appcompat.content.res.AppCompatResources;
                import androidx.appcompat.widget.AppCompatTextView;
                import com.banno.grip.R;
             
                public class CustomTextView extends AppCompatTextView {
            
                    public CustomTextView(Context context) {
                         super(context);
                        init();
                    }
                
                     public CustomTextView(Context context, @Nullable AttributeSet attrs) {
                         super(context, attrs);
                         init();
                     }
                
                     public CustomTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
                         super(context, attrs, defStyleAttr);
                         init();
                     }
                
                     public void init() {
                         setTextColor(getResources().getColor(R.color.body_text_primary_color));
                     }
                }""".trimIndent()
            )
        )
            .issues(NotThemedGetColorDetector.ISSUE)
            .run()
            .expect(
                """
                src/test/pkg/CustomTextView.java:28: Error: This code uses a getColor variant that does not support theming. Please use AttributeExtensionsKt.getColorIntFromAttr(getContext(), R.attr.body_text_primary_color) instead [NotThemedGetColorCall]
                         setTextColor(getResources().getColor(R.color.body_text_primary_color));
                                      ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """.trimIndent()
            )
    }

    @Test
    fun resourcesGetColorDoesReportJavaWhenAssignedToField() {
        lint().files(
            java(
                """
                package test.pkg;
                
                import android.content.Context;
                import android.util.AttributeSet;
                import androidx.annotation.Nullable;
                import androidx.appcompat.content.res.AppCompatResources;
                import androidx.appcompat.widget.AppCompatTextView;
                import androidx.annotation.ColorInt;
                import com.banno.grip.R;
             
                public class CustomTextView extends AppCompatTextView {
                
                    @ColorInt
                    private int mCurrentFilterColor;
            
                    public CustomTextView(Context context) {
                         super(context);
                        init();
                    }
                
                     public CustomTextView(Context context, @Nullable AttributeSet attrs) {
                         super(context, attrs);
                         init();
                     }
                
                     public CustomTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
                         super(context, attrs, defStyleAttr);
                         init();
                     }
                
                     public void init() {
                         mCurrentFilterColor = getResources().getColor(R.color.transparent);
                     }
                }""".trimIndent()
            )
        )
            .issues(NotThemedGetColorDetector.ISSUE)
            .run()
            .expect(
                """
                src/test/pkg/CustomTextView.java:32: Error: This code uses a getColor variant that does not support theming. Please use AttributeExtensionsKt.getColorIntFromAttr(getContext(), R.attr.transparent) instead [NotThemedGetColorCall]
                         mCurrentFilterColor = getResources().getColor(R.color.transparent);
                                               ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """.trimIndent()
            )
    }

    @Test
    fun resourcesGetColorDoesReportKotlin() {
        lint().files(
            kotlin(
                """
                package test.pkg;

                import android.content.Context
                import android.util.AttributeSet
                import android.widget.TextView
                import com.banno.grip.R
                
                class KotlinView : TextView {
                    constructor(context: Context) : this(context, null)
                    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
                    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
                    }
                
                    fun setupTextColor() {
                        setTextColor(resources.getColor(R.color.body_text_primary_color))
                    }
                }
                """.trimIndent()
            )
        )
            .issues(NotThemedGetColorDetector.ISSUE)
            .run()
            .expect(
                """
                src/test/pkg/KotlinView.kt:15: Error: This code uses a getColor variant that does not support theming. Please use context.getColorIntFromAttr(R.attr.body_text_primary_color) instead. [NotThemedGetColorCall]
                        setTextColor(resources.getColor(R.color.body_text_primary_color))
                                     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """.trimIndent()
            )
    }

    @Test
    fun resourcesGetColorDoesReportKotlinWhenAssignedToField() {
        lint().files(
            kotlin(
                """
                package test.pkg;

                import android.content.Context
                import android.util.AttributeSet
                import android.widget.TextView
                import com.banno.grip.R
                
                class KotlinView : TextView {
                    constructor(context: Context) : this(context, null)
                    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
                    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
                    }
                    
                    @ColorInt private val mCurrentFilterColor
                
                    fun setupTextColor() {
                        mCurrentFilterColor = resources.getColor(R.color.transparent)
                    }
                }
                """.trimIndent()
            )
        )
            .issues(NotThemedGetColorDetector.ISSUE)
            .run()
            .expect(
                """
                src/test/pkg/KotlinView.kt:17: Error: This code uses a getColor variant that does not support theming. Please use context.getColorIntFromAttr(R.attr.transparent) instead. [NotThemedGetColorCall]
                        mCurrentFilterColor = resources.getColor(R.color.transparent)
                                              ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """.trimIndent()
            )
    }

    @Test
    fun resourcesGetColorDoesReportKotlinWithJavaLikeSyntaxOnReceiver() {
        lint().files(
            kotlin(
                """
                package test.pkg;

                import android.content.Context
                import android.util.AttributeSet
                import android.widget.TextView
                import com.banno.grip.R
                
                class KotlinView : TextView {
                    constructor(context: Context) : this(context, null)
                    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
                    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
                    }
                
                    fun setupTextColor() {
                        setTextColor(getResources().getColor(R.color.body_text_primary_color))
                    }
                }
                """.trimIndent()
            )
        )
            .issues(NotThemedGetColorDetector.ISSUE)
            .run()
            .expect(
                """
                src/test/pkg/KotlinView.kt:15: Error: This code uses a getColor variant that does not support theming. Please use context.getColorIntFromAttr(R.attr.body_text_primary_color) instead. [NotThemedGetColorCall]
                        setTextColor(getResources().getColor(R.color.body_text_primary_color))
                                     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """.trimIndent()
            )
    }
}
