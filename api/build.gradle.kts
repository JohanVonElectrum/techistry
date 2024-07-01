plugins {
    java
    idea
    id("fabric-loom") version "1.7-SNAPSHOT"
    id("maven-publish")
}

val minecraftVersion: String by extra
val mappingsBuild: String by extra
val fabricLoaderVersion: String by extra
val fabricApiVersion: String by extra
val javaVersion: String by extra
val mavenGroup: String by extra

val apiVersion: String by extra

val modId: String by extra

group = mavenGroup
version = apiVersion

val baseArchiveName = "$modId-$minecraftVersion-core"
base {
    archivesName.set(baseArchiveName)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
    withSourcesJar()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    javaToolchains {
        compilerFor {
            languageVersion.set(JavaLanguageVersion.of(javaVersion))
        }
    }
    options.release.set(JavaLanguageVersion.of(javaVersion).asInt())
}

repositories {
    mavenCentral()
}

dependencies {
    minecraft(
        group = "com.mojang",
        name = "minecraft",
        version = minecraftVersion
    )
    mappings(
        group = "net.fabricmc",
        name = "yarn",
        version = "$minecraftVersion+build.$mappingsBuild",
        classifier = "v2"
    )
    modImplementation(
        group = "net.fabricmc",
        name = "fabric-loader",
        version = fabricLoaderVersion
    )
    modImplementation(
        group = "net.fabricmc.fabric-api",
        name = "fabric-api",
        version = "$fabricApiVersion+$minecraftVersion"
    )

    testImplementation(platform("org.junit:junit-bom:5.10.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.jar {
    from(sourceSets.main.get().output)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.named<Jar>("sourcesJar") {
    from(sourceSets.main.get().allJava)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveClassifier.set("sources")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    outputs.upToDateWhen { false }
}

artifacts {
    archives(tasks.remapJar)
    archives(tasks.remapSourcesJar)
}