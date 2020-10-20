import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "1.8"
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.4.10")
    implementation("com.android.tools.build:gradle:4.2.0-alpha13")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.10")

    testImplementation("junit:junit:4.13.1")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.23")
    testImplementation("net.jodah:concurrentunit:0.4.6")
    testImplementation("org.mockito:mockito-core:3.5.15")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
}
