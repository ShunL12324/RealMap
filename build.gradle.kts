import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.PluginDependency
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    id("org.spongepowered.gradle.plugin") version "1.1.1"
    id("com.github.johnrengelman.shadow") version "4.0.4"
}

group = "com.github.ericliucn"
version = "2.0"

repositories {
    mavenCentral()
}

dependencies{
    implementation( "mysql", "mysql-connector-java", "8.0.25")
}

sponge {
    apiVersion("8.0.0")
    plugin("realmap") {
        loader(PluginLoaders.JAVA_PLAIN)
        displayName("RealMap")
        mainClass("com.github.ericliucn.realmap.Main")
        description("Just testing things...")
        links {
            homepage("https://spongepowered.org")
            source("https://spongepowered.org/source")
            issues("https://spongepowered.org/issues")
        }
        contributor("Eric12324") {
            description("A Developer")
        }
        dependency("spongeapi") {
            loadOrder(PluginDependency.LoadOrder.AFTER)
            optional(false)
        }
    }
}

val javaTarget = 8 // Sponge targets a minimum of Java 8
java {
    sourceCompatibility = JavaVersion.toVersion(javaTarget)
    targetCompatibility = JavaVersion.toVersion(javaTarget)
}

tasks.withType(JavaCompile::class).configureEach {
    options.apply {
        encoding = "utf-8" // Consistent source file encoding
        if (JavaVersion.current().isJava10Compatible) {
            release.set(javaTarget)
        }
    }
}

// Make sure all tasks which produce archives (jar, sources jar, javadoc jar, etc) produce more consistent output
tasks.withType(AbstractArchiveTask::class).configureEach {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}

tasks {
    named<ShadowJar>("shadowJar") {
        relocate("google.protobuf", "myres.google.protobuf")
        relocate("com.google.protobuf", "myres.com.google.protobuf")
        relocate("com.mysql", "myres.com.mysql")
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}