pluginManagement {
   repositories {
     gradlePluginPortal()
     maven("https://papermc.io/repo/repository/maven-public/")
   }
 }

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "LokaLib"
