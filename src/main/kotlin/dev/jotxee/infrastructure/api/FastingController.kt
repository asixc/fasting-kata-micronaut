package dev.jotxee.infrastructure.api

import dev.jotxee.api.FastingApi
import dev.jotxee.model.FastingDto
import io.micronaut.http.annotation.Controller
import java.time.ZonedDateTime

@Controller
class FastingController : FastingApi {

    override fun getFasting(): List<FastingDto> {
        return listOf(
            FastingDto("Intermittent Fasting 16/8", ZonedDateTime.parse("2026-04-03T20:00:00Z"))
                .end(ZonedDateTime.parse("2026-04-04T12:00:00Z"))
                .notes("Mock data"),
            FastingDto("OMAD", ZonedDateTime.parse("2026-04-02T18:00:00Z"))
                .end(ZonedDateTime.parse("2026-04-03T18:00:00Z"))
                .notes("One meal a day")
        )
    }
}
