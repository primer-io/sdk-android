-dontwarn java.lang.invoke.StringConcatFactory
-dontusemixedcaseclassnames
-repackageclasses 'io.primer.ipay88.internal'
-allowaccessmodification
-renamesourcefileattribute SourceFile
-keepattributes Signature,Exceptions,*Annotation*,
                InnerClasses,PermittedSubclasses,EnclosingMethod,
                Deprecated,SourceFile,LineNumberTable

-keep public class **.BuildConfig { *; }
-keep class com.ipay.IPayIH { *; }

-keep class io.primer.ipay88.api.** {
    public *;
}
-keep class io.primer.ipay88.api.ui.NativeIPay88Activity {
    public protected static <methods>;
    *** Companion;
}
-keep class io.primer.ipay88.api.ui.NativeIPay88Activity$Companion {
    *;
}

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
