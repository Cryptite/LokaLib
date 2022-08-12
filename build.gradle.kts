plugins {
    `java-library`
    `maven-publish`
    id("io.papermc.paperweight.userdev") version "1.3.8"
    id("xyz.jpenilla.run-paper") version "1.0.6" // Adds runServer and runMojangMappedServer tasks for testing
}

group = "com.lokamc"
version = "2.3"
description = "LokaLib helpful utilities"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://maven.enginehub.org/repo/")
//    maven("https://test.lokamc.com:444/releases")
}

dependencies {
    paperweightDevBundle("com.lokamc.slice", "1.19.2-R0.1-SNAPSHOT")
    implementation("commons-io:commons-io:2.11.0")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.1")
    implementation("org.ocpsoft.prettytime:prettytime:5.0.3.Final")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.6")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.8")
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

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(tasks.reobfJar.flatMap { it.outputJar }) {
                classifier = "reobf"
            }
        }
    }

    repositories {
        maven {
            name = "Ysera"
            url = uri("https://test.lokamc.com:444/releases")
            // https://docs.gradle.org/current/samples/sample_publishing_credentials.html
            credentials(PasswordCredentials::class)
        }
    }
}