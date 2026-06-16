plugins {
    application
    id("java")
    id("io.freefair.lombok") version "8.11"
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "me.wakka"
version = "1.0-SNAPSHOT"
val javaVersion = 23

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.junit:junit-bom:5.10.0"))
    implementation("org.junit.jupiter:junit-jupiter")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("net.sourceforge.tess4j:tess4j:5.13.0")
    implementation("com.github.kwhat:jnativehook:2.2.2")
    implementation("org.jetbrains:annotations:26.0.2")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }

    withSourcesJar()
}

lombok {
    version.set("1.18.40")
}

javafx {
    version = "23.0.2"
    modules = listOf(
        "javafx.controls",
        "javafx.fxml",
        "javafx.graphics",
        "javafx.swing"
    )
}

application {
    mainClass.set("me.wakka.mapletools.MapleTools")
}

tasks {
    withType<JavaExec> {
        jvmArgs("-Dfile.encoding=UTF-8")
    }

    test {
        useJUnitPlatform()
        jvmArgs("-Dfile.encoding=UTF-8")
    }
}