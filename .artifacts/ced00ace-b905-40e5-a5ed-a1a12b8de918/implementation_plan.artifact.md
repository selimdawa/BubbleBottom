# Implementation Plan - Idiomatic Kotlin Refactoring and Test Cleanup

This plan focuses on making the codebase more idiomatic Kotlin ("functional/idiomatic functions") and removing unnecessary test boilerplate as requested by the user.

## Proposed Changes

### [Component] Project Dependencies

#### [MODIFY] [app/build.gradle.kts](file:///D:/MyProjects/Library/Bubble Bottom/Bubble Bottom/app/build.gradle.kts)
#### [MODIFY] [bubblebottom/build.gradle.kts](file:///D:/MyProjects/Library/Bubble Bottom/Bubble Bottom/bubblebottom/build.gradle.kts)
- Remove all test-related dependencies (`testImplementation`, `androidTestImplementation`) since the test directories are empty and the user asked if they are necessary.

### [Component] BubbleBottom Library Module

#### [MODIFY] [BubbleBottomNavigation.kt](file:///D:/MyProjects/Library/Bubble Bottom/Bubble Bottom/bubblebottom/src/main/java/io/selimdawa/bubblebottom/BubbleBottomNavigation.kt)
- Use `@JvmOverloads` on the main constructor to remove multiple constructor overloads.
- Use idiomatic Kotlin features like `apply`, `with`, and more concise function expressions.
- Simplify `init` and `setAttributeFromXml` logic.

#### [MODIFY] [BubbleBottomNavigationCell.kt](file:///D:/MyProjects/Library/Bubble Bottom/Bubble Bottom/bubblebottom/src/main/java/io/selimdawa/bubblebottom/BubbleBottomNavigationCell.kt)
- Further idiomatic improvements if any are missed.

#### [MODIFY] [Utils.kt](file:///D:/MyProjects/Library/Bubble Bottom/Bubble Bottom/bubblebottom/src/main/java/io/selimdawa/bubblebottom/Utils.kt)
- Optimize extension functions.

### [Component] App Module

#### [MODIFY] [MainActivity.kt](file:///D:/MyProjects/Library/Bubble Bottom/Bubble Bottom/app/src/main/java/com/flatcode/bubblebottom/MainActivity.kt)
- Use more concise Kotlin syntax for UI setup.

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to ensure the project still builds correctly.

### Manual Verification
- Verify the library still functions correctly with the simplified constructors.
- Ensure the removal of test dependencies doesn't break the build (it shouldn't since there are no tests).
