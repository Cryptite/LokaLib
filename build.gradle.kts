plugins {
    `java-library`
    `maven-publish`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
    id("xyz.jpenilla.run-paper") version "3.0.2" // Adds runServer and runMojangMappedServer tasks for testing
    id("com.gradleup.shadow") version "8.3.6" // Changed from io.github.goooler.shadow
}

group = "com.lokamc"
version = "3.1"
description = "LokaLib helpful utilities"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT", "fork.test")
    implementation("commons-io:commons-io:2.14.0")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    implementation("org.ocpsoft.prettytime:prettytime:5.0.9.Final")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.1.0-SNAPSHOT") {
        exclude(group = "com.google.code.gson")
        exclude(group = "com.google.guava")
        exclude(group = "it.unimi.dsi")
    }
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.9")
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

tasks.assemble {
    dependsOn(tasks.shadowJar)
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
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
    dependsOn("build")
    doLast {
        copy {
            from("build/libs/LokaLib-3.1-all.jar")
            into("D:/Loka/pts1211/plugins/update")
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}