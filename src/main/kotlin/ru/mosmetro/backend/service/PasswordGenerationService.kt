package ru.mosmetro.backend.service

import jakarta.annotation.PostConstruct
import org.passay.AllowedCharacterRule
import org.passay.CharacterData
import org.passay.CharacterRule
import org.passay.EnglishCharacterData
import org.passay.PasswordGenerator
import org.springframework.stereotype.Service

private const val TEMP_PASSWORD_CHAR_COUNT = 10

@Service
class PasswordGenerationService {

    private val specialChars: CharacterData = object : CharacterData {
        override fun getErrorCode(): String {
            return AllowedCharacterRule.ERROR_CODE
        }

        override fun getCharacters(): String {
            return "!@#$%^&*()_+"
        }
    }

    private val lowerCaseRule = CharacterRule(EnglishCharacterData.LowerCase)
    private val upperCaseRule = CharacterRule(EnglishCharacterData.UpperCase)
    private val digitRule = CharacterRule(EnglishCharacterData.Digit)
    private val splCharRule = CharacterRule(specialChars)

    private val passwordGenerator: PasswordGenerator = PasswordGenerator()

    @PostConstruct
    fun init() {
        lowerCaseRule.numberOfCharacters = 2
        upperCaseRule.numberOfCharacters = 2
        digitRule.numberOfCharacters = 2
        splCharRule.numberOfCharacters = 2
    }

    fun generateTempPassword(): String {
        val password: String = passwordGenerator.generatePassword(
            TEMP_PASSWORD_CHAR_COUNT,
            splCharRule, lowerCaseRule, upperCaseRule, digitRule
        )
        return password
    }

}