package dev.jorgecastillo.lintchecks

import com.android.tools.lint.checks.infrastructure.TestFiles.java
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class KotlinAndJavaDirectColorReferencesDetectorTest {

    @Test
    fun attributeReferenceDoesNotReportJava() {
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
                         int textColor = AttributeExtensionsKt.getColorIntFromAttr(
                            context,
                            R.attr.body_text_primary_color);

                         setTextColor(textColor);
                     }
                }""".trimIndent()
            )
        )
            .issues(KotlinAndJavaDirectColorReferencesDetector.ISSUE)
            .run()
            .expectClean()
    }

    @Test
    fun attributeReferenceDoesNotReportKotlin() {
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
                        setTextColor(context.getColorIntFromAttr(R.attr.body_text_primary_color))
                    }
                }
                """.trimIndent()
            )
        )
            .issues(KotlinAndJavaDirectColorReferencesDetector.ISSUE)
            .run()
            .expectClean()
    }

    @Test
    fun directColorReferenceDoesReportJava() {
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
                         setTextColor(R.color.body_text_primary_color);
                     }
                }""".trimIndent()
            )
        )
            .issues(KotlinAndJavaDirectColorReferencesDetector.ISSUE)
            .run()
            .expect(
                """
                src/test/pkg/CustomTextView.java:28: Error: This code uses R.color.body_text_primary_color which is a direct color reference. Direct color references remove support for theme colors. Please use AttributeExtensionsKt.getColorIntFromAttr(getContext(), R.attr.body_text_primary_color) instead [KotlinAndJavaDirectColorReference]
                         setTextColor(R.color.body_text_primary_color);
                                      ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """.trimIndent()
            )
    }

    @Test
    fun directColorReferenceDoesReportKotlin() {
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
            .issues(KotlinAndJavaDirectColorReferencesDetector.ISSUE)
            .run()
            .expect(
                """
                src/test/pkg/KotlinView.kt:15: Error: This code uses R.color.body_text_primary_color which is a direct color reference. Direct color references remove support for theme colors. Please use context.getColorIntFromAttr(R.attr.body_text_primary_color) instead. [KotlinAndJavaDirectColorReference]
                        setTextColor(resources.getColor(R.color.body_text_primary_color))
                                                        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """.trimIndent()
            )
    }
}
