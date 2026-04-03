package dev.jotxee.infrastructure.api

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/hello")
class Hello {

    @Get
    fun index(): String {
        return "Hello World"
    }
}