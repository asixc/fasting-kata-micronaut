package dev.jotxee.controller

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/hello")
class Hello {

    @Get
    fun index(): String {
        return "Hello World"
    }
}