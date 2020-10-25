import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

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
    args(Json.decodeFromString<List<String>>(stfDeviceSerialList))
}
