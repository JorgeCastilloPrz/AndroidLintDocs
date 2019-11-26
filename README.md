## Android Lint Docs

Lint source of knowledge. This is a try to gather all possible knowledge about Android Lint in a single place. It covers:

* Android Lint setup docs.
* Writing custom rules.
* Writing tests for custom rules.

## Setup Lint checks in separate module

* Create a Kotlin only library module (i.e: `lintchecks`).
* Add the following code to the module `build.gradle` file:

```groovy
apply plugin: 'kotlin'

test {
    testLogging {
        events "passed", "skipped", "failed", "standardOut", "standardError"
    }
}

jar {
    manifest {
        attributes("Lint-Registry-v2": "dev.jorgecastillo.lintchecks.CustomIssueRegistry")
    }
}

compileJava {
    sourceCompatibility = '1.8'
    targetCompatibility = '1.8'
}

dependencies {
    compileOnly("com.android.tools.lint:lint-api:$androidLintVersion")
    compileOnly("com.android.tools.lint:lint-checks:$androidLintVersion")

    testImplementation "com.android.tools.lint:lint:$androidLintVersion"
    testImplementation "com.android.tools.lint:lint-tests:$androidLintVersion"
    testImplementation "com.android.tools:testutils:$androidLintVersion"

    // Kotlin
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version"
}
```

Having `androidLintVersion` declared in main module like:
```groovy
buildscript {
    ext.androidLintVersion = '26.5.2'
}
```

Custom lint rules are registered through the `jar` task above that points to the custom lint rules registry: `dev.jorgecastillo.lintchecks.CustomIssueRegistry`. That's an actual file we've got in the module, where all the custom rules are registered, like:

```kotlin
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
```

Then you just need to depend on this `lintchecks` kotlin module from the modules you want those checks to be installed and passed on, like the `app` one in the case of this sample.

```groovy
dependencies {
    implementation project(":lintchecks")
}
```

And that's pretty much it, you're good to go 🎉

## How to write custom rules

For samples on how to write custom Lint rules, look at [the ones provided in this repo](https://github.com/JorgeCastilloPrz/AndroidLintDocs/tree/master/lintchecks/src/main/java/dev/jorgecastillo/lintchecks).

For more diverse samples you can also take a look at any classes extending `Detector`, there are tons of them that come built into Lint that you can take a look at in your IDE. Those should be available right away. You can also take a look at them [here](https://android.googlesource.com/platform/tools/base/+/master/lint/libs/lint-checks/src/main/java/com/android/tools/lint/checks).

## How to test custom rules

For samples on how to write tests, take a look at [the ones provided in this repo](https://github.com/JorgeCastilloPrz/AndroidLintDocs/tree/master/lintchecks/src/test/java/dev/jorgecastillo/lintchecks).