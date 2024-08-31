plugins {
    kotlin("jvm")
    id("com.gradleup.shadow")
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.guardsquare:proguard-gradle:7.5.0")
    }
}

subprojects {
    group = rootProject.group
    version = rootProject.version
    apply {
        plugin<com.github.jengelman.gradle.plugins.shadow.ShadowPlugin>()
        plugin<JavaPlugin>()
        plugin<JavaLibraryPlugin>()
    }
    repositories {
//    阿里的服务器速度快一点
        maven {
            name = "aliyun"
            url = uri("https://maven.aliyun.com/repository/public")
        }
        maven {
            name = "aliyun-google"
            url = uri("https://maven.aliyun.com/repository/google")
        }
//        google()
        mavenCentral()
        maven {
            name = "spigot"
            url = uri("https://hub.spigotmc.org/nexus/content/repositories/public/")
        }
        maven {
            name = "jitpack"
            url = uri("https://jitpack.io")
        }
        maven {
            name = "CodeMC"
            url = uri("https://repo.codemc.org/repository/maven-public")
        }
        maven {
            name = "PlaceholderAPI"
            url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        }
        mavenLocal()
    }

    dependencies {
        val kotlinVersion: String by rootProject
        val exposedVersion: String by rootProject
        //基础库
        compileOnly("de.tr7zw:item-nbt-api-plugin:2.13.2")
        compileOnly(platform("org.jetbrains.kotlin:kotlin-bom:$kotlinVersion"))
        compileOnly(kotlin("stdlib"))
        compileOnly("org.spigotmc", "spigot-api", "1.20.3-R0.1-SNAPSHOT")
        compileOnly("me.clip:placeholderapi:2.11.3")

        // 数据库
        compileOnly("org.jetbrains.exposed", "exposed-core", exposedVersion)
        compileOnly("org.jetbrains.exposed", "exposed-dao", exposedVersion)
        compileOnly("org.jetbrains.exposed", "exposed-jdbc", exposedVersion)
        compileOnly("org.jetbrains.exposed", "exposed-java-time", exposedVersion)
        compileOnly("com.zaxxer:HikariCP:4.0.3")
    }

    tasks {
        compileJava {
            options.encoding = "UTF-8"
            sourceCompatibility = "1.8"
            targetCompatibility = "1.8"
        }
    }

}

repositories {
//    阿里的服务器速度快一点
    maven {
        name = "aliyun"
        url = uri("https://maven.aliyun.com/repository/public/")
    }
    google()
    mavenCentral()
}
dependencies {
    //基础库
    compileOnly(kotlin("stdlib"))
}
