plugins {
    kotlin("jvm") version "1.9.22"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    // kotest の依存関係
    testImplementation("io.kotest:kotest-runner-junit5:5.8.1") // KotestのJUnit5ランナー
    testImplementation("io.kotest:kotest-assertions-core:5.8.1") // Kotestのアサーションライブラリ
    testImplementation("io.kotest:kotest-property:5.8.1") // Kotestのプロパティベースのテスト
    testImplementation("io.kotest:kotest-framework-engine:5.8.1") // Kotestのプロパティベースのテスト
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
