plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.31"
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

group = "org.bundleproject"
version = "0.1.1"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io/")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.31")

    implementation("com.google.code.gson:gson:2.8.9")

    implementation("com.formdev:flatlaf:1.6.1")

    implementation("io.ktor:ktor-client-gson:1.6.5")
    implementation("io.ktor:ktor-client-core:1.6.5")
    implementation("io.ktor:ktor-client-apache:1.6.5")
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
