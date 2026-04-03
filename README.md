## Micronaut 4.10.11 Documentation

- [User Guide](https://docs.micronaut.io/4.10.11/guide/index.html)
- [API Reference](https://docs.micronaut.io/4.10.11/api/index.html)
- [Configuration Reference](https://docs.micronaut.io/4.10.11/guide/configurationreference.html)
- [Micronaut Guides](https://guides.micronaut.io/index.html)

---

- [Shadow Gradle Plugin](https://gradleup.com/shadow/)
- [Micronaut Gradle Plugin documentation](https://micronaut-projects.github.io/micronaut-gradle-plugin/latest/)
- [GraalVM Gradle Plugin documentation](https://graalvm.github.io/native-build-tools/latest/gradle-plugin.html)

## Feature ksp documentation

- [Micronaut Kotlin Symbol Processing (KSP) documentation](https://docs.micronaut.io/latest/guide/#kotlin)
- [https://kotlinlang.org/docs/ksp-overview.html](https://kotlinlang.org/docs/ksp-overview.html)

## Feature serialization-jackson documentation

- [Micronaut Serialization Jackson Core documentation](https://micronaut-projects.github.io/micronaut-serialization/latest/guide/)

## Feature micronaut-aot documentation

- [Micronaut AOT documentation](https://micronaut-projects.github.io/micronaut-aot/latest/guide/)

---

## Commands

### Build & Run

| Command | Description |
|---|---|
| `./gradlew build` | Compile and run tests |
| `./gradlew run` | Run the application locally |
| `./gradlew test` | Run all tests |
| `./gradlew clean` | Clean build output |

### Docker — JVM

> ⚠️ The Micronaut Gradle plugin hardcodes `eclipse-temurin:21-jre` for any JDK >= 21.
> The fix in `build.gradle.kts` overrides it using the correct task type `MicronautDockerfile`:
> ```kotlin
> tasks.named<io.micronaut.gradle.docker.MicronautDockerfile>("dockerfile") {
>     baseImage.set("eclipse-temurin:25-jre")
> }
> ```

```bash
# Build JVM image — tagged as <name>:<version>-<git-short-hash>
./gradlew clean dockerBuild

# Run JVM container (auto-removed on stop)
docker run --rm --name demo-jvm -p 8081:8080 -d demo:<version>-<hash>
```

### Docker — Native (GraalVM)

```bash
# Build native image — tagged as <name>:<version>-<git-short-hash>-native
./gradlew clean dockerBuildNative

# Run native container (auto-removed on stop)
docker run --rm --name demo-native -p 8082:8080 -d demo:<version>-<hash>-native
```

### Docker — Utilities

```bash
# Compare JVM vs Native resource usage in real time
docker stats demo-jvm demo-native

# Remove all unused images
docker image prune -a

# Show detailed disk usage
docker system df -v
```

### OpenAPI

```bash
# Regenerate interfaces and models from the YAML spec
./gradlew generateServerOpenApiApis generateServerOpenApiModels

# Generated files are located at:
# build/generated/openapi/generateServerOpenApiApis/src/main/java/dev/jotxee/api/
# build/generated/openapi/generateServerOpenApiModels/src/main/java/dev/jotxee/model/

# Swagger UI (with the app running):
# http://localhost:8080/swagger-ui/
# http://localhost:8080/swagger/demo-1.0.0.yml
```

---

## Adding OpenAPI server generation (API-first)

Steps required to configure server code generation from an OpenAPI spec.

### 1. Define the spec and schemas

```
src/main/resources/
├── fasting-definition.yml      # spec root (openapi: 3.0.3, paths with $ref)
├── paths/
│   └── fasting.yml             # endpoint definitions (operationId is required)
└── schemas/
    ├── fasting.yml             # domain model schema
    └── get.yml                 # response schema (array of fasting)
```

> The spec must use `openapi: 3.0.3` — the `io.micronaut.openapi:5.0.0-M1` plugin does not support 3.1 or 3.2.
> Each endpoint needs an `operationId` so the generator can name methods correctly.
> To add a new resource, repeat steps 1–5 and register a new `server()` block in `build.gradle.kts` (see step 2).

### 2. Add and configure the plugin in `build.gradle.kts`

```kotlin
plugins {
    id("io.micronaut.openapi") version "5.0.0-M1"
}

micronaut {
    openapi {
        // Add one server() block per resource
        server(file("src/main/resources/fasting-definition.yml")) {
            apiPackageName = "dev.jotxee.api"      // package for generated interfaces
            modelPackageName = "dev.jotxee.model"  // package for generated models
            useReactive = false
            useAuth = false   // true requires micronaut-security
        }
        // server(file("src/main/resources/workout-definition.yml")) { ... }
    }
}
```

### 3. Add dependencies in `build.gradle.kts`

```kotlin
dependencies {
    // Validation — generated models use @NotNull, @Valid, etc.
    implementation("io.micronaut.validation:micronaut-validation")

    // OpenAPI runtime — Swagger UI and spec serving
    runtimeOnly("io.micronaut.openapi:micronaut-openapi")
}

// Exclude micronaut-openapi from KSP — the plugin adds it automatically,
// but in API-first mode the annotation processor is not needed (it generates spec from annotations).
// With KSP 2.3.6 + Kotlin 2.3.20 it causes a compatibility bug.
// See resolve-openapi-problems.md for details.
configurations.named("ksp") {
    exclude(group = "io.micronaut.openapi", module = "micronaut-openapi")
}

// KSP2 and the Java APT both generate the same introspection class for the Java models.
tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Enable Swagger UI static file generation during Java annotation processing.
tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Amicronaut.openapi.views.spec=swagger-ui.enabled=true")
}
```

### 4. Configure spec serving in `application.yaml`

```yaml
micronaut:
  router:
    static-resources:
      swagger:
        paths: classpath:META-INF/swagger
        mapping: /swagger/**
      swagger-ui:
        paths: classpath:META-INF/swagger/views/swagger-ui
        mapping: /swagger-ui/**
```

### 5. Implement the controller

The generator produces a `FastingApi` interface in `dev.jotxee.api`. The controller implements it:

```kotlin
// src/main/kotlin/dev/jotxee/infrastructure/api/FastingController.kt
@Controller  // required even though the interface already has it
class FastingController : FastingApi {
    override fun getFasting(): List<Fasting> { ... }
}
```

> `@Controller` must be on the implementing class — Micronaut does not inherit routing solely from the interface.

---

### Dependency Updates

Uses the `com.github.ben-manes.versions` plugin. Unstable versions (alpha, beta, rc, milestone) are filtered automatically.

```bash
# Check for stable dependency updates
./gradlew dependencyUpdates -Drevision=release

# Report is saved to:
# build/dependencyUpdates/report.txt
```
