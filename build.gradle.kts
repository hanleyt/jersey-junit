plugins {
    java
    idea
    jacoco
    `maven-publish`
    signing
}

group = "com.github.hanleyt"
version = "2.1.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

val jerseyVersion = "2.28"
val junitJupiterVersion = "5.4.1"

jacoco {
    toolVersion = "0.8.2"
}

tasks {

    withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }

    }

    jacocoTestReport {
        reports {
            xml.isEnabled = true
            csv.isEnabled = true
            html.isEnabled = true
        }
    }

    check {
        dependsOn(jacocoTestReport)
    }

    withType<Wrapper> {
        gradleVersion = "6.0.1"
    }
}

dependencies {
    compileOnly("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
    compileOnly("org.glassfish.jersey.test-framework:jersey-test-framework-core:$jerseyVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")

    testImplementation("org.glassfish.jersey.test-framework.providers:jersey-test-framework-provider-grizzly2:$jerseyVersion")
    testImplementation("org.glassfish.jersey.inject:jersey-hk2:$jerseyVersion")
}

publishing {
    publications {
        create<MavenPublication>("jerseyJunit") {
            groupId = project.group as String
            artifactId = "jersey-junit"
            version = project.version as String
            from(components["java"])

            pom {

                name.set("Jersey JUnit")
                description.set("A JUnit 5 extension library for testing JAX-RS and Jersey-based applications using the Jersey test framework.")
                url.set("https://github.com/hanleyt/jersey-junit")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("hanleyt")
                        name.set("Tom Hanley")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/hanleyt/jersey-junit.git")
                    developerConnection.set("scm:git:ssh://github.com/hanleyt/jersey-junit.git")
                    url.set("https://github.com/hanleyt/jersey-junit/")
                }
            }
        }
    }


    repositories {
        maven {
            val nexusUsername: String by project
            val nexusPassword: String by project
            credentials {
                username = nexusUsername
                password = nexusPassword
            }
            val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots")
            val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
        }
    }
}

signing {
    sign(publishing.publications["jerseyJunit"])
}