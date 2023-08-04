plugins {
    kotlin("jvm") version "1.9.0"
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("com.android.tools.build:gradle:8.1.0")
    implementation("com.android.tools.ddms:ddmlib:31.1.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.26.1")
    testImplementation("org.mockito:mockito-core:5.4.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.0.0")
}
