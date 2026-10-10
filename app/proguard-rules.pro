# ProGuard / R8 Rules for Bubble Bottom App

# Keep BubbleBottom library classes
-keep class io.selimdawa.bubblebottom.** { *; }

# Preserve annotations & source attributes for stack traces
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keepattributes SourceFile, LineNumberTable
