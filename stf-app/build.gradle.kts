import com.google.gson.Gson

buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath("com.google.code.gson:gson:2.8.6")
    }
}

plugins {
    application
}

dependencies {
    implementation(project(":device-cleaner"))
}

application {
    mainClass.set("pl.droidsonroids.stf.devicecleaner.CleanerApplication")
}

tasks.named("run", JavaExec::class) {
    val stfDeviceSerialList = System.getenv("STF_DEVICE_SERIAL_LIST") ?: "[]"
    args(Gson().fromJson(stfDeviceSerialList, Array<String>::class.java))
}
