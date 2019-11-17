plugins {
    java
    idea
    jacoco
    maven
}

group = "com.github.hanleyt"
version = "2.1.0"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

val jerseyVersion = "2.28"
val junitJupiterVersion = "5.4.1"

jacoco {
    toolVersion = "0.8.2"
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
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

    artifacts {
        archives(sourcesJar)
    }

    withType<Wrapper> {
        gradleVersion = "5.3.1"
    }
}

dependencies {
    compileOnly("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
    compileOnly("org.glassfish.jersey.test-framework:jersey-test-framework-core:$jerseyVersion")

    testCompile("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")

    testCompile("org.glassfish.jersey.test-framework.providers:jersey-test-framework-provider-grizzly2:$jerseyVersion")
    testCompile("org.glassfish.jersey.inject:jersey-hk2:$jerseyVersion")
}
