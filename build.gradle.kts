plugins {
	java
	id("org.springframework.boot") version "3.4.1"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.asciidoctor.jvm.convert") version "3.3.2"
	id("com.epages.restdocs-api-spec") version "0.19.4"
}

val snippetsDir by extra { file("build/generated-snippets") }
val asciidoctorExt: Configuration by configurations.creating

fun getGitHash(): String {
	return providers.exec {
		commandLine("git", "rev-parse", "--short", "HEAD")
	}.standardOutput.asText.get().trim()
}

group = "kr.hhplus.be"
version = getGitHash()

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:2024.0.0")
	}
}

dependencies {
    // Spring
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")

    // DB
	runtimeOnly("com.mysql:mysql-connector-j")

	// Docs
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")
	asciidoctorExt("org.springframework.restdocs:spring-restdocs-asciidoctor:3.0.0")
	
	// RestDocs API Spec
	testImplementation("com.epages:restdocs-api-spec:0.19.4")
	testImplementation("com.epages:restdocs-api-spec-mockmvc:0.19.4")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:mysql")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	outputs.dir(snippetsDir)
}

tasks.named("asciidoctor") {
	dependsOn(tasks.test)
	
	doFirst {
		delete(file("src/main/resources/static/docs"))
	}
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
	dependsOn("asciidoctor")
	from("${layout.buildDirectory.dir("docs/asciidoc")}") {
		into("static/docs")
	}
}

tasks.register<Copy>("copyDocument") {
	dependsOn("asciidoctor")
	from(layout.buildDirectory.dir("docs/asciidoc"))
	into("src/main/resources/static/docs")
}

tasks.getByName("build") {
	dependsOn("copyDocument")
}

extensions.configure<com.epages.restdocs.apispec.gradle.OpenApi3Extension> {
	this.setServer("http://localhost:8080")
	title = "Spring RestDocs to Redoc"
	description = "Spring RestDocs를 Redoc문서로 변환한다."
	version = "0.0.1"
	format = "yaml"
	outputDirectory = "src/main/resources/static/docs"
}