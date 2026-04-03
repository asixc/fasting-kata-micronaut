package dev.jotxee

import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@MicronautTest
class HelloControllerTest {

    @Inject
    lateinit var application: EmbeddedApplication<*>

    @Inject
    @field:Client("/")
    lateinit var httpClient: HttpClient

    @Test
    fun testHello() {
        Assertions.assertTrue(application.isRunning)
        val body = httpClient.toBlocking().retrieve("/hello")
        Assertions.assertEquals("Hello World", body)
    }

    @Test
    fun testHelloWorldShouldRespondsWithStatusCode200() {
        val response = httpClient.toBlocking().exchange("/hello", String::class.java)
        assertEquals(response.status, HttpStatus.OK)
        assertEquals(response.getBody().get() , "Hello World")

    }

}