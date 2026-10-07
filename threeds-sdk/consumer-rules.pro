


-keeppackagenames com.netcetera.threeds.sdk.api.info,com.netcetera.threeds.sdk.api,com.netcetera.threeds.sdk.api.configparameters,com.netcetera.threeds.sdk.api.ui.logic,com.netcetera.threeds.sdk.infrastructure,com.netcetera.threeds.sdk.api.security,com.netcetera.threeds.sdk.api.transaction.challenge,com.netcetera.threeds.sdk.api.exceptions,org.bouncycastle.jce.provider,com.netcetera.threeds.sdk.api.transaction,com.netcetera.threeds.sdk.api.ui,com.netcetera.threeds.sdk.api.transaction.challenge.events
-adaptresourcefilecontents !jni/arm64-v8a/libac2f.so,!jni/armeabi-v7a/libac2f.so,!jni/x86/libac2f.so,!jni/x86_64/libac2f.so,!lib/arm64-v8a/libac2f.so,!lib/armeabi-v7a/libac2f.so,!lib/x86/libac2f.so,!lib/x86_64/libac2f.so,dummyfile
-dontwarn com.netcetera.threeds.sdk.**,org.bouncycastle.**,kotlin.KotlinVersion,proguard.annotation.**




# Keep everyting in the Netcetera Android 3DS SDK package
-keep public class com.netcetera.threeds.sdk.** {
    public protected <fields>;    public protected <methods>;
}

-keep,allowshrinking class com.netcetera.threeds.sdk.** {
    <fields>;    <methods>;
}

# Keep everyting in Guardsquare Dexguard
-keep public class com.guardsquare.dexguard.** {
    public protected <fields>;    public protected <methods>;
}

# Keep everything from bouncycastle
-keep class org.bouncycastle.** {
    <fields>;    <methods>;
}

-keep,allowshrinking class org.bouncycastle.** {
    <fields>;    <methods>;
}

# Keep the classes from slf4j
-keep class org.slf4j.** {
    <fields>;    <methods>;
}

# Keep kotlin.KotlinVersion if present
-keep class kotlin.KotlinVersion {
    <fields>;    <methods>;
}

-keep class com.netcetera.threeds.sdk.infrastructure.get {
    int e1;    int get;    int ThreeDS2ServiceInstance;    int initialize;    long[] ThreeDS2Service;    long[] createTransaction;    short getWarnings;    byte[] getSDKInfo;    int cleanup;    int getSDKVersion;    int addParam;    <init>(java.io.InputStream,int,int,short,int,int);
    <init>(java.io.InputStream,int,int,short,int,int,int,int);
    int read();
    int read(byte[],int,int);
    long skip(long);
    int available();
    boolean markSupported();
    void ThreeDS2Service();
    int ThreeDS2ServiceInstance();
    void get();
}

