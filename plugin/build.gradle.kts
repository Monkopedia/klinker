/*
 * Copyright 2022 Jason Monk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`

    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.gradle.publish)
    `maven-publish`
    `signing`
}

repositories {
    mavenCentral()
}

group = "com.monkopedia.klinker"
description = "Tool to link kotlin/native binaries with clang or other linkers"

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation(libs.kotlin.gradle)
    implementation(libs.guava)

    testImplementation("org.jetbrains.kotlin:kotlin-test")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    withJavadocJar()
    withSourcesJar()
}

gradlePlugin {
    website = "https://github.com/monkopedia/klinker"
    vcsUrl = "https://github.com/monkopedia/klinker"

    description = project.description
    val klinker by plugins.creating {
        id = "com.monkopedia.klinker.plugin"
        implementationClass = "com.monkopedia.klinker.KlinkerPlugin"
        displayName = "Klinker Gradle Plugin"
        description = project.description
        tags = listOf("kotlin", "kotlin/native", "linker")
    }
}

val functionalTestSourceSet = sourceSets.create("functionalTest") {
}

configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])

// Add a task to run the functional tests
val functionalTest by tasks.registering(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
}

gradlePlugin.testSourceSets(functionalTestSourceSet)

tasks.named<Task>("check") {
    dependsOn(functionalTest)
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

publishing {
    publications.all {
        if (this !is MavenPublication) return@all

        afterEvaluate {
            pom {
                name.set("klinker-gradle-plugin")
                description.set(project.description)
                url.set("http://www.github.com/Monkopedia/klinker")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("monkopedia")
                        name.set("Jason Monk")
                        email.set("monkopedia@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/Monkopedia/klinker.git")
                    developerConnection.set("scm:git:ssh://github.com/Monkopedia/klinker.git")
                    url.set("http://github.com/Monkopedia/klinker/")
                }
            }
        }
    }
    repositories {
        maven(url = "https://oss.sonatype.org/service/local/staging/deploy/maven2/") {
            name = "OSSRH"
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }
}

signing {
    useGpgCmd()
}
