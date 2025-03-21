import java.io.FileInputStream
import java.util.Properties

val apiKeysPropertiesFile = rootProject.file("apikeys.properties")
val apiKeysProperties = Properties()
apiKeysProperties.load(FileInputStream(apiKeysPropertiesFile))
// Add after your other tasks
tasks.register("generateGoogleServices") {
    doLast {
        val templateFile = File(projectDir, "google-services.json.template")
        val outputFile = File(projectDir, "google-services.json")

        if (templateFile.exists()) {
            var content = templateFile.readText()
            content = content.replace("\${FIREBASE_API_KEY}", apiKeysProperties.getProperty("FIREBASE_API_KEY"))
            outputFile.writeText(content)
            println("Generated google-services.json file")
        } else {
            println("Template file not found")
        }
    }
}

// Make sure this task runs before the app is built
tasks.named("preBuild") {
    dependsOn("generateGoogleServices")
}
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.safe.args)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.idz.teamup"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.idz.teamup"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "FIREBASE_API_KEY", apiKeysProperties.getProperty("FIREBASE_API_KEY"))
        buildConfigField("String", "GEODB_API_KEY", apiKeysProperties.getProperty("GEODB_API_KEY"))
        buildConfigField("String", "TOMMOROW_API_KEY", apiKeysProperties.getProperty("TOMMOROW_API_KEY"))

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        buildConfig = true
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.material)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.picasso)
    implementation(libs.firebase.appcheck.debug)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.volley)
    implementation(libs.firebase.appcheck.playintegrity)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.swiperefreshlayout)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.lottie)

}