-keep class com.netcetera.threeds.sdk.infrastructure.ThreeDS2ServiceInstance {
    short ThreeDS2Service;    byte[] ThreeDS2ServiceInstance;    byte[] get;    byte[] initialize;    int e1;    int getSDKVersion;    int getWarnings;    int getSDKInfo;    int createTransaction;    int cleanup;    int addParam;    int onCompleted;    int ThreeDS2ServiceInitializationCallback;    int ConfigParameters;    int onError;    int getParamValue;    <init>(java.io.InputStream,int[],int,byte[],int,int);
    <init>(java.io.InputStream,int[],int,byte[],int,int,int,int);
    int read();
    int read(byte[],int,int);
    long skip(long);
    int available();
    boolean markSupported();
    void ThreeDS2Service(long,int);
    void get(long);
    void get();
    void ThreeDS2Service();
    int e1();
    void initialize();
    void <clinit>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.getSDKInfo {
    int ThreeDS2ServiceInstance;    int initialize;    <init>();
    char[] ThreeDS2Service(long,char[],int);
}

-keep class com.netcetera.threeds.sdk.infrastructure.getSDKVersion {
    int initialize;    int ThreeDS2Service;    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.cleanup {
    int e1;    char get;    char ThreeDS2ServiceInstance;    int initialize;    int ThreeDS2Service;    int getWarnings;    int createTransaction;    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.createTransaction {
    int ThreeDS2Service;    int e1;    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.getWarnings {
    int e1;    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.ThreeDS2ServiceInitializationCallback {
    int initialize;    int get;    char ThreeDS2Service;    char ThreeDS2ServiceInstance;    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.onCompleted {
    int initialize;    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.ConfigParameters {
    int ThreeDS2Service;    int initialize;    int ThreeDS2ServiceInstance;    <init>();
    void initialize(int[]);
    int get(int);
}

-keep class com.netcetera.threeds.sdk.infrastructure.addParam {
    int ThreeDS2Service;    int get;    int initialize;    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.onError {
    int ThreeDS2Service;    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.ConfigurationBuilder {
    int initialize;    char ThreeDS2ServiceInstance;    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.restrictedParameters {
    java.lang.Object initialize(int);
    java.lang.Object ThreeDS2ServiceInstance(int,int,char,int,boolean,java.lang.String,java.lang.Class[]);
    int ThreeDS2ServiceInstance(java.lang.Object);
    int e1(int);
}

-keep class com.netcetera.threeds.sdk.infrastructure.SDKNotInitializedException {
    java.lang.Integer ThreeDS2Service;}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.SDKNotInitializedException {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.ThreeDS2ServiceInstance {
    <init>();
}

-keep class com.netcetera.threeds.sdk.api.ThreeDS2Service {
    void initialize(android.content.Context,com.netcetera.threeds.sdk.api.configparameters.ConfigParameters,java.lang.String,java.util.Map);
    void initialize(android.content.Context,com.netcetera.threeds.sdk.api.configparameters.ConfigParameters,java.lang.String,java.util.Map,com.netcetera.threeds.sdk.api.ThreeDS2Service$InitializationCallback);
    java.util.List getWarnings();
    java.lang.String getSDKVersion();
    com.netcetera.threeds.sdk.api.info.SDKInfo getSDKInfo();
    void cleanup(android.content.Context);
    com.netcetera.threeds.sdk.api.transaction.Transaction createTransaction(java.lang.String,java.lang.String);
    com.netcetera.threeds.sdk.api.transaction.Transaction createTransaction(java.lang.String,java.lang.String,java.util.Map);
}

-keep class com.netcetera.threeds.sdk.api.ThreeDS2Service$InitializationCallback {
    void onCompleted();
    void onError(java.lang.Throwable);
}

-keep class com.netcetera.threeds.sdk.api.configparameters.ConfigParameters {
    java.lang.String getParamValue(java.lang.String,java.lang.String);
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.api.configparameters.builder.ConfigurationBuilder {
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.api.configparameters.builder.SchemeConfiguration {
    void ThreeDS2ServiceInstance();
    void init$0();
}

-keep class com.netcetera.threeds.sdk.api.exceptions.InvalidInputException

-keep class com.netcetera.threeds.sdk.api.exceptions.SDKAlreadyInitializedException

-keep class com.netcetera.threeds.sdk.api.exceptions.SDKNotInitializedException

-keep class com.netcetera.threeds.sdk.api.exceptions.SDKRuntimeException

-keep class com.netcetera.threeds.sdk.api.info.CertificateInfo

-keep class com.netcetera.threeds.sdk.api.info.CertificateInfo$CertificateType {
    com.netcetera.threeds.sdk.api.info.CertificateInfo$CertificateType CERTIFICATE;    com.netcetera.threeds.sdk.api.info.CertificateInfo$CertificateType PUBLIC_KEY;}

-keep class com.netcetera.threeds.sdk.api.info.SDKInfo

-keep class com.netcetera.threeds.sdk.api.security.Severity {
    com.netcetera.threeds.sdk.api.security.Severity MEDIUM;    com.netcetera.threeds.sdk.api.security.Severity HIGH;}

-keep class com.netcetera.threeds.sdk.api.security.Warning {
    <init>(java.lang.String,java.lang.String,com.netcetera.threeds.sdk.api.security.Severity);
    java.lang.String getId();
}

-keep class com.netcetera.threeds.sdk.api.transaction.AuthenticationRequestParameters {
    <init>(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String);
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.api.transaction.AuthenticationRequestParameters {
    void init$0();
}

-keep class com.netcetera.threeds.sdk.api.transaction.Transaction {
    com.netcetera.threeds.sdk.api.transaction.AuthenticationRequestParameters getAuthenticationRequestParameters();
    void doChallenge(android.app.Activity,com.netcetera.threeds.sdk.api.transaction.challenge.ChallengeParameters,com.netcetera.threeds.sdk.api.transaction.challenge.ChallengeStatusReceiver,int);
    void useBridgingExtension(com.netcetera.threeds.sdk.api.transaction.Transaction$BridgingMessageExtensionVersion);
    com.netcetera.threeds.sdk.api.ui.ProgressView getProgressView(android.app.Activity);
    void close();
}

-keep class com.netcetera.threeds.sdk.api.transaction.Transaction$BridgingMessageExtensionVersion

-keep class com.netcetera.threeds.sdk.api.transaction.challenge.ChallengeParameters {
    java.lang.String get3DSServerTransactionID();
    java.lang.String getAcsTransactionID();
    java.lang.String getAcsRefNumber();
    java.lang.String getAcsSignedContent();
}

-keep class com.netcetera.threeds.sdk.api.transaction.challenge.ChallengeStatusReceiver {
    void completed(com.netcetera.threeds.sdk.api.transaction.challenge.events.CompletionEvent);
    void cancelled();
    void timedout();
    void protocolError(com.netcetera.threeds.sdk.api.transaction.challenge.events.ProtocolErrorEvent);
    void runtimeError(com.netcetera.threeds.sdk.api.transaction.challenge.events.RuntimeErrorEvent);
}

-keep class com.netcetera.threeds.sdk.api.transaction.challenge.ErrorMessage {
    java.lang.String getErrorCode();
    java.lang.String getErrorDescription();
    java.lang.String getErrorDetails();
    java.lang.String getErrorComponent();
}

-keep class com.netcetera.threeds.sdk.api.transaction.challenge.events.CompletionEvent {
    java.lang.String getTransactionStatus();
}

-keep class com.netcetera.threeds.sdk.api.transaction.challenge.events.ProtocolErrorEvent {
    com.netcetera.threeds.sdk.api.transaction.challenge.ErrorMessage getErrorMessage();
}

-keep class com.netcetera.threeds.sdk.api.transaction.challenge.events.RuntimeErrorEvent {
    java.lang.String getErrorCode();
    java.lang.String getErrorMessage();
}

-keep class com.netcetera.threeds.sdk.api.ui.ProgressView

-keep class com.netcetera.threeds.sdk.api.ui.logic.UiCustomization

-keep class com.netcetera.threeds.sdk.api.ui.logic.UiCustomization$UiCustomizationType

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.api.utils.DsRidValues {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.rootPublicKey {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.SDKAlreadyInitializedException {
    <init>(com.netcetera.threeds.sdk.infrastructure.SDKAlreadyInitializedException$e1);
}

-keep class com.netcetera.threeds.sdk.infrastructure.SDKAlreadyInitializedException$e1 {
    com.netcetera.threeds.sdk.infrastructure.SDKAlreadyInitializedException$e1 e1(android.content.Context);
}

-keep class com.netcetera.threeds.sdk.infrastructure.getSDKEphemeralPublicKey {
    void ThreeDS2ServiceInstance(com.netcetera.threeds.sdk.infrastructure.doChallenge);
    void ThreeDS2Service(java.lang.Class);
    java.lang.Object e1(java.lang.Class);
}

-keep class com.netcetera.threeds.sdk.infrastructure.doChallenge

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.getValue {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.getAcsSignedContent {
    java.security.PublicKey e1(java.lang.String);
    com.netcetera.threeds.sdk.api.info.CertificateInfo$CertificateType initialize(java.lang.String);
}

-keep class com.netcetera.threeds.sdk.infrastructure.protocolError {
    <init>(com.netcetera.threeds.sdk.infrastructure.CompletionEvent,java.util.List,com.netcetera.threeds.sdk.infrastructure.setVelocityScale$ThreeDS2ServiceInstance);
}

-keep class com.netcetera.threeds.sdk.infrastructure.runtimeError {
    java.lang.String initialize(java.lang.String);
}

-keep class com.netcetera.threeds.sdk.infrastructure.getErrorDescription {
    com.netcetera.threeds.sdk.infrastructure.runtimeError get(java.lang.String,java.lang.String,java.lang.String,com.netcetera.threeds.sdk.infrastructure.setVelocityScale$get);
}

-keep class com.netcetera.threeds.sdk.infrastructure.getErrorDetails {
    java.lang.String ThreeDS2ServiceInstance(com.netcetera.threeds.sdk.infrastructure.protocolError);
}

-keep class com.netcetera.threeds.sdk.infrastructure.CompletionEvent

-keep class com.netcetera.threeds.sdk.infrastructure.getMessageVersionNumber {
    void e1(com.netcetera.threeds.sdk.api.configparameters.ConfigParameters,java.util.Locale);
    void ThreeDS2ServiceInstance(com.netcetera.threeds.sdk.infrastructure.setVelocityScale$get,com.netcetera.threeds.sdk.infrastructure.setVelocityScale$ThreeDS2ServiceInstance,java.lang.String,java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.CompletionEvent e1();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.getErrorComponent {
    void cleanup();
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.getErrorComponent$initialize {
    void ThreeDS2Service();
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.getErrorMessage {
    void ThreeDS2ServiceInstance();
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.getTransactionStatus {
    void init$0();
}

-keep class com.netcetera.threeds.sdk.infrastructure.isProgressShown {
    int ThreeDS2ServiceInstance();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setBackgroundColor {
    int ThreeDS2Service();
}

-keep class com.netcetera.threeds.sdk.infrastructure.LabelCustomization {
    int ThreeDS2ServiceInstance();
}

-keep class com.netcetera.threeds.sdk.infrastructure.hasAlgorithm {
    void get(long,long);
}

-keep class com.netcetera.threeds.sdk.infrastructure.getService {
    java.lang.Object[] e1(android.content.Context,int,int,int);
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.getService {
    void init$0();
    void init$1();
}

-keep class com.netcetera.threeds.sdk.infrastructure.addAlgorithm {
    void ThreeDS2Service(long,long);
}

-keep class com.netcetera.threeds.sdk.infrastructure.getKeyInfoConverter {
    java.lang.Object[] e1(android.content.Context,int,int,int);
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.getKeyInfoConverter {
    void init$0();
    void init$1();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setDrawingCacheEnabled {
    void ThreeDS2ServiceInstance(long,long);
}

-keep class com.netcetera.threeds.sdk.infrastructure.setFrameContentVelocity {
    java.lang.Object[] ThreeDS2ServiceInstance(android.content.Context,int,int);
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setFrameContentVelocity {
    void init$0();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setHandwritingDelegatorCallback {
    int get();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setLabelFor {
    int initialize();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setRotationX {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setRotationX {
    void e1();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setScrollCaptureHint {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setScrollCaptureHint {
    void ThreeDS2ServiceInstance();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setScrollContainer {
    com.netcetera.threeds.sdk.infrastructure.setScrollbarFadingEnabled getSDKVersion();
    java.lang.Object ThreeDS2ServiceInstance(int,java.lang.Object[],int,int,int,int,int);
}

-keep class com.netcetera.threeds.sdk.infrastructure.setScrollbarFadingEnabled {
    com.netcetera.threeds.sdk.infrastructure.setScrollbarFadingEnabled$e1 ThreeDS2Service();
    java.lang.Object get(int,int,int,int,int,int,java.lang.Object[]);
}

-keep class com.netcetera.threeds.sdk.infrastructure.setScrollbarFadingEnabled$e1 {
    java.lang.String initialize();
    java.lang.String ThreeDS2Service();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setScrollX {
    java.lang.Object ThreeDS2Service(int,int,int,int,int,java.lang.Object[],int);
}

-keep class com.netcetera.threeds.sdk.infrastructure.setScrollY {
    java.lang.Object get(java.lang.Object[],int,int,int,int,int,int);
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setScrollY$ThreeDS2ServiceInstance {
    void e1();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setSoundEffectsEnabled {
    java.lang.Object ThreeDS2Service(int,int,java.lang.Object[],int,int,int,int);
}

-keep class com.netcetera.threeds.sdk.infrastructure.setStateListAnimator {
    java.lang.Object get(int,int,java.lang.Object[],int,int,int,int);
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setSupplementalDescription {
    void e1();
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setTransitionAlpha {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setTouchDelegate {
    void get();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setTranslationZ {
    void initialize(long,long);
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setTranslationZ {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setTranslationX {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setTranslationX {
    void init$0();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setVerticalFadingEdgeEnabled {
    com.netcetera.threeds.sdk.infrastructure.setScrollContainer get(java.lang.String);
    java.util.List ThreeDS2ServiceInstance();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setTranslationY {
    com.netcetera.threeds.sdk.infrastructure.setVerticalFadingEdgeEnabled e1(com.netcetera.threeds.sdk.api.configparameters.ConfigParameters);
}

-keep class com.netcetera.threeds.sdk.infrastructure.setX {
    com.netcetera.threeds.sdk.infrastructure.getAcsSignedContent ThreeDS2Service();
    java.lang.Object[] ThreeDS2ServiceInstance(android.content.Context,int,int);
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setX {
    void init$0();
    void init$1();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled {
    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled cleanup;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled getSDKInfo;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled onCompleted;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled addParam;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled ThreeDS2ServiceInitializationCallback;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled onError;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled ConfigParameters;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled mastercardSchemeConfiguration;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled visaSchemeConfiguration;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled build;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled amexConfiguration;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled jcbConfiguration;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled dinersSchemeConfiguration;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled getSchemeId;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled getSchemeLogo;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled newSchemeConfiguration;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled getSchemeName;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled SchemeConfigurationBuilder;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled getSchemeEncryptionPublicKey;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled getSchemeEncryptionPublicKeyId;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled logo;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled ids;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled encryptionPublicKeyFromAssetCertificate;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled logoDark;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled encryptionPublicKey;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled SDKAlreadyInitializedException;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled SDKNotInitializedException;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled InvalidInputException;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled rootPublicKey;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled getErrorCode;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled getType;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled getCertPrefix;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled getName;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled getEncryptionCertificateKid;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled getRootCertificates;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled getIds;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled Severity;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled getId;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled getSDKAppID;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled doChallenge;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled TransactionBridgingMessageExtensionVersion;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled close;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled ChallengeParameters;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled set3DSServerTransactionID;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled setAcsSignedContent;    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled setAcsTransactionID;    com.netcetera.threeds.sdk.infrastructure.setY initialize(java.lang.Object[]);
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled {
    void ThreeDS2ServiceInstance();
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setAnimationCacheEnabled {
    void ThreeDS2ServiceInstance();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setAddStatesFromChildren {
    void e1(com.netcetera.threeds.sdk.infrastructure.setAddStatesFromChildren$get);
    void initialize(com.netcetera.threeds.sdk.infrastructure.setAddStatesFromChildren$get,java.lang.Runnable);
    java.lang.Object ThreeDS2Service(com.netcetera.threeds.sdk.infrastructure.setAddStatesFromChildren$e1);
}

-keep class com.netcetera.threeds.sdk.infrastructure.setAddStatesFromChildren$e1 {
    java.lang.Object e1();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setAddStatesFromChildren$get {
    void ThreeDS2Service();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setY {
    com.netcetera.threeds.sdk.infrastructure.setAlwaysDrawnWithCacheEnabled e1();
    void ThreeDS2ServiceInstance(long,long);
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setClipChildren {
    void init$0();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setDescendantFocusability {
    int ThreeDS2ServiceInstance;    <init>(com.netcetera.threeds.sdk.api.transaction.challenge.ChallengeStatusReceiver);
    void initialize(com.netcetera.threeds.sdk.api.transaction.challenge.events.CompletionEvent);
    void e1();
    void get();
    void ThreeDS2ServiceInstance(com.netcetera.threeds.sdk.api.transaction.challenge.events.ProtocolErrorEvent);
    void e1(com.netcetera.threeds.sdk.api.transaction.challenge.events.RuntimeErrorEvent);
    void <clinit>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setPersistentDrawingCache {
    com.netcetera.threeds.sdk.infrastructure.setAddStatesFromChildren ThreeDS2ServiceInstance(com.netcetera.threeds.sdk.infrastructure.setSelectAllOnFocus);
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setPersistentDrawingCache {
    void get();
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setDataDirectorySuffix {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setTransitionGroup {
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setCertificate {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setFindListener {
    com.netcetera.threeds.sdk.infrastructure.setCompoundDrawableTintBlendMode get(com.netcetera.threeds.sdk.infrastructure.setScrollbarFadingEnabled$e1);
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setFindListener {
    void init$0();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setHorizontalScrollbarOverlay {
    void get(java.util.Date);
    com.netcetera.threeds.sdk.api.info.SDKInfo ThreeDS2ServiceInstance(java.util.List);
    void ThreeDS2Service();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setHorizontalScrollbarOverlay {
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setInitialScale {
    void init$0();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setNetworkAvailable {
    java.util.Locale ThreeDS2Service(java.lang.String);
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setRendererPriorityPolicy {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setRendererPriorityPolicy {
    void initialize();
    void init$0();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setPictureListener {
    void initialize(com.netcetera.threeds.sdk.infrastructure.setTextSize,com.netcetera.threeds.sdk.infrastructure.setTextClassifier);
    void e1(com.netcetera.threeds.sdk.infrastructure.setSwitchPadding,com.netcetera.threeds.sdk.infrastructure.setWebContentsDebuggingEnabled);
}

-keep class com.netcetera.threeds.sdk.infrastructure.setTextClassifier {
    void e1(com.netcetera.threeds.sdk.infrastructure.setTitleTextColor);
    void e1(com.netcetera.threeds.sdk.infrastructure.setThumbTextPadding);
    void e1(com.netcetera.threeds.sdk.infrastructure.setSwitchPadding);
    void ThreeDS2ServiceInstance(com.netcetera.threeds.sdk.infrastructure.setY);
    void ThreeDS2Service(com.netcetera.threeds.sdk.infrastructure.setY);
    void e1(com.netcetera.threeds.sdk.infrastructure.setY);
}

-keep class com.netcetera.threeds.sdk.infrastructure.setWebContentsDebuggingEnabled {
    void initialize();
    void ThreeDS2Service(com.netcetera.threeds.sdk.infrastructure.setY);
}

-keep class com.netcetera.threeds.sdk.infrastructure.setVerticalScrollbarOverlay {
    java.lang.String e1();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setWebChromeClient {
    com.netcetera.threeds.sdk.infrastructure.setDrawSelectorOnTop e1();
    com.netcetera.threeds.sdk.infrastructure.setWebChromeClient ThreeDS2Service();
    com.netcetera.threeds.sdk.infrastructure.setWebViewRenderProcessClient initialize();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setWebViewRenderProcessClient

-keep class com.netcetera.threeds.sdk.infrastructure.setDrawSelectorOnTop {
    com.netcetera.threeds.sdk.infrastructure.setVerticalScrollbarOverlay ThreeDS2ServiceInstance();
    boolean initialize();
    int get();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setBottomEdgeEffectColor {
    com.netcetera.threeds.sdk.infrastructure.setWebChromeClient ThreeDS2ServiceInstance(java.lang.String,java.lang.String,java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.setWebChromeClient initialize(java.lang.String,java.lang.String,java.lang.String,java.lang.String);
}

-keep class com.netcetera.threeds.sdk.infrastructure.setAdapter {
    com.netcetera.threeds.sdk.infrastructure.setBottomEdgeEffectColor initialize();
    com.netcetera.threeds.sdk.infrastructure.setBottomEdgeEffectColor ThreeDS2ServiceInstance(java.lang.String);
}

-keep class com.netcetera.threeds.sdk.infrastructure.setEdgeEffectColor {
    <init>(javax.net.ssl.SSLSocketFactory,java.lang.String);
}

-keep class com.netcetera.threeds.sdk.infrastructure.setFastScrollStyle {
    java.lang.Object e1(int,java.lang.Object[],int,int,int,int,int);
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setFastScrollStyle {
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setFastScrollAlwaysVisible {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setFastScrollEnabled$ThreeDS2ServiceInstance {
    int get();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setRecyclerListener {
    void get(long,long);
}

-keep class com.netcetera.threeds.sdk.infrastructure.setFriction {
    java.lang.Integer ThreeDS2Service;    java.lang.Integer e1;    java.lang.Integer get;    java.lang.Integer initialize;    android.util.Range getWarnings;}

-keep class com.netcetera.threeds.sdk.infrastructure.setMultiChoiceModeListener {
    <init>();
    boolean ThreeDS2Service(com.netcetera.threeds.sdk.infrastructure.setY);
    java.lang.String ThreeDS2Service(com.netcetera.threeds.sdk.infrastructure.setDrawSelectorOnTop);
    boolean e1(com.netcetera.threeds.sdk.infrastructure.setY);
}

-keep class com.netcetera.threeds.sdk.infrastructure.setRemoteViewsAdapter {
    com.netcetera.threeds.sdk.infrastructure.setPictureListener ThreeDS2ServiceInstance(com.netcetera.threeds.sdk.infrastructure.setPictureListener);
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setRemoteViewsAdapter {
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setOnItemClickListener {
    void ThreeDS2Service();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setVelocityScale {
    com.netcetera.threeds.sdk.infrastructure.setVelocityScale$get ThreeDS2Service();
    com.netcetera.threeds.sdk.infrastructure.setVelocityScale$get initialize(java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.setVelocityScale$ThreeDS2ServiceInstance initialize();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setVelocityScale$ThreeDS2ServiceInstance

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setVelocityScale$ThreeDS2ServiceInstance {
    void ThreeDS2Service();
    void init$0();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setVelocityScale$get {
    com.netcetera.threeds.sdk.infrastructure.setVelocityScale$get initialize;    com.netcetera.threeds.sdk.infrastructure.setVelocityScale$get get;    java.lang.String get();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setVelocityScale$get {
    void e1();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setButtonIcon {
    java.lang.String e1();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setSelection {
    java.lang.String initialize();
    java.lang.String e1(com.netcetera.threeds.sdk.infrastructure.setVelocityScale$get);
    java.lang.String get();
    java.lang.String ThreeDS2Service();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setButtonTintBlendMode {
    <init>();
    java.lang.String ThreeDS2Service();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setOrientationO1223$2 {
    int[] ThreeDS2Service;}

-keep class com.netcetera.threeds.sdk.infrastructure.setVerticalGravity {
    com.netcetera.threeds.sdk.infrastructure.setVerticalGravity e1(java.lang.String);
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setVerticalGravity {
    void e1();
    void init$0();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setHeaderDividersEnabled {
    java.lang.String initialize();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setFooterDividersEnabled {
    void init$0();
    void init$1();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setDividerHeight {
    com.netcetera.threeds.sdk.infrastructure.setDividerHeight get(android.content.Context);
    java.lang.String e1();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setDividerHeight {
    void init$0();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setSelectionAfterHeaderView {
    void initialize(com.netcetera.threeds.sdk.infrastructure.setProgress);
    com.netcetera.threeds.sdk.infrastructure.setProgress e1();
    void initialize();
    java.lang.Long get();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setIndeterminateDrawable {
    java.lang.String initialize(com.netcetera.threeds.sdk.infrastructure.setMin);
    java.lang.String ThreeDS2ServiceInstance(com.netcetera.threeds.sdk.infrastructure.setProgress);
    com.netcetera.threeds.sdk.infrastructure.setProgress initialize(java.lang.String);
}

-keep class com.netcetera.threeds.sdk.infrastructure.setOverscrollHeader {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setOverscrollHeader {
    void init$0();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setIndeterminate {
    java.lang.String initialize();
    void initialize(com.netcetera.threeds.sdk.infrastructure.setMax);
}

-keep class com.netcetera.threeds.sdk.infrastructure.setIndeterminateTintBlendMode {
    void ThreeDS2ServiceInstance(java.lang.String);
    boolean get();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setInterpolator {
    void get();
    void init$0();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setIndeterminateTintMode

-keep class com.netcetera.threeds.sdk.infrastructure.setIndeterminateTintMode$initialize {
    <init>();
    com.netcetera.threeds.sdk.infrastructure.setIndeterminateTintMode$initialize ThreeDS2ServiceInstance(java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.setIndeterminateTintMode$initialize initialize(java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.setIndeterminateTintMode initialize();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setMin

-keep class com.netcetera.threeds.sdk.infrastructure.setMin$ThreeDS2Service {
    <init>();
    com.netcetera.threeds.sdk.infrastructure.setMin$ThreeDS2Service get(java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.setMin$ThreeDS2Service ThreeDS2Service(java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.setMin$ThreeDS2Service e1(java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.setMin$ThreeDS2Service initialize(java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.setMin$ThreeDS2Service ThreeDS2ServiceInstance(com.netcetera.threeds.sdk.infrastructure.setIndeterminateTintMode);
    com.netcetera.threeds.sdk.infrastructure.setMin$ThreeDS2Service ThreeDS2ServiceInstance(java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.setMin$ThreeDS2Service e1(java.lang.String[]);
    com.netcetera.threeds.sdk.infrastructure.setMin$ThreeDS2Service cleanup(java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.setMin initialize();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setMinWidth {
    java.lang.String initialize();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setProgress {
    com.netcetera.threeds.sdk.infrastructure.setMax ThreeDS2Service();
    com.netcetera.threeds.sdk.infrastructure.setMinWidth initialize();
    java.lang.String e1();
    java.lang.Boolean ThreeDS2ServiceInstance();
    com.netcetera.threeds.sdk.infrastructure.setProgress getSDKVersion();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setMax

-keep class com.netcetera.threeds.sdk.infrastructure.setProgressDrawable {
    void ThreeDS2Service(com.netcetera.threeds.sdk.infrastructure.setSelection,java.lang.String);
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setProgressBackgroundTintList {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setProgressBackgroundTintList {
    void init$0();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setProgressBackgroundTintBlendMode {
    int ThreeDS2ServiceInstance();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setProgressBackgroundTintBlendMode {
    void init$0();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setProgressTintBlendMode {
    java.text.DateFormat e1;}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setProgressTintBlendMode {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setProgressTintBlendMode {
    void get();
    void init$0();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setProgressTintBlendMode$e1 {
    com.netcetera.threeds.sdk.infrastructure.setProgressTintBlendMode$e1 get;    com.netcetera.threeds.sdk.infrastructure.setProgressTintBlendMode$e1 ThreeDS2Service;    com.netcetera.threeds.sdk.infrastructure.setProgressTintBlendMode$e1 initialize;    com.netcetera.threeds.sdk.infrastructure.setProgressTintBlendMode$e1 e1;    java.lang.String initialize();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setProgressTintBlendMode$e1 {
    void e1();
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setSecondaryProgress {
    void ThreeDS2Service();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setSmoothScrollingEnabled {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setSmoothScrollingEnabled {
    void init$0();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setAllCaps

-keep class com.netcetera.threeds.sdk.infrastructure.setAllCaps$get {
    <init>(java.lang.String,java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.setAllCaps$get get(java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.setAllCaps$get ThreeDS2Service(java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.setAllCaps$get e1(java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.setAllCaps$get initialize(java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.setAllCaps$get createTransaction(java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.setAllCaps$get getSDKInfo(java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.setAllCaps$get getSDKVersion(java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.setAllCaps$get getWarnings(java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.setAllCaps$get cleanup(java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.setAllCaps$get onCompleted(java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.setAllCaps$get ThreeDS2ServiceInitializationCallback(java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.setAllCaps$get onError(java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.setAllCaps$get addParam(java.lang.String);
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setAllCaps$get {
    void init$0();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setFillViewport {
    void ThreeDS2Service();
    void e1(com.netcetera.threeds.sdk.infrastructure.setAllCaps$get);
    void initialize();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setSecondaryProgressTintMode {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setAutoSizeTextTypeUniformWithPresetSizes {
    void get();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setBreakStrategy {
    void e1(java.util.List);
    java.util.List initialize();
    void ThreeDS2ServiceInstance(java.util.List);
}

-keep class com.netcetera.threeds.sdk.infrastructure.setCompoundDrawablePadding {
    java.lang.String initialize(java.util.List);
    java.lang.String ThreeDS2Service(com.netcetera.threeds.sdk.infrastructure.setCursorVisible);
    java.util.List ThreeDS2Service(java.lang.String);
}

-keep class com.netcetera.threeds.sdk.infrastructure.setCompoundDrawables {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setCompoundDrawables {
    void init$0();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setCompoundDrawableTintBlendMode

-keep class com.netcetera.threeds.sdk.infrastructure.setCompoundDrawableTintMode {
    java.lang.String e1();
    java.lang.String get();
    java.lang.String initialize();
    java.lang.String ThreeDS2Service();
    java.lang.String ThreeDS2ServiceInstance();
    java.lang.String getWarnings();
    com.netcetera.threeds.sdk.infrastructure.setCompoundDrawableTintBlendMode cleanup();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setCompoundDrawableTintMode$e1 {
    <init>();
    com.netcetera.threeds.sdk.infrastructure.setCompoundDrawableTintMode$e1 initialize(java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.setCompoundDrawableTintMode$e1 ThreeDS2Service(java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.setCompoundDrawableTintMode$e1 e1(java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.setCompoundDrawableTintMode$e1 ThreeDS2ServiceInstance(java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.setCompoundDrawableTintMode$e1 get(java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.setCompoundDrawableTintMode$e1 getWarnings(java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.setCompoundDrawableTintMode$e1 ThreeDS2Service(com.netcetera.threeds.sdk.infrastructure.setCompoundDrawableTintBlendMode);
    com.netcetera.threeds.sdk.infrastructure.setCompoundDrawableTintMode ThreeDS2Service();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setCompoundDrawablesWithIntrinsicBounds

-keep class com.netcetera.threeds.sdk.infrastructure.setCompoundDrawablesWithIntrinsicBounds$ThreeDS2Service {
    <init>();
    com.netcetera.threeds.sdk.infrastructure.setCompoundDrawablesWithIntrinsicBounds$ThreeDS2Service e1(java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.setCompoundDrawablesWithIntrinsicBounds$ThreeDS2Service get(java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.setCompoundDrawablesWithIntrinsicBounds$ThreeDS2Service ThreeDS2Service(java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.setCompoundDrawablesWithIntrinsicBounds$ThreeDS2Service initialize(java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.setCompoundDrawablesWithIntrinsicBounds$ThreeDS2Service ThreeDS2ServiceInstance(java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.setCompoundDrawablesWithIntrinsicBounds$ThreeDS2Service cleanup(java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.setCompoundDrawablesWithIntrinsicBounds$ThreeDS2Service ThreeDS2ServiceInstance(java.util.Map);
    com.netcetera.threeds.sdk.infrastructure.setCompoundDrawablesWithIntrinsicBounds$ThreeDS2Service e1(com.netcetera.threeds.sdk.infrastructure.setCompoundDrawableTintBlendMode);
    com.netcetera.threeds.sdk.infrastructure.setCompoundDrawablesWithIntrinsicBounds ThreeDS2Service();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setCursorVisible {
    java.util.List ThreeDS2ServiceInstance();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setCursorVisible$e1 {
    <init>(java.lang.String,java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.setCursorVisible$e1 e1(java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.setCursorVisible$e1 e1(com.netcetera.threeds.sdk.infrastructure.setCompoundDrawablesWithIntrinsicBounds);
    com.netcetera.threeds.sdk.infrastructure.setCursorVisible$e1 get(java.util.List);
    com.netcetera.threeds.sdk.infrastructure.setCursorVisible get();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setCustomInsertionActionModeCallback {
    void e1(com.netcetera.threeds.sdk.infrastructure.setSelection,java.lang.String);
    void ThreeDS2ServiceInstance(com.netcetera.threeds.sdk.infrastructure.setCompoundDrawableTintMode);
    void get();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setCompoundDrawablesRelative {
    int ThreeDS2Service();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setCompoundDrawablesRelative {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setCompoundDrawablesRelative {
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setCompoundDrawablesRelativeWithIntrinsicBounds {
    void init$0();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setError {
    int e1();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setEditableFactory

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setEditableFactory {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setFocusedSearchResultIndex {
    int ThreeDS2Service(java.lang.Object);
    int e1(int);
}

-keep class com.netcetera.threeds.sdk.infrastructure.setLineBreakWordStyle {
    java.lang.Object[] get$3ad024ac(int,int,java.lang.Object,int,boolean);
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setLineBreakWordStyle {
    void init$0();
    void init$1();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setLocalePreferredLineHeightForMinimumUsed {
    void init$0();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setLinksClickable {
    com.netcetera.threeds.sdk.api.ThreeDS2Service e1();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setLinksClickable {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setMinimumFontMetrics {
    com.netcetera.threeds.sdk.api.transaction.AuthenticationRequestParameters ThreeDS2ServiceInstance(com.netcetera.threeds.sdk.infrastructure.protocolError,java.lang.String,java.security.KeyPair,com.netcetera.threeds.sdk.infrastructure.setVelocityScale$get);
}

-keep class com.netcetera.threeds.sdk.infrastructure.setRawInputType {
    void get();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setScroller {
    java.lang.String get(java.security.KeyPair);
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setPrivateImeOptions {
    void ThreeDS2Service();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setPaintFlags {
    void get();
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setSearchResultHighlights {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setShiftDrawingOffsetForStartOverhang {
    void ThreeDS2Service();
    void ThreeDS2ServiceInstance();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setSearchResultHighlightColor {
    void ThreeDS2Service();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setShadowLayer {
    java.lang.String ThreeDS2ServiceInstance();
    java.lang.String get();
    java.security.PublicKey e1();
    java.security.KeyPair initialize();
    java.lang.String getSDKInfo();
    void getSDKVersion();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setSelectAllOnFocus {
    void e1(com.netcetera.threeds.sdk.infrastructure.setThumbTextPadding);
    void ThreeDS2Service();
    void initialize();
    void e1(com.netcetera.threeds.sdk.infrastructure.setSwitchPadding);
    void initialize(com.netcetera.threeds.sdk.infrastructure.setY);
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setSingleLine {
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setShowSoftInputOnFocus {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setSpannableFactory {
    void e1();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setTextKeepState {
    com.netcetera.threeds.sdk.infrastructure.setShiftDrawingOffsetForStartOverhang e1(com.netcetera.threeds.sdk.infrastructure.setSearchResultHighlightColor);
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setTextLocale {
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setTextIsSelectable {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setTextSelectHandle {
    void get();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setTextSelectHandleRight {
    java.lang.String get(java.lang.String);
}

-keep class com.netcetera.threeds.sdk.infrastructure.setTextSelectHandleLeft {
    java.lang.String ThreeDS2Service(com.netcetera.threeds.sdk.infrastructure.setTextSize);
    java.lang.String initialize(com.netcetera.threeds.sdk.infrastructure.setSwitchPadding);
}

-keep class com.netcetera.threeds.sdk.infrastructure.setTextSize {
    com.netcetera.threeds.sdk.infrastructure.entrySet initialize();
    java.lang.String get();
    java.lang.String ThreeDS2Service();
    java.lang.String getSDKVersion();
    com.netcetera.threeds.sdk.infrastructure.putAll createTransaction();
    com.netcetera.threeds.sdk.infrastructure.getProperty getWarnings();
    java.lang.String addParam();
    com.netcetera.threeds.sdk.infrastructure.isEmpty apiKey();
    java.lang.Boolean ConfigurationBuilder();
    com.netcetera.threeds.sdk.infrastructure.clear visaSchemeConfiguration();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setTextSize {
    void init$0();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setTextScaleX

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setTextScaleX {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setTextScaleX {
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setTransformationMethod {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setCollapseContentDescription {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setCollapseContentDescription {
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setTypeface {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setUseBoundsForWidth {
    java.lang.String initialize(java.lang.String);
}

-keep class com.netcetera.threeds.sdk.infrastructure.setContentInsetEndWithActions {
    void ThreeDS2ServiceInstance(com.netcetera.threeds.sdk.infrastructure.setTitleTextColor);
    void e1(com.netcetera.threeds.sdk.infrastructure.setThumbTextPadding);
    void initialize(com.netcetera.threeds.sdk.infrastructure.setSwitchPadding);
    void initialize(com.netcetera.threeds.sdk.infrastructure.setY);
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setContentInsetStartWithNavigation {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setNavigationIcon {
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setLogo {
    void init$0();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setLogo$get {
    int cleanup();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setTitleTextColor

-keep class com.netcetera.threeds.sdk.infrastructure.setThumbTextPadding

-keep class com.netcetera.threeds.sdk.infrastructure.setSwitchPadding

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setTrackTintList {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setTrackTintList {
    void createTransaction();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setTrackTintList$e1 {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setTrackTintList$get {
    void ThreeDS2Service();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setThumbDrawable {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setThumbResource {
    void init$0();
    void init$1();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setTextOff {
    void init$0();
}

-keep class com.netcetera.threeds.sdk.infrastructure.setBackInvokedCallbackEnabled {
    int ThreeDS2ServiceInstance();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setBackInvokedCallbackEnabled {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setCollapsible$get {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setMenu {
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setShowText {
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.findFragmentByTag {
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.findFragmentById {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.findFragmentById {
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.computeValue {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setMenuCallbacks {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.setMenuCallbacks {
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.clone {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.clone {
    void init$0();
}

-keep class com.netcetera.threeds.sdk.infrastructure.compareTo {
    <init>(java.lang.Runnable);
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.equals {
    void e1();
    void init$0();
}

-keep class com.netcetera.threeds.sdk.infrastructure.getClass {
    com.netcetera.threeds.sdk.infrastructure.getClass e1(com.netcetera.threeds.sdk.infrastructure.setShadowLayer,com.netcetera.threeds.sdk.infrastructure.hg,com.netcetera.threeds.sdk.infrastructure.setUseBoundsForWidth,com.netcetera.threeds.sdk.infrastructure.ok,com.netcetera.threeds.sdk.infrastructure.km,com.netcetera.threeds.sdk.infrastructure.setAllCaps$get);
    void initialize(java.lang.String,com.netcetera.threeds.sdk.infrastructure.setContentInsetEndWithActions);
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.notify {
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.addSuppressed {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.notifyAll {
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.fillInStackTrace {
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.wait {
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.getLocalizedMessage {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.printStackTrace {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.initCause {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.initCause$5 {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.initCause$4 {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.initCause$4 {
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.initCause$3 {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.initCause$3 {
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.initCause$2 {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.initCause$1 {
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.initCause$10 {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.getSuppressed {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.getStackTrace {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.getStackTrace {
    void getSDKVersion();
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.getStackTrace$4 {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.getStackTrace$1 {
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.getStackTrace$2 {
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.getStackTrace$6 {
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.getStackTrace$7 {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.getStackTrace$7 {
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.getStackTrace$10 {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.getStackTrace$10 {
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.getStackTrace$15 {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.getStackTrace$14 {
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.getStackTrace$13 {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.getStackTrace$17 {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.getStackTrace$19 {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.getStackTrace$19 {
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.getStackTrace$21 {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.getStackTrace$24 {
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.computeIfAbsent {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.computeIfAbsent {
    void ThreeDS2ServiceInstance();
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.computeIfAbsent$initialize {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.computeIfAbsent$initialize {
    void e1();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.compute {
    void e1();
    void init$0();
}

-keep class com.netcetera.threeds.sdk.infrastructure.clear

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.clear {
    void ThreeDS2ServiceInstance();
}

-keep class com.netcetera.threeds.sdk.infrastructure.entrySet {
    com.netcetera.threeds.sdk.infrastructure.entrySet e1;}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.entrySet {
    void e1();
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.getInfo {
    void ThreeDS2ServiceInstance();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.getOrDefault {
    void ThreeDS2Service();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.elements {
    void ThreeDS2ServiceInstance();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.forEach {
    void ThreeDS2ServiceInstance();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.getServices {
    void get();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.keySet {
    void ThreeDS2Service();
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.keys {
    void ThreeDS2ServiceInstance();
}

-keep class com.netcetera.threeds.sdk.infrastructure.getProperty

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.getProperty {
    void get();
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.getVersion {
    void get();
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.putIfAbsent {
    void ThreeDS2ServiceInstance();
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.put {
    void ThreeDS2Service();
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.merge {
    void e1();
    void init$0();
}

-keep class com.netcetera.threeds.sdk.infrastructure.putAll {
    com.netcetera.threeds.sdk.infrastructure.putAll get;}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.putAll {
    void ThreeDS2Service();
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.load {
    void ThreeDS2ServiceInstance();
    void init$0();
}

-keep class com.netcetera.threeds.sdk.infrastructure.isEmpty

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.isEmpty {
    void e1();
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.remove {
    void e1();
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.writeReplace {
    void ThreeDS2ServiceInstance();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.replace {
    void ThreeDS2Service();
    void init$0();
}

-keep class com.netcetera.threeds.sdk.infrastructure.contains

-keep class com.netcetera.threeds.sdk.infrastructure.save {
    void get();
    void get(com.netcetera.threeds.sdk.infrastructure.setTitleTextColor);
    void ThreeDS2ServiceInstance();
}

-keep class com.netcetera.threeds.sdk.infrastructure.propertyNames {
    void get(com.netcetera.threeds.sdk.infrastructure.contains);
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.store$ThreeDS2Service {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.aj$get {
    void ThreeDS2Service();
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.ak {
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.ak$initialize {
    void ThreeDS2Service();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.ao {
    void ThreeDS2Service();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.as {
    void ThreeDS2ServiceInstance();
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.az {
    void ThreeDS2Service();
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.ba {
    void ThreeDS2Service();
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.ay {
    void initialize();
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.ax {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.ax {
    void ThreeDS2ServiceInstance();
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.bd {
    void ThreeDS2Service();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.be {
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.bf {
    void ThreeDS2Service();
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.bf$get {
    void e1();
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.bg {
    void get();
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.bg$ThreeDS2ServiceInstance {
    void get();
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.bi {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.bi {
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.bl {
    void initialize();
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.bt {
    void initialize();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.by {
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.bz {
    void ThreeDS2Service();
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.bz$3 {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.bz$3 {
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.cb {
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.cb$initialize {
    void e1();
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.ce {
    void ThreeDS2Service();
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.ce$3 {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.ce$3 {
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.cd {
    void ThreeDS2Service();
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.ci {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.cj {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.cj {
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.co {
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.cl {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.cn {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.cp {
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.ct {
    void ThreeDS2Service();
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.cy {
    void getSDKInfo();
}

-keep class com.netcetera.threeds.sdk.infrastructure.cw {
    com.netcetera.threeds.sdk.infrastructure.save ThreeDS2ServiceInstance(com.netcetera.threeds.sdk.infrastructure.propertyNames);
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.dc {
    void init$0();
    void init$1();
    void init$2();
}

-keep class com.netcetera.threeds.sdk.infrastructure.dk {
    int initialize();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.dm {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.dl {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.ej {
    int ThreeDS2ServiceInstance();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.eo {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.ev {
    void get(long,long);
}

-keep class com.netcetera.threeds.sdk.infrastructure.ff {
    java.lang.Object[] ThreeDS2ServiceInstance(int,int);
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.ff {
    void init$0();
    void init$1();
}

-keep class com.netcetera.threeds.sdk.infrastructure.fj {
    int e1();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.fj {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.fm {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.fm$ThreeDS2Service {
    void ThreeDS2ServiceInstance();
    void init$0();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.fm$e1 {
    void ThreeDS2ServiceInstance();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.fm$get {
    void ThreeDS2ServiceInstance();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.fq {
    void ThreeDS2ServiceInstance();
}

-keep class com.netcetera.threeds.sdk.infrastructure.fo {
    int e1();
}

-keep class com.netcetera.threeds.sdk.infrastructure.go {
    com.netcetera.threeds.sdk.infrastructure.setTextScaleX initialize();
}

-keep class com.netcetera.threeds.sdk.infrastructure.hg

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.hh {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.ia {
    int cleanup();
}

-keep class com.netcetera.threeds.sdk.infrastructure.jk {
    <init>(java.util.Map);
    java.lang.String ThreeDS2Service();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.jk {
    void e1();
}

-keep class com.netcetera.threeds.sdk.infrastructure.jr {
    boolean ThreeDS2Service();
    void get();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.jr {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.jq {
    void init$0();
}

-keep class com.netcetera.threeds.sdk.infrastructure.ke {
    com.netcetera.threeds.sdk.infrastructure.setTextSize ThreeDS2Service();
    com.netcetera.threeds.sdk.infrastructure.setTextSize ThreeDS2Service(com.netcetera.threeds.sdk.infrastructure.contains);
    com.netcetera.threeds.sdk.infrastructure.setSwitchPadding ThreeDS2Service(com.netcetera.threeds.sdk.infrastructure.setY);
}

-keep class com.netcetera.threeds.sdk.infrastructure.kl {
    com.netcetera.threeds.sdk.infrastructure.setWebContentsDebuggingEnabled ThreeDS2Service();
}

-keep class com.netcetera.threeds.sdk.infrastructure.kk {
    com.netcetera.threeds.sdk.api.transaction.Transaction initialize(com.netcetera.threeds.sdk.infrastructure.protocolError,com.netcetera.threeds.sdk.infrastructure.runtimeError,com.netcetera.threeds.sdk.infrastructure.setScrollContainer,com.netcetera.threeds.sdk.infrastructure.na,com.netcetera.threeds.sdk.infrastructure.lf,com.netcetera.threeds.sdk.infrastructure.setVelocityScale$get,java.lang.String,com.netcetera.threeds.sdk.infrastructure.ok,com.netcetera.threeds.sdk.infrastructure.setFillViewport,com.netcetera.threeds.sdk.infrastructure.setAllCaps$get,com.netcetera.threeds.sdk.infrastructure.pe,com.netcetera.threeds.sdk.infrastructure.setCustomInsertionActionModeCallback,com.netcetera.threeds.sdk.infrastructure.setCompoundDrawableTintMode);
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.kk {
    void init$0();
}

-keep class com.netcetera.threeds.sdk.infrastructure.km

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.kp {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.kq {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.ku {
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.kt {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.lf

-keep class com.netcetera.threeds.sdk.infrastructure.ld {
    com.netcetera.threeds.sdk.infrastructure.lf get(java.util.Map,com.netcetera.threeds.sdk.infrastructure.na);
}

-keep class com.netcetera.threeds.sdk.infrastructure.lk {
    java.util.Map e1(java.util.Map);
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.lk {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.lh {
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.lg {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.li {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.lj {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.ln {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.ln {
    void initialize();
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.lm {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.ll {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.ll {
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.lr {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.lr {
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.lt {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.lt {
    void e1();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.ls {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.lu {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.mc {
    void ThreeDS2ServiceInstance(long,long);
}

-keep class com.netcetera.threeds.sdk.infrastructure.ms {
    int getWarnings();
}

-keep class com.netcetera.threeds.sdk.infrastructure.na

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.ng {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.nn {
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.nj {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.nr {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.ns {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.np {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.nx$ThreeDS2Service {
    void e1();
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.nw {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.nt {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.nu {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.oc {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.ny {
    boolean ThreeDS2Service(java.lang.String);
}

-keep class com.netcetera.threeds.sdk.infrastructure.nz {
    java.lang.Object get(int,int,int,int,java.lang.Object[],int,int);
}

-keep class com.netcetera.threeds.sdk.infrastructure.nz$get {
    boolean ThreeDS2ServiceInstance(java.lang.Object);
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.oa {
    void ThreeDS2Service();
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.od {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.og {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.of {
    <init>(java.lang.Object);
    java.lang.String ThreeDS2ServiceInstance(java.lang.String);
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.of {
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.oe {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.ok {
    com.netcetera.threeds.sdk.infrastructure.ok ThreeDS2Service(com.netcetera.threeds.sdk.api.configparameters.ConfigParameters);
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.ok {
    void get();
    void init$0();
}

-keep class com.netcetera.threeds.sdk.infrastructure.oi {
    void e1(java.lang.Object,java.lang.String);
    java.lang.String ThreeDS2Service(java.lang.String,java.lang.String);
    void get(int,int,java.lang.String);
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.oi {
    void init$0();
}

-keep class com.netcetera.threeds.sdk.infrastructure.oj {
    <init>();
    void get(java.lang.String);
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.oq {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.oq {
    void init$0();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.or {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.on {
    java.lang.Object get();
}

-keep class com.netcetera.threeds.sdk.infrastructure.ou {
    void initialize();
}

-keep class com.netcetera.threeds.sdk.infrastructure.ov {
    com.netcetera.threeds.sdk.infrastructure.ov e1(java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.ov e1(java.util.Collection);
    java.lang.String get(int);
    int get();
    java.lang.String toString();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.ov {
    void initialize();
    void init$0();
}

-keep class com.netcetera.threeds.sdk.infrastructure.ow {
    com.netcetera.threeds.sdk.infrastructure.ow get(java.lang.String);
    java.lang.String ThreeDS2Service(java.lang.String);
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.ow {
    void get();
    void init$0();
}

-keep class com.netcetera.threeds.sdk.infrastructure.ot {
    void e1(java.lang.String);
    void ThreeDS2ServiceInstance(java.lang.String,java.lang.Object[]);
    void initialize(java.lang.String);
    void initialize(java.lang.String,java.lang.Object[]);
    void e1(java.lang.String,com.netcetera.threeds.sdk.infrastructure.ok);
}

-keep class com.netcetera.threeds.sdk.infrastructure.oz {
    com.netcetera.threeds.sdk.infrastructure.ot initialize(java.lang.Class);
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.oz {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.pe {
    void ThreeDS2Service(com.netcetera.threeds.sdk.infrastructure.ou,java.lang.String);
    java.lang.Object initialize(com.netcetera.threeds.sdk.infrastructure.on,java.lang.String);
    void get(com.netcetera.threeds.sdk.infrastructure.ou,java.lang.String);
    java.lang.Object ThreeDS2Service(com.netcetera.threeds.sdk.infrastructure.on,java.lang.String);
    java.util.Map initialize(com.netcetera.threeds.sdk.infrastructure.setProgress);
    void ThreeDS2ServiceInstance();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.pk {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.pk {
    void init$0();
    void init$1();
    void init$2();
}

-keep class com.netcetera.threeds.sdk.infrastructure.ph {
    java.lang.String[] ThreeDS2ServiceInstance(java.lang.Object[]);
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.ph {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.pm {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.pq {
    java.lang.CharSequence initialize(java.lang.CharSequence,java.lang.CharSequence);
    boolean e1(java.lang.CharSequence);
    boolean get(java.lang.CharSequence);
    boolean e1(java.lang.CharSequence,java.lang.CharSequence);
    boolean ThreeDS2Service(java.lang.CharSequence,java.lang.CharSequence);
    boolean initialize(java.lang.CharSequence);
    java.lang.String[] ThreeDS2ServiceInstance(java.lang.String,java.lang.String);
    boolean ThreeDS2Service(java.lang.CharSequence[]);
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.pq {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.pn {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.pp {
    java.lang.Long initialize;}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.pp {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.pu {
    java.util.Date e1(java.util.Date,int);
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.pu {
    <init>();
}

-keep class org.bouncycastle.jce.provider.NcaBouncyCastleProvider {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class org.bouncycastle.jce.provider.NcaBouncyCastleProvider {
    void get();
}

-keep class com.netcetera.threeds.sdk.infrastructure.pr {
    java.lang.String ThreeDS2ServiceInstance(byte[]);
    byte[] ThreeDS2ServiceInstance(java.lang.String);
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.pr {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.ps {
    <init>();
    byte[] ThreeDS2ServiceInstance(java.lang.String);
    java.lang.String initialize(java.lang.String);
    java.lang.String initialize(byte[]);
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.pt {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.pv$e1 {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.pw {
    <init>();
    com.netcetera.threeds.sdk.infrastructure.pw$ThreeDS2Service ThreeDS2Service();
    com.netcetera.threeds.sdk.infrastructure.pw$ThreeDS2Service ThreeDS2ServiceInstance();
    java.security.SecureRandom get();
}

-keep class com.netcetera.threeds.sdk.infrastructure.pw$ThreeDS2Service {
    void e1(java.lang.String);
    java.lang.String get();
    java.lang.String initialize();
    void ThreeDS2Service(java.lang.String);
    java.lang.String ThreeDS2ServiceInstance();
    void initialize(java.lang.String);
    void ThreeDS2ServiceInstance(java.lang.String);
    java.lang.String e1();
    void get(java.lang.String);
    java.lang.String createTransaction();
    void getWarnings(java.lang.String);
    java.lang.String getSDKInfo();
    void getSDKInfo(java.lang.String);
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.py {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.py$2 {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.py$ThreeDS2ServiceInstance {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.px {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.qa {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.qc {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.qb {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.qj {
    java.lang.String initialize();
    boolean ThreeDS2ServiceInstance();
}

-keep class com.netcetera.threeds.sdk.infrastructure.qg {
    boolean ThreeDS2ServiceInstance(java.lang.String,java.lang.String);
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.qg {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.qk {
    com.netcetera.threeds.sdk.infrastructure.qk ThreeDS2Service;    <init>(com.netcetera.threeds.sdk.infrastructure.qk$get,java.lang.String[]);
    void initialize(java.lang.String);
}

-keep class com.netcetera.threeds.sdk.infrastructure.qk$get {
    com.netcetera.threeds.sdk.infrastructure.qk$get e1;}

-keep class com.netcetera.threeds.sdk.infrastructure.qm {
    com.netcetera.threeds.sdk.infrastructure.qj e1(java.lang.String);
}

-keep class com.netcetera.threeds.sdk.infrastructure.qo {
    com.netcetera.threeds.sdk.infrastructure.qo e1();
    com.netcetera.threeds.sdk.infrastructure.qm initialize();
    com.netcetera.threeds.sdk.infrastructure.qm ThreeDS2ServiceInstance();
    com.netcetera.threeds.sdk.infrastructure.qm get();
}

-keep class com.netcetera.threeds.sdk.infrastructure.ql {
    org.slf4j.Logger e1;    java.lang.String get;    <init>();
    void ThreeDS2ServiceInstance(java.lang.String);
    void ThreeDS2Service(java.lang.String);
    java.lang.String ThreeDS2Service();
    java.lang.String initialize();
    void ThreeDS2ServiceInstance(com.netcetera.threeds.sdk.infrastructure.sm);
    void initialize(java.lang.String);
}

-keep class com.netcetera.threeds.sdk.infrastructure.rn {
    com.netcetera.threeds.sdk.infrastructure.ro initialize(java.security.spec.ECParameterSpec);
    com.netcetera.threeds.sdk.infrastructure.ro ThreeDS2Service(java.security.spec.ECParameterSpec,java.lang.String,java.security.SecureRandom);
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.rn {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.ro {
    java.security.interfaces.ECPublicKey get();
    java.security.interfaces.ECPrivateKey e1();
}

-keep class com.netcetera.threeds.sdk.infrastructure.rl {
    java.security.Key createTransaction();
    void e1(java.lang.String);
    java.lang.String getWarnings();
}

-keep class com.netcetera.threeds.sdk.infrastructure.rl$initialize {
    com.netcetera.threeds.sdk.infrastructure.rl get(java.lang.String);
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.rl$initialize {
    <init>();
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.rq {
    void init$0();
}

-keep class com.netcetera.threeds.sdk.infrastructure.rr {
    java.security.PrivateKey onError();
}

-keep class com.netcetera.threeds.sdk.infrastructure.rr$ThreeDS2Service {
    com.netcetera.threeds.sdk.infrastructure.rr ThreeDS2Service(java.security.Key);
    com.netcetera.threeds.sdk.infrastructure.rr e1(java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.rr get(java.lang.String,java.lang.String);
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.rr$ThreeDS2Service {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.rp$ThreeDS2ServiceInstance {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.rp$e1 {
    void e1(long,long);
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.rp$e1 {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.rp$initialize {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.rx$ThreeDS2ServiceInstance {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.rx$ThreeDS2Service {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.rx$initialize {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.rv {
    <init>();
    boolean initialize();
    java.lang.String createTransaction();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.ru {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.ry$initialize {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.ry$ThreeDS2Service {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.ry$ThreeDS2ServiceInstance {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.ry$get {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.ry$e1 {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.ry$getWarnings {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.sd {
    java.lang.String get(java.lang.String[]);
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.sd {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.rz {
    void get(java.lang.String,java.lang.String);
    void ThreeDS2Service(java.lang.String,java.lang.Object);
    void e1(java.lang.String,com.netcetera.threeds.sdk.infrastructure.rl);
    java.lang.String e1(java.lang.String);
    java.lang.Long get(java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.rr initialize(java.lang.String,java.lang.String);
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.rz {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.sb {
    byte[] ThreeDS2ServiceInstance;    <init>();
    void e1(java.lang.String[]);
    void ThreeDS2Service(java.lang.String);
    java.lang.String getSDKInfo();
    void initialize(java.lang.String,java.lang.String);
    void initialize(java.lang.String);
    com.netcetera.threeds.sdk.infrastructure.rz ConfigParameters();
    void e1(java.lang.String,java.lang.String);
    java.lang.String getSDKInfo(java.lang.String);
    void getSDKVersion(java.lang.String);
    java.lang.String addParam();
    void cleanup(java.lang.String);
    java.security.Key onError();
    void ThreeDS2Service(java.security.Key);
    byte[] onCompleted();
    void get(byte[]);
    boolean ConfigurationBuilder();
    com.netcetera.threeds.sdk.infrastructure.qk removeParam();
    void ThreeDS2Service(com.netcetera.threeds.sdk.infrastructure.qk);
    void restrictedParameters();
    com.netcetera.threeds.sdk.infrastructure.pw apiKey();
    void ThreeDS2ServiceInstance(com.netcetera.threeds.sdk.infrastructure.pw);
}

-keep class com.netcetera.threeds.sdk.infrastructure.sa {
    void ThreeDS2Service(java.security.Key);
    java.security.Key ThreeDS2ServiceInstance(java.security.Key,java.lang.Class);
    void initialize(java.security.Key);
    void initialize(byte[],java.lang.String);
    void initialize(java.security.Key,java.lang.String,int);
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.sa {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.sc {
    <init>(byte[]);
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.si {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.se {
    <init>();
    boolean ThreeDS2ServiceInstance();
}

-keep class com.netcetera.threeds.sdk.infrastructure.sg {
    java.security.spec.ECParameterSpec initialize;    java.lang.String e1(java.security.spec.EllipticCurve);
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.sg {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.sh {
    <init>(byte[]);
}

-keep class com.netcetera.threeds.sdk.infrastructure.sf {
    java.lang.Object[] ThreeDS2Service(int,int);
}

-keepclassmembers,allowshrinking,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.sf {
    void init$0();
    void init$1();
}

-keep class com.netcetera.threeds.sdk.infrastructure.sm {
    com.netcetera.threeds.sdk.infrastructure.sm initialize;    com.netcetera.threeds.sdk.infrastructure.sm ThreeDS2Service;}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.sj {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.sk {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.sl {
    byte[] e1;    byte[] get(int);
    byte[] ThreeDS2ServiceInstance(long);
    boolean initialize(byte[],byte[]);
    byte[] ThreeDS2ServiceInstance(byte[][]);
    byte[] e1(byte[],int,int);
    byte[] ThreeDS2ServiceInstance(byte[]);
    byte[] initialize(byte[]);
    int get(byte[]);
    int initialize(int);
    int ThreeDS2Service(int);
    byte[] ThreeDS2ServiceInstance(int,java.security.SecureRandom);
    byte[] ThreeDS2ServiceInstance(int);
    java.lang.String e1(byte[]);
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.sl {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.sn {
    java.lang.String initialize(java.lang.Throwable);
    java.lang.String ThreeDS2Service(java.lang.Throwable,java.lang.Class);
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.sn {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.sp {
    java.security.MessageDigest initialize(java.lang.String);
    java.security.MessageDigest ThreeDS2ServiceInstance(java.lang.String,java.lang.String);
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.sp {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.ss {
    <init>(java.lang.String);
}

-keep class com.netcetera.threeds.sdk.infrastructure.sr {
    <init>(java.lang.String);
}

-keep class com.netcetera.threeds.sdk.infrastructure.sq {
    <init>(java.lang.String);
    <init>(java.lang.String,java.lang.Throwable);
}

-keep class com.netcetera.threeds.sdk.infrastructure.so {
    <init>(java.lang.String);
    <init>(java.lang.String,java.lang.Throwable);
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.sv {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.sx {
    java.lang.String ThreeDS2Service(byte[],java.lang.String);
    byte[] ThreeDS2ServiceInstance(java.lang.String);
    byte[] initialize(java.lang.String);
    byte[] get(java.lang.String,java.lang.String);
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.sx {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.su {
    <init>(java.lang.String);
    <init>(java.lang.String,java.lang.Throwable);
}

-keep class com.netcetera.threeds.sdk.infrastructure.sw {
    javax.crypto.Mac initialize(java.lang.String,java.security.Key,java.lang.String);
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.sw {
    <init>();
}

-keep class com.netcetera.threeds.sdk.infrastructure.st {
    byte[] e1(byte[]);
    byte[] ThreeDS2Service(byte[]);
}

-keepclassmembers,allowoptimization,allowobfuscation class com.netcetera.threeds.sdk.infrastructure.ta {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class androidx.lifecycle.Lifecycle {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class org.bouncycastle.asn1.ASN1Object {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class org.bouncycastle.asn1.ASN1Sequence {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class org.bouncycastle.asn1.ASN1Set {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class org.bouncycastle.jcajce.provider.asymmetric.mlkem.MLKEMKeyFactorySpi {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class org.bouncycastle.jcajce.provider.symmetric.util.ClassUtil {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class org.bouncycastle.jcajce.provider.util.AlgorithmProvider {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class org.bouncycastle.jce.provider.BouncyCastleProvider {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class org.bouncycastle.pqc.jcajce.provider.bike.BIKEKeyFactorySpi {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class org.bouncycastle.pqc.jcajce.provider.cmce.CMCEKeyFactorySpi {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class org.bouncycastle.pqc.jcajce.provider.dilithium.DilithiumKeyFactorySpi {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class org.bouncycastle.pqc.jcajce.provider.falcon.FalconKeyFactorySpi {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class org.bouncycastle.pqc.jcajce.provider.hqc.HQCKeyFactorySpi {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class org.bouncycastle.pqc.jcajce.provider.kyber.KyberKeyFactorySpi {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class org.bouncycastle.pqc.jcajce.provider.lms.LMSKeyFactorySpi {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class org.bouncycastle.pqc.jcajce.provider.newhope.NHKeyFactorySpi {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class org.bouncycastle.pqc.jcajce.provider.ntru.NTRUKeyFactorySpi {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class org.bouncycastle.pqc.jcajce.provider.picnic.PicnicKeyFactorySpi {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class org.bouncycastle.pqc.jcajce.provider.sphincs.Sphincs256KeyFactorySpi {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class org.bouncycastle.pqc.jcajce.provider.sphincsplus.SPHINCSPlusKeyFactorySpi {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class org.bouncycastle.pqc.jcajce.provider.xmss.XMSSKeyFactorySpi {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class org.bouncycastle.pqc.jcajce.provider.xmss.XMSSMTKeyFactorySpi {
    <init>();
}

-keepclassmembers,allowoptimization,allowobfuscation class org.bouncycastle.util.Strings {
    <init>();
}

-keep class org.slf4j.Logger {
    boolean isDebugEnabled();
}

-keep class org.slf4j.LoggerFactory {
    org.slf4j.Logger getLogger(java.lang.Class);
}

-keepclassmembers,allowoptimization,allowobfuscation class org.slf4j.helpers.MessageFormatter {
    <init>();
}

-keepclassmembers !abstract class !com.google.ads.** extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context,android.util.AttributeSet);
    public <init>(android.content.Context,android.util.AttributeSet,int);
    public void set*(...);
}

-keepclassmembers !abstract class * {
    public <init>(android.content.Context,android.util.AttributeSet);
    public <init>(android.content.Context,android.util.AttributeSet,int);
}

# Enumerations.
-keepclassmembers enum  * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class * extends javax.net.ssl.SSLSocketFactory {
    ** sslParameters;    ** context;    ** delegate;}

# #################################
# Keep #
# # Keep API enumerations ##
-keepclassmembers enum  com.netcetera.threeds.sdk.api.** {
    <fields>;    <methods>;
}
