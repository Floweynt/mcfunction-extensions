plugins {
    `java-library`
    id("com.floweytf.paperweight-aw.userdev") version "1.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.spongepowered.org/maven/")
    maven("https://maven.floweytf.com/releases")
    maven("https://maven.fabricmc.net/")
    maven("https://repo.codemc.io/repository/maven-public/")
}

val igniteVersion: String by project
val paperVersion: String by project
val mixinVersion: String by project
val mixinExtrasVersion: String by project
val tinyRemapperVersion: String by project

val shadowImplementation: Configuration by configurations.creating
paperweight.awPath.set(file("src/main/resources/REPLACEME-example.accesswidener"))

dependencies {
    paperweight.paperDevBundle(paperVersion)

    // We need ignite!
    implementation(libs.bundles.ignite)

    // We need paper!
    implementation(libs.paper)

    // Required for server to start when running "Minecraft Server"
    implementation(libs.bundles.papermisc)

    // Compile time: mixins, ignite, paper
    implementation(libs.bundles.ignite)
    compileOnly(libs.bundles.mixin)

    // Tiny remapper
    remapper(libs.tinyremapper) {
        artifact {
            classifier = "fat"
        }
    }
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(17)
    withSourcesJar()
}

tasks {
    jar {
        archiveClassifier.set("dev")
    }

    shadowJar {
        archiveClassifier.set("")
        configurations = listOf(shadowImplementation)
    }

    reobfJar {
        remapperArgs.add("--mixin")
    }

    build {
        dependsOn(reobfJar)
    }
}
