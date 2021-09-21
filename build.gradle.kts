import org.gradle.api.publish.PublishingExtension


plugins {
  `java-library`
  `maven-publish`
  id("io.papermc.paperweight.userdev") version "1.1.11"
  id("xyz.jpenilla.run-paper") version "1.0.4" // Adds runServer and runMojangMappedServer tasks for testing
}

group = "com.lokamc"
version = "2.0"
description = "LokaLib helpful utilities"

repositories {
    mavenCentral()
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    paperDevBundle("1.17.1-R0.1-SNAPSHOT")
    implementation("commons-io:commons-io:2.7")
    implementation("com.github.ben-manes.caffeine:caffeine:3.0.3")
    implementation("org.ocpsoft.prettytime:prettytime:5.0.1.Final")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.5")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.0-SNAPSHOT")
}

tasks {
  // Run reobfJar on build
  build {
    dependsOn(reobfJar)
  }

  compileJava {
    options.encoding = Charsets.UTF_8.name()
    options.release.set(16)
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
            url = uri("https://ysera.dyndns.org:444/releases")
            // https://docs.gradle.org/current/samples/sample_publishing_credentials.html
            credentials(PasswordCredentials::class)
        }
    }
}