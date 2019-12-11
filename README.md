## Android Lint Docs [![CircleCI](https://circleci.com/gh/JorgeCastilloPrz/AndroidLintDocs/tree/master.svg?style=svg&circle-token=1d8f3ab1c4338786d0382ae5818130c6bab4a461)](https://circleci.com/gh/JorgeCastilloPrz/AndroidLintDocs/tree/master)

Android Lint bible. This is a try to gather all possible knowledge about Android Lint in a single place. It covers:

* Android Lint setup docs.
* Writing custom rules.
* Writing tests for custom rules.
* Gotchas and disclaimers.

## Setup Lint checks in separate module

* Create a Kotlin only library module (i.e: `lintchecks`).
* Add the following code to the module `build.gradle` file:

```groovy
jar {
    manifest {
        // Registering custom lint checks by pointing to your custom issue registry
        // (you'll find one example below).
        attributes("Lint-Registry-v2": "dev.jorgecastillo.lintchecks.CustomIssueRegistry")
    }
}

dependencies {
    // these two are used to write the custom rules and the registry.
    compileOnly("com.android.tools.lint:lint-api:$androidLintVersion")
    compileOnly("com.android.tools.lint:lint-checks:$androidLintVersion")

    // these are used to write the custom rule tests
    testImplementation "com.android.tools.lint:lint:$androidLintVersion"
    testImplementation "com.android.tools.lint:lint-tests:$androidLintVersion"
    testImplementation "com.android.tools:testutils:$androidLintVersion"
}
```

You can have `androidLintVersion` declared in main module like:
```groovy
buildscript {
    ext.androidLintVersion = '26.5.2'
}
```

Custom lint rules are registered through the `jar` task above that points to the custom lint rules registry using its fully qualified name: `dev.jorgecastillo.lintchecks.CustomIssueRegistry`.

That's an actual file we've got in the module, where all the custom rules are registered, like:

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

As you can see, custom lint rules are registered through their issues. In other words, you register a list of custom issues that are linked to their corresponding custom lint rules from inside.

This is an example of one of those issues that we always declare in the `companion object` of their corresponding detector class (custom rule).

```kotlin
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
```

The arguments are:
* `id`: That's the id for the check, the one you can use when ignoring occurrences in code (or XML).
* `briefDescription`: Self explanatory, you'll see this in the IDEA inspections popup.
* `explanation`: Same thing, more detailed description.
* `category`: This one states a category for the issue. Categories can be ignored all together.
* `priority`: You can set a priority value for the issue. Priority can be used to filter warnings or errors, ordering those etc.
* `severity`: This one decides whether it's gonna be considered a warning or a failure, among other choices.
* `implementation`: Here is where you link the required detector and decide what file scope you're interested in.

Finally, you just need to depend on this `lintchecks` kotlin module from the modules you want those checks to be installed and passed on, like the `app` one in the case of this sample.

```groovy
dependencies {
    lintChecks project(":lintchecks")
}
```

And that's pretty much it, you're good to go 🎉

## How to write custom rules

For samples on how to write custom Lint rules, look at [the ones provided in this repo](https://github.com/JorgeCastilloPrz/AndroidLintDocs/tree/master/lintchecks/src/main/java/dev/jorgecastillo/lintchecks).

For more diverse samples you can also take a look at any classes extending `Detector`, there are tons of them that come built into Lint that you can take a look at in your IDE. Those should be available right away. You can also take a look at them [here](https://android.googlesource.com/platform/tools/base/+/master/lint/libs/lint-checks/src/main/java/com/android/tools/lint/checks).

### TDD for writing the rules

What has worked **considerably better** for me is to write a rule "skeleton" that does literally nothing when visiting the elements, then write a basic test for it checking for the most basic behavior you'd require from it, then complete the rule implementation gradually adding more tests to cover more cases. This works great given it's quite tedious to test the rules and reproduce the failing scenarios otherwise since it requires running gradle tasks, and the IDE does not always react very fluently.

For samples on how to write those tests, take a look at [the ones provided in this repo](https://github.com/JorgeCastilloPrz/AndroidLintDocs/tree/master/lintchecks/src/test/java/dev/jorgecastillo/lintchecks).

#### ⚠️ Include stubs for the third party dependencies ⚠️

If you are testing calls to a method **in a third party library** (i.e: `material`), tests are not able to resolve sources for those. You'll need to include a stub version of the required class (including a stub version of the called methods) in the test sources for the required scenarios, [the same way we did in this example](https://github.com/JorgeCastilloPrz/AndroidLintDocs/blob/master/lintchecks/src/test/java/dev/jorgecastillo/lintchecks/TextInputLayoutSetErrorDetectorTest.kt) for calling `TextInputLayout#setError()`, since that's the method calls we're checking with the Lint rule.

### Cookbook

* Extend both `Detector(), UastScanner` to scan **Java and Kotlin files** ([example](https://github.com/JorgeCastilloPrz/AndroidLintDocs/blob/c02f23e618fcf5475c13799ef1473ef1984bd54f/lintchecks/src/main/java/dev/jorgecastillo/lintchecks/TextInputLayoutSetErrorDetector.kt#L30)).
* Extend `ResourceXmlDetector` for scanning XML resources. You can override `appliesTo` method to decide which resource types you're interested in. E.g:

```kotlin
// Just interested on Layouts.
@Override
public boolean appliesTo(@NonNull ResourceFolderType folderType) {
    return folderType == ResourceFolderType.LAYOUT;
}
```

* Extend `BinaryResourceScanner` for scanning resource files (not the contents). I.e: you check for prefixes in the name of a bunch of different types of resources. You can constrain the resource types you're interested in. E.g:

```kotlin
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
```

[Here you have an example on this repo](https://github.com/JorgeCastilloPrz/AndroidLintDocs/blob/c02f23e618fcf5475c13799ef1473ef1984bd54f/lintchecks/src/main/java/dev/jorgecastillo/lintchecks/XMLDirectColorReferencesDetector.kt#L25) that mixes both `ResourceXmlDetector` and `BinaryResourceScanner` to check for prefixes in both file names and resource Ids within the files.

## Writing a library that provides Lint checks

If you're writing a library where an artifact requires to export lint checks so client projects can get those applied, you must use [lintPublish](https://developer.android.com/studio/build/dependencies#dependency_configurations) so the rules are published into the AAR. That's **required since AGP 3.5.0**.