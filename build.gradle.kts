plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij") version "1.17.4"
}
group = "com.hovertranslate"
version = "1.0.0.1"
repositories {
    mavenCentral()
}
intellij {
    version.set("2024.1")
    type.set("IC")
    plugins.set(listOf())
    updateSinceUntilBuild.set(false)
}
tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
    patchPluginXml {
        sinceBuild.set("232")
        untilBuild.set("253.*")
    }
    initializeIntelliJPlugin {
        selfUpdateCheck.set(false)
    }
    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }
    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
    buildSearchableOptions {
        enabled = false
    }
}
