# Problemas encontrados al integrar OpenAPI server generation

Registro de todos los errores encontrados al configurar `io.micronaut.openapi` en modo API-first (spec → código generado) con Kotlin 2.3.20, KSP 2.3.6 y Micronaut 4.10.11.

## Tabla de problemas y soluciones

| Problema | Causa | Fix |
|---|---|---|
| YAMLs rotos (`fasting-definition.yml`, `paths/fasting.yml`) | Bloque `get:` suelto en el root + indentación incorrecta en `responses` y `content` + `$ref` apuntando a ruta inexistente | Reescribir los ficheros con la estructura correcta |
| `openapi: 3.2.0` no soportado | El generador del plugin `io.micronaut.openapi:5.0.0-M1` solo soporta hasta OpenAPI 3.0.x | Cambiar a `openapi: 3.0.3` |
| `useAuth = true` sin dependencia de security | El generador añade `@Secured(SecurityRule.IS_ANONYMOUS)` en la interfaz, que requiere `micronaut-security` | Cambiar a `useAuth = false` en `build.gradle.kts` |
| Paquetes `example.micronaut.*` | Valor por defecto de la guía, no corresponde al grupo del proyecto | Cambiar a `dev.jotxee.api` y `dev.jotxee.model` |
| Bug KSP: `OpenApiApplicationVisitor` falla en `finish()` | Incompatibilidad entre `micronaut-openapi` como KSP processor y KSP 2.3.6/Kotlin 2.3.20. El visitor intenta acceder a elementos PSI ya invalidados al escribir el swagger YAML al final del procesamiento | Excluir `micronaut-openapi` de la configuración `ksp` — en enfoque API-first no se necesita el annotation processor (ver detalle abajo) |
| `jakarta.validation` no disponible | Los modelos generados usan `@NotNull`, `@Valid`, etc. pero la dependencia no estaba declarada | Añadir `implementation("io.micronaut.validation:micronaut-validation")` |
| Clase de introspección duplicada (KSP + Java APT) | KSP2 procesa los `.java` generados por el OpenAPI generator (que tienen `@Serdeable`) y el procesador Java APT también lo hace, generando `$Fasting$Introspection.class` dos veces | `tasks.withType<Jar> { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }` |
| `FastingController` sin `@Controller` → 404 | Micronaut requiere la anotación en la clase implementadora, no solo en la interfaz generada | Añadir `@Controller` a `FastingController` |
| Swagger UI → 404 | El annotation processor no genera los ficheros estáticos de Swagger UI por defecto | Añadir `-Amicronaut.openapi.views.spec=swagger-ui.enabled=true` a los args del compilador Java |

## Detalle: el bug de `OpenApiApplicationVisitor`

### Qué es

`OpenApiApplicationVisitor` es un **TypeElementVisitor** de Micronaut. Es el componente del enfoque **code-first** (anotaciones → spec): escanea los `@Controller`, `@Get`, etc. de tu código y genera automáticamente el fichero `swagger.yml` en `META-INF/swagger/` durante la compilación.

### Por qué falla con KSP 2.3.6 / Kotlin 2.3.20

En KSP2 (la versión actual para Kotlin 2.x), los elementos PSI (el árbol sintáctico que KSP usa para analizar el código) tienen un **tiempo de vida ligado a la sesión de análisis**. En la fase `finish()` — que es cuando todos los visitors escriben sus ficheros finales — la sesión ya ha terminado y los objetos PSI están invalidados. El visitor intenta acceder a ellos para obtener el `filePath` del fichero donde escribir el YAML y lanza `KaInvalidLifetimeOwnerAccessException`.

### ¿Es culpa de usar Kotlin?

Sí y no:

- **Sí**: el problema solo ocurre en el procesamiento KSP (Kotlin). En un proyecto Java puro, el mismo visitor funciona vía el procesador de anotaciones de Java (APT de javac), que no tiene esta restricción de tiempo de vida en PSI.
- **No** exactamente: es un bug de **compatibilidad entre versiones** — `micronaut-openapi 4.10.x` no fue actualizado para manejar correctamente el ciclo de vida de PSI en KSP2. No es un problema inherente a Kotlin, sino a que el visitor fue escrito para KSP1 y KSP2 cambió el modelo de tiempo de vida.

### Por qué se puede excluir sin problema

En el enfoque **API-first** (que es el que usamos), el flujo es:

```
spec YAML → OpenAPI Generator → FastingApi.java + Fasting.java → tu controller implementa la interfaz
```

El `OpenApiApplicationVisitor` haría el flujo contrario:

```
@Controller + @Get en tu código → OpenApiApplicationVisitor → swagger.yml generado
```

Como no necesitamos que se genere el spec desde las anotaciones (ya tenemos el spec de origen), podemos excluir el visitor completamente de KSP sin perder nada.
