package ru.mosmetro.backend.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import ru.mosmetro.backend.model.ErrorCode
import ru.mosmetro.backend.model.dto.ErrorDTO
import ru.mosmetro.backend.util.getLogger

@RestControllerAdvice
class ErrorHandlingControllerAdvise {

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleException(e: Exception): ErrorDTO {
        log.error("Caught unhandled error", e)
        return ErrorDTO(ErrorCode.UNKNOWN_ERROR, e.message)
    }

    companion object {
        private val log = getLogger<ErrorHandlingControllerAdvise>()
    }
}
