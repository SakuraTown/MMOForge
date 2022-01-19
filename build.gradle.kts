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
    maven {
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }
    maven {
        url = uri("https://maven.pkg.github.com/SakuraTown/InsekiCore")
        credentials {
            username = project.properties["USER"].toString()
            password = project.properties["TOKEN"].toString()
        }
    }
//    mavenLocal()
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.18.1-R0.1-SNAPSHOT")
    implementation("com.entiv:insekicore:1.0.1")
    compileOnly(fileTree("lib"))
    compileOnly(kotlin("stdlib"))

}
tasks {
    shadowJar {
        dependencies {
            include(dependency("org.jetbrains.kotlin:kotlin-stdlib:1.6.10"))
            include(dependency("com.entiv:insekicore:1.0.1"))
        }
        destinationDirectory.set(file(jarOutputFile))
        minimize()
    }
    processResources {
        val p = "${project.group}.${rootProject.name.toLowerCase()}"
        include("config.yml")
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