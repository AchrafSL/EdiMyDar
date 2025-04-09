plugins {
    alias(libs.plugins.android.application) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false

}

buildscript {
    dependencies {
        classpath(libs.secrets.gradle.plugin)
    }
}