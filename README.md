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

## Commsnds: 
- `./gradlew build` - Build the project
- `./gradlew run` - Run the application
- `./gradlew test` - Run tests
- `./gradlew clean dockerBuild` - Build a Docker image JVM but is needed aggregate to build.gradle.kts:
  - ```kotlin
    tasks.named<io.micronaut.gradle.docker.Dockerfile>("dockerfile") {
        baseImage.set("eclipse-temurin:25-jre")
    }
    ```
- `./gradlew clean dockerBuildNative`  - Build a native image and create a Docker image
- `docker image prune -a` - Remove all unused images
- `docker system df -v` - Show detailed information about disk usage
- 