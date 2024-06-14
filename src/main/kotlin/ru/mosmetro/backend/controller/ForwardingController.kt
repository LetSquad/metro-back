package ru.mosmetro.backend.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class ForwardingController {

    @RequestMapping(
        path = [
            "/{path:[^\\.]*}",
            "/{path1:[^\\.]*}/{path2:[^\\.]*}",
            "/{path1:[^\\.]*}/{path2:[^\\.]*}/{path3:[^\\.]*}"
        ]
    )
    fun forward(): String {
        return "forward:/"
    }
}
