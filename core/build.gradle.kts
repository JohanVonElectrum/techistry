plugins {
    java
    idea
    id("fabric-loom") version "1.7-SNAPSHOT"
}

val minecraftVersion: String by extra
val mappingsBuild: String by extra
val fabricLoaderVersion: String by extra
val fabricApiVersion: String by extra
val javaVersion: String by extra
val mavenGroup: String by extra

val author: String by extra
val githubUrl: String by extra
val coreVersion: String by extra
val apiVersion: String by extra

val modId: String by extra
val modName: String by extra

group = mavenGroup
version = coreVersion

val baseArchiveName = "$modId-$minecraftVersion-core"
base {
    archivesName.set(baseArchiveName)
}

val dependencyProjects: List<ProjectDependency> = listOf(
    project.dependencies.project(":api", configuration = "namedElements"),
)

dependencyProjects.forEach {
    project.evaluationDependsOn(it.dependencyProject.path)
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

tasks.withType<ProcessResources> {
    inputs.property("version", version)

    filesMatching("fabric.mod.json") {
        expand(
            mapOf(
                "minecraftVersion" to minecraftVersion,
                "fabricLoaderVersion" to fabricLoaderVersion,
                "fabricApiVersion" to fabricApiVersion,
                "javaVersion" to javaVersion,
                "mavenGroup" to mavenGroup,
                "author" to author,
                "modId" to "$modId-core",
                "modName" to "$modName: Core",
                "modVersion" to version,
                "sourcesUrl" to githubUrl,
            )
        )
    }
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

    dependencyProjects.forEach {
        implementation(it)
    }

    testImplementation(platform("org.junit:junit-bom:5.10.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

loom {
    runs {
        val classPath = sourceSets.main.get().output.classesDirs
        val resourcesPath = listOf(
            sourceSets.main.get().output.resourcesDir,
        )
        val classPathGroups = listOf(classPath, resourcesPath).flatten().filterNotNull()
        val classPathGroupsString = classPathGroups.joinToString(separator = File.pathSeparator) {
            it.absolutePath.toString()
        }

        val isolatedLoomRunDir = rootProject.projectDir
            .resolve("run")
            .relativeTo(project.projectDir)
        project.logger.lifecycle("Loom run directory: $isolatedLoomRunDir")

        named("client") {
            client()
            configName = "Fabric Client"
            ideConfigGenerated(true)
            runDir(isolatedLoomRunDir.resolve("client").toString())
            vmArgs("-Dfabric.classPathGroups=$classPathGroupsString")
            project.logger.lifecycle("Client run directory: ${File(project.projectDir, runDir)}")
        }

        named("server") {
            server()
            configName = "Fabric Server"
            ideConfigGenerated(true)
            runDir(isolatedLoomRunDir.resolve("server").toString())
            vmArgs("-Dfabric.classPathGroups=$classPathGroupsString")
            project.logger.lifecycle("Server run directory: ${File(project.projectDir, runDir)}")
        }
    }
}

tasks.jar {
    from(sourceSets.main.get().output)
    for (p in dependencyProjects) {
        from(p.dependencyProject.sourceSets.main.get().output)
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.named<Jar>("sourcesJar") {
    from(sourceSets.main.get().allJava)
    for (p in dependencyProjects) {
        from(p.dependencyProject.sourceSets.main.get().allJava)
    }
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

tasks.register<Copy>("copyJarToClient") {
    from(tasks.remapJar.get().outputs.files)
    into("../run/client/mods")
}

tasks.register<Copy>("copyJarToServer") {
    from(tasks.remapJar.get().outputs.files)
    into("../run/server/mods")
}