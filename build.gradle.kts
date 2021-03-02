import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.31"
    application
}

group = "io.ys"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
    testImplementation("org.testcontainers:testcontainers:1.15.2")
    testImplementation("org.testcontainers:postgresql:1.15.2")
    testImplementation("io.kotest:kotest-extensions-testcontainers:4.4.1")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:4.4.1")
    testImplementation("io.kotest:kotest-assertions-core-jvm:4.4.1")

    implementation("org.jetbrains.exposed:exposed-core:0.29.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.29.1")
    implementation("com.zaxxer:HikariCP:4.0.2")
    implementation("org.postgresql:postgresql:42.2.19")

}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClassName = "MainKt"
}