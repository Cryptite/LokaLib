plugins {
    `java-library`
    `maven-publish`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
    id("xyz.jpenilla.run-paper") version "3.0.2" // Adds runServer and runMojangMappedServer tasks for testing
    id("com.gradleup.shadow") version "9.5.1" // Changed from io.github.goooler.shadow
}

group = "com.lokamc"
version = "4.0"
description = "LokaLib helpful utilities"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    paperweight.paperDevBundle("26.2-R0.1-SNAPSHOT", "fork.test")
    implementation("commons-io:commons-io:2.14.0")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    implementation("org.ocpsoft.prettytime:prettytime:5.0.9.Final")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.1.0-SNAPSHOT") {
        exclude(group = "com.google.code.gson")
        exclude(group = "com.google.guava")
        exclude(group = "it.unimi.dsi")
    }
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.4.0") {
        exclude(group = "com.google.code.gson")
        exclude(group = "com.google.guava")
        exclude(group = "it.unimi.dsi")
    }
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(25)
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
            from("build/libs/LokaLib-4.0-all.jar")
            into("C:/Loka/pts1211/plugins/update")
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

// Mirrors copyJar, but uploads the shadow jar via scp run inside WSL2, so it
// uses the OpenSSH keys / known_hosts / ssh config already set up there.
// Config via gradle.properties (or -P): deployHost (required), deployUser (root),
// deployPort (22), deployDir (defaults below), deployKey (optional WSL path).
tasks.register<Exec>("uploadJar") {
    dependsOn("copyJar")
    val deployHost = providers.gradleProperty("deployHost")
    val deployUser = providers.gradleProperty("deployUser").getOrElse("root")
    val deployPort = providers.gradleProperty("deployPort").getOrElse("22")
    val deployKey = providers.gradleProperty("deployKey")
    val jar = file("build/libs/LokaLib-$version-all.jar")
    val remoteDir = providers.gradleProperty("deployDir")
        .getOrElse("/usr/local/lokacommon/server/global/plugins/update/")

    doFirst {
        require(deployHost.isPresent) {
            "deployHost is not set. Add it to gradle.properties or pass -PdeployHost=<host>."
        }
        fun toWslPath(win: String): String {
            val p = win.replace("\\", "/")
            return if (p.length >= 2 && p[1] == ':') "/mnt/" + p[0].lowercaseChar() + p.substring(2) else p
        }

        val keyOpt = if (deployKey.isPresent) "-i '${deployKey.get()}' " else ""
        val script = "scp -P $deployPort ${keyOpt}-o BatchMode=yes -o StrictHostKeyChecking=accept-new " +
                "'${toWslPath(jar.absolutePath)}' '$deployUser@${deployHost.get()}:$remoteDir'"
        commandLine("wsl", "bash", "-lc", script)
    }
}