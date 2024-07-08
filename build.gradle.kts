plugins {
    `java-library`
    `maven-publish`
    id("io.papermc.paperweight.userdev") version "1.5.9"
    id("xyz.jpenilla.run-paper") version "2.1.0" // Adds runServer and runMojangMappedServer tasks for testing
}

group = "com.lokamc"
version = "2.7"
description = "LokaLib helpful utilities"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    paperweightDevBundle("com.lokamc.slice", "1.20.4-R0.1-SNAPSHOT")
    implementation("commons-io:commons-io:2.11.0")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.5")
    implementation("org.ocpsoft.prettytime:prettytime:5.0.4.Final")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.6")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.0-SNAPSHOT")
}

tasks {
    // Run reobfJar on build
    build {
        dependsOn(reobfJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }
    processResources {
        val props = mapOf(
            "version" to project.version
        )
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}

tasks.register("copyJar") {
    dependsOn("reobfJar")
    doLast {
        copy {
            from("build/libs/LokaLib-2.7.jar")
            into("D:/Loka/pts/plugins/update")
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(tasks.reobfJar.flatMap { it.outputJar }) {
                classifier = "reobf"
            }
        }
    }
}