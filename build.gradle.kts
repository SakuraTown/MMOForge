plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
    kotlin("jvm") version "1.6.10"

}
group = "top.iseason"
version = "1.0.0"
val mainClass = "RPGForgeSystem"
val author = "Iseason"
val jarOutputFile = "E:\\mc\\1.18 server\\plugins"

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
    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib"))
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
}
tasks {
    shadowJar {
//        dependencies {
//            include(dependency("org.jetbrains.kotlin:kotlin-stdlib:1.6.10"))
//            include(dependency("org.jetbrains.kotlin:kotlin-reflect:1.6.10"))
//            include(dependency("com.entiv:insekicore:1.0.1"))
//        }
        relocate("com.entiv.insekicore", "${project.group}.${mainClass.toLowerCase()}.lib")
        destinationDirectory.set(file(jarOutputFile))
        minimize {
            exclude(dependency("org.jetbrains.kotlin:kotlin-reflect*"))
        }
    }
    processResources {
        val p = "${project.group}.${rootProject.name.toLowerCase()}"
        include("plugin.yml").expand(
            "name" to rootProject.name.toLowerCase(),
            "main" to "$p.$mainClass",
            "version" to project.version,
            "author" to author
        )
    }
    compileJava {
        options.encoding = "UTF-8"
    }
}
tasks.jar {
    destinationDirectory.set(file(jarOutputFile))
}