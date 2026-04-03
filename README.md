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

### Dependency Updates

Uses the `com.github.ben-manes.versions` plugin. Unstable versions (alpha, beta, rc, milestone) are filtered automatically.

```bash
# Check for stable dependency updates
./gradlew dependencyUpdates -Drevision=release

# Report is saved to:
# build/dependencyUpdates/report.txt
```
