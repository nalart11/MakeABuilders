plugins {
    java
    id("com.gradleup.shadow") version "9.0.0-beta10"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "org.MakeACakeStudios"
version = "b0.4"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("net.luckperms:api:5.4")

    implementation("com.mojang:authlib:1.5.25")
    implementation("org.incendo:cloud-paper:2.0.0-beta.10")
    implementation("org.incendo:cloud-core:2.0.0")
    implementation("net.kyori:adventure-api:4.11.0")
    implementation("net.kyori:adventure-platform-bukkit:4.1.2")
    implementation("net.kyori:adventure-text-minimessage:4.11.0")
    implementation("org.incendo:cloud-paper:2.0.0-beta.10")
    implementation("org.xerial:sqlite-jdbc:3.40.0.0")
}

val targetJavaVersion = 21

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    }
}

tasks {
    runServer {
        minecraftVersion("1.21.4")
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
        val properties = inputs.properties.map {
            it.key to it.value
        }.toMap(hashMapOf()).apply { this["version"] = version }
        filesMatching("paper-plugin.yml") { expand(properties) }
    }

    shadowJar {
        archiveClassifier.set("shad")
        mergeServiceFiles()
    }

    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(targetJavaVersion)
        }
    }
}



