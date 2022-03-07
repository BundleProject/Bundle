plugins {
    id("org.jetbrains.kotlin.jvm") version "1.6.0"
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

group = "org.bundleproject"
version = "0.1.2"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io/")
}

dependencies {
    implementation(kotlin("stdlib-jdk8", "1.6.0"))
    implementation(kotlin("reflect", "1.6.0"))

    implementation("com.google.code.gson:gson:2.8.9")

    implementation("com.formdev:flatlaf:1.6.3")

    implementation("io.ktor:ktor-client-gson:1.6.5")
    implementation("io.ktor:ktor-client-core:1.6.5")
    implementation("io.ktor:ktor-client-apache:1.6.5")

    implementation("io.github.microutils:kotlin-logging-jvm:2.1.0")
    implementation("ch.qos.logback:logback-classic:1.2.11")

    implementation("org.bundleproject:libversion:0.0.2")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        
        relocate("com.google.code.gson", "org.bundleproject.lib.gson")
    }
    jar {
        dependsOn(shadowJar)
    }
    
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(8)
    }
}
