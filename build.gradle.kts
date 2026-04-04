plugins {
    id("org.jetbrains.kotlin.jvm") version "2.3.20"
    id("org.jetbrains.kotlin.plugin.allopen") version "2.3.20"
    id("com.google.devtools.ksp") version "2.3.6"
    id("io.micronaut.application") version "4.6.2"
    id("com.gradleup.shadow") version "9.4.1"
    id("io.micronaut.aot") version "4.6.2"
    id("com.github.ben-manes.versions") version "0.53.0"
    id("io.micronaut.openapi") version "5.0.0-M1"
}

version = "0.0.1"
group = "dev.jotxee"


val kotlinVersion = project.properties["kotlinVersion"]

repositories {
    mavenCentral()
}

dependencies {
    ksp("io.micronaut:micronaut-http-validation")
    ksp("io.micronaut.serde:micronaut-serde-processor")
    implementation("io.micronaut.validation:micronaut-validation")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut.serde:micronaut-serde-jackson")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
    // openApi
    runtimeOnly("io.micronaut.openapi:micronaut-openapi")

    compileOnly("io.micronaut:micronaut-http-client")
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin") {
      exclude(group = "org.jetbrains.kotlin", module = "kotlin-reflect")
    }
    testImplementation("io.micronaut:micronaut-http-client")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}



application {
    mainClass = "dev.jotxee.ApplicationKt"
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Amicronaut.openapi.views.spec=swagger-ui.enabled=true")
}

kotlin {
    jvmToolchain(25)
}

graalvmNative.toolchainDetection = false

micronaut {
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("dev.jotxee.*")
    }
    aot {
        optimizeServiceLoading = false
        convertYamlToJava = false
        precomputeOperations = true
        cacheEnvironment = true
        optimizeClassLoading = true
        deduceEnvironment = true
        optimizeNetty = true
        replaceLogbackXml = true
    }
    openapi {
        server(file("src/main/resources/fasting-definition.yml")) {
            apiPackageName = "dev.jotxee.api"
            modelPackageName = "dev.jotxee.model"
            useReactive = false
            useAuth = false
        }
    }
}
// El plugin io.micronaut.openapi añade micronaut-openapi al KSP automáticamente.
// En enfoque API-first no necesitamos el annotation processor de OpenAPI (genera spec desde anotaciones).
// Lo excluimos para evitar un bug de compatibilidad con KSP 2.3.6 / Kotlin 2.3.20.
configurations.named("ksp") {
    exclude(group = "io.micronaut.openapi", module = "micronaut-openapi")
}

// Filtrar versiones inestables en dependencyUpdates
fun isStable(version: String): Boolean {
    val unstableKeywords = listOf("alpha", "beta", "rc", "cr", "m", "preview", "snapshot")
    return unstableKeywords.none { version.lowercase().contains(it) }
}

tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
    rejectVersionIf { !isStable(candidate.version) }
}

// KSP2 y el APT de Java generan la misma clase de introspección para los modelos Java del OpenAPI generator.
tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Git short hash — lazy, compatible con configuration cache
val gitHash = providers.exec {
    commandLine("git", "rev-parse", "--short", "HEAD")
}.standardOutput.asText.map { it.trim() }

// Docker JVM — el plugin hardcodea "21-jre" para cualquier versión >= 21,
// así que hay que sobreescribir baseImage directamente con el tipo correcto.
tasks.named<io.micronaut.gradle.docker.MicronautDockerfile>("dockerfile") {
    baseImage.set("eclipse-temurin:25-jre")
}

// Tag: nombre:version-gitHash  (ej: demo:0.1-a3f9c12)
tasks.named<com.bmuschko.gradle.docker.tasks.image.DockerBuildImage>("dockerBuild") {
    images.set(gitHash.map { hash ->
        setOf("${project.name}:${project.version}-$hash")
    })
}

// Docker Native
tasks.named<io.micronaut.gradle.docker.NativeImageDockerfile>("dockerfileNative") {
    jdkVersion = "25"
}

tasks.named<com.bmuschko.gradle.docker.tasks.image.DockerBuildImage>("dockerBuildNative") {
    images.set(gitHash.map { hash ->
        setOf("${project.name}:${project.version}-$hash-native")
    })
}
