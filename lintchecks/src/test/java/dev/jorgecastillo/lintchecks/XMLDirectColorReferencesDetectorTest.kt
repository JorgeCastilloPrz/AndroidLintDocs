package com.banno.android.lintchecks

import com.android.tools.lint.checks.infrastructure.TestFiles.xml
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class XMLDirectColorReferencesDetectorTest {

    @Test
    fun reportsDirectColorReferenceInLayouts() {
        lint().files(
            xml(
                "res/layout/test_layout.xml",
                """
                |<?xml version="1.0" encoding="utf-8"?>
                |<FrameLayout
                |    android:id="@+id/stepFragmentContainer"
                |    xmlns:android="http://schemas.android.com/apk/res/android"
                |    xmlns:tools="http://schemas.android.com/tools"
                |    android:layout_width="match_parent"
                |    android:layout_height="match_parent"
                |    android:background="@color/body_text_primary_color"
                |    tools:ignore="MergeRootFrame"/>
                """.trimMargin()
            )
        )
            .issues(XMLDirectColorReferencesDetector.ISSUE)
            .run()
            .expect(
                """
                res/layout/test_layout.xml:8: Error: Direct color reference "@color/body_text_primary_color", should use ?attr/ prefix for theme support [XMLDirectColorReference]
                    android:background="@color/body_text_primary_color"
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """.trimIndent()
            )
    }

    @Test
    fun reportsDirectColorReferenceInLayoutsWhenUsingCustomAttributes() {
        lint().files(
            xml(
                "res/layout/test_layout.xml",
                """
                |<?xml version="1.0" encoding="utf-8"?>
                |<com.banno.grip.widget.ThemableTextView
                |    android:id="@+id/stepFragmentContainer"
                |    xmlns:android="http://schemas.android.com/apk/res/android"
                |    xmlns:tools="http://schemas.android.com/tools"
                |    xmlns:app="http://schemas.android.com/apk/res-auto"
                |    android:layout_width="match_parent"
                |    android:layout_height="match_parent"
                |    app:drawableColor="@color/body_text_primary_color"
                |    tools:ignore="MergeRootFrame"/>
                """.trimMargin()
            )
        )
            .issues(XMLDirectColorReferencesDetector.ISSUE)
            .run()
            .expect(
                """
                res/layout/test_layout.xml:9: Error: Direct color reference "@color/body_text_primary_color", should use ?attr/ prefix for theme support [XMLDirectColorReference]
                    app:drawableColor="@color/body_text_primary_color"
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """.trimIndent()
            )
    }

    @Test
    fun doesNotReportThemeAttributeColorReferenceInLayouts() {
        lint().files(
            xml(
                "res/layout/test_layout.xml",
                """
                |<?xml version="1.0" encoding="utf-8"?>
                |<FrameLayout
                |    android:id="@+id/stepFragmentContainer"
                |    xmlns:android="http://schemas.android.com/apk/res/android"
                |    xmlns:tools="http://schemas.android.com/tools"
                |    android:layout_width="match_parent"
                |    android:layout_height="match_parent"
                |    android:background="?attr/body_text_primary_color"
                |    tools:ignore="MergeRootFrame"/>
                """.trimMargin()
            )
        )
            .issues(XMLDirectColorReferencesDetector.ISSUE)
            .run()
            .expectClean()
    }

    @Test
    fun doesNotReportThemeAttributeColorReferenceInLayoutsWhenUsingCustomAttributes() {
        lint().files(
            xml(
                "res/layout/test_layout.xml",
                """
                |<?xml version="1.0" encoding="utf-8"?>
                |<com.banno.grip.widget.ThemableTextView
                |    android:id="@+id/stepFragmentContainer"
                |    xmlns:android="http://schemas.android.com/apk/res/android"
                |    xmlns:tools="http://schemas.android.com/tools"
                |    xmlns:app="http://schemas.android.com/apk/res-auto"
                |    android:layout_width="match_parent"
                |    android:layout_height="match_parent"
                |    app:drawableColor="?attr/body_text_primary_color"
                |    tools:ignore="MergeRootFrame"/>
                """.trimMargin()
            )
        )
            .issues(XMLDirectColorReferencesDetector.ISSUE)
            .run()
            .expectClean()
    }

    @Test
    fun reportsDirectColorReferenceInMenus() {
        lint().files(
            xml(
                "res/menu/test_menu.xml",
                """
                |<?xml version="1.0" encoding="utf-8"?>
                |<menu xmlns:android="http://schemas.android.com/apk/res/android">
                |    <item
                |        android:id="@+id/add_cards"
                |        android:title="@string/add_more"
                |        android:icon="@drawable/icon_add_actionbar"/>
                |
                |    <item
                |        android:id="@+id/cancel_reordering"
                |        android:title="@string/cancel"
                |        android:icon="@drawable/icon_undo_actionbar"
                |        android:iconTint="@color/inline_icon_color"/>
                |</menu>
                """.trimMargin()
            )
        )
            .issues(XMLDirectColorReferencesDetector.ISSUE)
            .run()
            .expect(
                """
                res/menu/test_menu.xml:12: Error: Direct color reference "@color/inline_icon_color", should use ?attr/ prefix for theme support [XMLDirectColorReference]
                        android:iconTint="@color/inline_icon_color"/>
                        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """.trimIndent()
            )
    }

    @Test
    fun doesNotReportThemeAttributeColorReferenceInMenus() {
        lint().files(
            xml(
                "res/menu/test_menu.xml",
                """
                |<?xml version="1.0" encoding="utf-8"?>
                |<menu xmlns:android="http://schemas.android.com/apk/res/android">
                |    <item
                |        android:id="@+id/add_cards"
                |        android:title="@string/add_more"
                |        android:icon="@drawable/icon_add_actionbar"/>
                |
                |    <item
                |        android:id="@+id/cancel_reordering"
                |        android:title="@string/cancel"
                |        android:icon="@drawable/icon_undo_actionbar"
                |        android:iconTint="?attr/inline_icon_color"/>
                |</menu>
                """.trimMargin()
            )
        )
            .issues(XMLDirectColorReferencesDetector.ISSUE)
            .run()
            .expectClean()
    }

    @Test
    fun reportsDirectColorReferenceInDrawables() {
        lint().files(
            xml(
                "res/drawable/test_shape.xml",
                """
                |<?xml version="1.0" encoding="utf-8"?>
                |<shape xmlns:android="http://schemas.android.com/apk/res/android">
                |    <solid android:color="@color/body_text_primary_color"/>
                |</shape>
                """.trimMargin()
            )
        )
            .issues(XMLDirectColorReferencesDetector.ISSUE)
            .run()
            .expect(
                """
                res/drawable/test_shape.xml:3: Error: Direct color reference "@color/body_text_primary_color", should use ?attr/ prefix for theme support [XMLDirectColorReference]
                    <solid android:color="@color/body_text_primary_color"/>
                           ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """.trimIndent()
            )
    }

    @Test
    fun doesNotReportThemeAttributeColorReferenceInDrawables() {
        lint().files(
            xml(
                "res/drawable/test_shape.xml",
                """
                |<?xml version="1.0" encoding="utf-8"?>
                |<shape xmlns:android="http://schemas.android.com/apk/res/android">
                |    <solid android:color="?attr/body_text_primary_color"/>
                |</shape>
                """.trimMargin()
            )
        )
            .issues(XMLDirectColorReferencesDetector.ISSUE)
            .run()
            .expectClean()
    }

    @Test
    fun reportsDirectColorReferenceInColors() {
        lint().files(
            xml(
                "res/color/test_text_color_selector.xml",
                """
                |<?xml version="1.0" encoding="utf-8"?>
                |<selector xmlns:android="http://schemas.android.com/apk/res/android">
                |    <item android:state_pressed="true" android:color="@color/error_text_color"/>
                |    <item android:color="@color/body_text_alert_color"/>
                |</selector>
                """.trimMargin()
            )
        )
            .issues(XMLDirectColorReferencesDetector.ISSUE)
            .run()
            .expect(
                """
                res/color/test_text_color_selector.xml:3: Error: Direct color reference "@color/error_text_color", should use ?attr/ prefix for theme support [XMLDirectColorReference]
                    <item android:state_pressed="true" android:color="@color/error_text_color"/>
                                                       ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                res/color/test_text_color_selector.xml:4: Error: Direct color reference "@color/body_text_alert_color", should use ?attr/ prefix for theme support [XMLDirectColorReference]
                    <item android:color="@color/body_text_alert_color"/>
                          ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                2 errors, 0 warnings
                """.trimIndent()
            )
    }

    @Test
    fun doesNotReportThemeAttributeColorReferenceInColors() {
        lint().files(
            xml(
                "res/color/test_text_color_selector.xml",
                """
                |<?xml version="1.0" encoding="utf-8"?>
                |<selector xmlns:android="http://schemas.android.com/apk/res/android">
                |    <item android:state_pressed="true" android:color="?attr/error_text_color"/>
                |    <item android:color="?attr/body_text_alert_color"/>
                |</selector>
                """.trimMargin()
            )
        )
            .issues(XMLDirectColorReferencesDetector.ISSUE)
            .run()
            .expectClean()
    }

    @Test
    fun reportsDirectColorReferenceInValues() {
        lint().files(
            xml(
                "res/values/cards.xml",
                """
                |<?xml version="1.0" encoding="utf-8"?>
                |<resources>
                |    <color name="card_magnetic_ad">@color/magnetic_ad_background_color</color>
                |</resources>
                """.trimMargin()
            )
        )
            .issues(XMLDirectColorReferencesDetector.ISSUE)
            .run()
            .expect(
                """
                res/values/cards.xml:3: Error: Direct color reference "@color/magnetic_ad_background_color", should use ?attr/ prefix for theme support [XMLDirectColorReference]
                    <color name="card_magnetic_ad">@color/magnetic_ad_background_color</color>
                                                   ^
                1 errors, 0 warnings
                """.trimIndent()
            )
    }

    @Test
    fun doesNotReportThemeAttributeColorReferenceInValues() {
        lint().files(
            xml(
                "res/values/cards.xml",
                """
                |<?xml version="1.0" encoding="utf-8"?>
                |<resources>
                |    <color name="card_magnetic_ad">?attr/magnetic_ad_background_color</color>
                |</resources>
                """.trimMargin()
            )
        )
            .issues(XMLDirectColorReferencesDetector.ISSUE)
            .run()
            .expectClean()
    }

    @Test
    fun reportsDirectColorReferenceInStyles() {
        lint().files(
            xml(
                "res/values/styles.xml",
                """
                |<resources xmlns:ns2="http://schemas.android.com/apk/res-auto" xmlns:tools="http://schemas.android.com/tools">
                |   <!-- Base application theme. -->
                |   <style name="AppTheme" parent="Theme.MaterialComponents.DayNight.NoActionBar">
                |       <item name="colorPrimary">@color/toolbar_color</item>
                |   </style>
                |</resources>
                """.trimMargin()
            )
        )
            .issues(XMLDirectColorReferencesDetector.ISSUE)
            .run()
            .expect(
                """
                res/values/styles.xml:4: Error: Direct color reference "@color/toolbar_color", should use ?attr/ prefix for theme support [XMLDirectColorReference]
                       <item name="colorPrimary">@color/toolbar_color</item>
                                                 ^
                1 errors, 0 warnings
                """.trimIndent()
            )
    }

    @Test
    fun doesNotReportThemeAttributeColorReferenceInStyles() {
        lint().files(
            xml(
                "res/values/styles.xml",
                """
                |<resources xmlns:ns2="http://schemas.android.com/apk/res-auto" xmlns:tools="http://schemas.android.com/tools">
                |   <!-- Base application theme. -->
                |   <style name="AppTheme" parent="Theme.MaterialComponents.DayNight.NoActionBar">
                |       <item name="colorPrimary">?attr/toolbar_color</item>
                |   </style>
                |</resources>
                """.trimMargin()
            )
        )
            .issues(XMLDirectColorReferencesDetector.ISSUE)
            .run()
            .expectClean()
    }

    @Test
    fun doesNotReportDirectColorReferenceToSelectorsInStyles() {
        lint().files(
            xml(
                "res/values/styles.xml",
                """
                |<resources xmlns:ns2="http://schemas.android.com/apk/res-auto" xmlns:tools="http://schemas.android.com/tools">
                |   <!-- Base application theme. -->
                |   <style name="AppTheme" parent="Theme.MaterialComponents.DayNight.NoActionBar">
                |       <item name="colorPrimary">@color/selector_something</item>
                |   </style>
                |</resources>
                """.trimMargin()
            )
        )
            .issues(XMLDirectColorReferencesDetector.ISSUE)
            .run()
            .expectClean()
    }
}
