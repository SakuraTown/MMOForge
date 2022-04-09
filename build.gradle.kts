plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
    kotlin("jvm") version "1.6.10"
}
group = "top.iseason"
version = "1.0.0"
val mainClass = "MMOForge"
val author = "Iseason"

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }
    maven {
        name = "Vault"
        url = uri("https://jitpack.io")
    }
//    maven {
//        url = uri("https://maven.pkg.github.com/SakuraTown/InsekiCore")
//        credentials {
//            username = project.properties["USER"].toString()
//            password = project.properties["TOKEN"].toString()
//        }
//    }

}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.18.1-R0.1-SNAPSHOT")
    compileOnly(fileTree("lib"))
    implementation("com.entiv:insekicore:1.0.7")
    compileOnly(kotlin("stdlib"))
    compileOnly(kotlin("reflect"))
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
}
tasks {
    shadowJar {
        relocate("com.entiv.core", "${project.group}.${mainClass.toLowerCase()}.lib.core")
        relocate("org.bstats", "${project.group}.${mainClass.toLowerCase()}.lib.bstats")
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "top.iseason.mmoforge.MMOForge"))
        }
        minimize()
        project.findProperty("outputPath")?.let {
            destinationDirectory.set(file(it.toString()))
        }
    }
    processResources {
        val p = "${project.group}.${rootProject.name.toLowerCase()}"
        include("plugin.yml").expand(
            "name" to rootProject.name,
            "main" to "$p.$mainClass",
            "version" to project.version,
            "author" to author,
            "kotlin" to "1.6.10"
        )
    }
    compileJava {
        options.encoding = "UTF-8"
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> { kotlinOptions.jvmTarget = "17" }