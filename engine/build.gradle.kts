plugins {
    // Deliberately kotlin("jvm") and NOT com.android.library: with no Android
    // classes on the compile classpath, an `android.*` import cannot compile.
    // No version: the Kotlin plugin is already on the build classpath via AGP,
    // and re-declaring a version there fails plugin resolution.
    kotlin("jvm")
}

java {
    // Matches :app's compileOptions so the engine jar stays consumable by it.
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed", "skipped")
    }
}
