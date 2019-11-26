package dev.jorgecastillo.lintchecks

import com.android.tools.lint.checks.infrastructure.TestFiles.java
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class NotThemedColorStateListDetectorTest {

    @Test
    fun appCompatResourcesVariantDoesNotReportJava() {
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
                     setTextColor(
                        AppCompatResources.getColorStateList(
                            getContext(),
                            R.color.selector_contact_button_text_color)
                        );
                     }
                 }
                 """
                    .trimIndent()
            )
        )
            .issues(NotThemedColorStateListDetector.ISSUE)
            .run()
            .expectClean()
    }

    @Test
    fun appCompatResourcesVariantDoesNotReportKotlin() {
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
                        setTextColor(
                            AppCompatResources.getColorStateList(
                                getContext(),
                                R.color.selector_contact_button_text_color
                            )
                        )
                    }
                }
                """.trimIndent()
            )
        )
            .issues(NotThemedColorStateListDetector.ISSUE)
            .run()
            .expectClean()
    }

    @Test
    fun resourcesVariantWithExplicitThemePassedInDoesNotReportJava() {
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
                        val color = getResources().getColorStateList(
                            R.color.selector_account_card_background_color, 
                            context.getTheme());
                        setTextColor(color);
                    }
                 }
                 """
                    .trimIndent()
            )
        )
            .issues(NotThemedColorStateListDetector.ISSUE)
            .run()
            .expectClean()
    }

    @Test
    fun resourcesVariantWithExplicitThemePassedInDoesNotReportKotlin() {
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
                        val color = resources.getColorStateList(
                            R.color.selector_account_card_background_color, 
                            context.theme)
                        setTextColor(color)
                    }
                }
                """.trimIndent()
            )
        )
            .issues(NotThemedColorStateListDetector.ISSUE)
            .run()
            .expectClean()
    }

    @Test
    fun resourcesVariantDoesReportJava() {
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
        
                    public CustomTextView (Context context) {
                        super(context);
                        init();
                    }
        
                    public CustomTextView (Context context, @Nullable AttributeSet attrs) {
                        super(context, attrs);
                        init();
                    }
        
                    public CustomTextView (Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
                        super(context, attrs, defStyleAttr);
                        init();
                    }
        
                    public void init() {
                        setTextColor(
                            getResources().getColorStateList(
                                R.color.selector_contact_button_icon_color
                            )
                        );
                    }
                }
                """.trimIndent()
            )
        )
            .issues(NotThemedColorStateListDetector.ISSUE)
            .run()
            .expect(
                """
                src/test/pkg/CustomTextView.java:30: Error: This code uses resources.getColorStateList(colorResId) which removes support for multiple themes. Please use AppCompatResources.getColorStateList(context, colorResId). [NotThemedColorStateList]
                            getResources().getColorStateList(
                            ^
                1 errors, 0 warnings
                """.trimIndent()
            )
    }

    @Test
    fun resourcesVariantDoesReportKotlin() {
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
                        setTextColor(resources.getColorStateList(R.color.selector_contact_button_icon_color))
                    }
                }
                """.trimIndent()
            )
        )
            .issues(NotThemedColorStateListDetector.ISSUE)
            .run()
            .expect(
                """
                src/test/pkg/KotlinView.kt:15: Error: This code uses resources.getColorStateList(colorResId) which removes support for multiple themes. Please use AppCompatResources.getColorStateList(context, colorResId). [NotThemedColorStateList]
                        setTextColor(resources.getColorStateList(R.color.selector_contact_button_icon_color))
                                     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """.trimIndent()
            )
    }

    @Test
    fun indirectReferenceFromViewContextDoesReportJava() {
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
        
                    public CustomTextView (Context context) {
                        super(context);
                        init();
                    }
        
                    public CustomTextView (Context context, @Nullable AttributeSet attrs) {
                        super(context, attrs);
                        init();
                    }
        
                    public CustomTextView (Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
                        super(context, attrs, defStyleAttr);
                        init();
                    }
        
                    public void init() {
                        int color = getContext().getResources().getColorStateList(
                            R.color.selector_contact_button_icon_color);
                        setTextColor(color);
                    }
                }
                """.trimIndent()
            )
        )
            .issues(NotThemedColorStateListDetector.ISSUE)
            .run()
            .expect(
                """
                src/test/pkg/CustomTextView.java:29: Error: This code uses resources.getColorStateList(colorResId) which removes support for multiple themes. Please use AppCompatResources.getColorStateList(context, colorResId). [NotThemedColorStateList]
                        int color = getContext().getResources().getColorStateList(
                                    ^
                1 errors, 0 warnings
                """.trimIndent()
            )
    }

    @Test
    fun indirectReferenceFromViewContextDoesReportKotlin() {
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
                        val color = context.resources.getColorStateList(R.color.selector_contact_button_icon_color)
                        setTextColor(color)
                    }
                }
                """.trimIndent()
            )
        )
            .issues(NotThemedColorStateListDetector.ISSUE)
            .run()
            .expect(
                """
                src/test/pkg/KotlinView.kt:15: Error: This code uses resources.getColorStateList(colorResId) which removes support for multiple themes. Please use AppCompatResources.getColorStateList(context, colorResId). [NotThemedColorStateList]
                        val color = context.resources.getColorStateList(R.color.selector_contact_button_icon_color)
                                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """.trimIndent()
            )
    }
}
