package ru.mosmetro.backend.service

import org.springframework.stereotype.Service
import ru.mosmetro.backend.model.domain.OrderBaggage
import ru.mosmetro.backend.model.entity.PassengerEntity
import ru.mosmetro.backend.model.entity.PassengerPhoneEntity
import ru.mosmetro.backend.repository.PassengerEntityRepository
import ru.mosmetro.backend.repository.PassengerOrderEntityRepository
import ru.mosmetro.backend.repository.PassengerPhoneEntityRepository

@Service
class DirtyHackService(
    private val passengerOrderEntityRepository: PassengerOrderEntityRepository,
    private val passengerEntityRepository: PassengerEntityRepository,
    private val passengerPhoneEntityRepository: PassengerPhoneEntityRepository
) {
    /**
     *
     * Dirty hack
     *
     *
     * */
    suspend fun dirtyHack() {
        val passengersPhones = listOf(
            "79876543352",
            "79876544175",
            "79876544023",
            "79876543437",
            "79876543441",
            "79876543370",
            "79876543535",
            "79876543287",
            "79876544104",
            "79876543984",
            "79876544060",
            "79876544155",
            "79876543667",
            "79876543922",
            "79876543583",
            "79876543841",
            "79876543272",
            "79876544127",
            "79876543552",
            "79876543479",
            "79876543274",
            "79876543228",
            "79876543280",
            "79876543284",
            "79876543277",
            "79876543279",
            "79876544459",
            "79876543296",
            "79876543857",
            "79876544418",
            "79876543289",
            "79876543231",
            "79876543739",
            "79876544432",
            "79876543869",
            "79876544358",
            "79876543365",
            "79876543333",
            "79876543214",
            "79876543230",
            "79876543218",
            "79876544274",
            "79876543219",
            "79876543735",
            "79876543225",
            "79876543233",
            "79876543238",
            "79876543507",
            "79876543508",
            "79876543262",
            "79876543647",
            "79876543915",
            "79876543919",
            "79876544201",
            "79876543998",
            "79876543275",
            "79876544183",
            "79876543861",
            "79876544467",
            "79876544171",
            "79876544463",
            "79876544149",
            "79876543616",
            "79876544170",
            "79876543895",
            "79876544270",
            "79876543946",
            "79876543893",
            "79876543413",
            "79876543331",
            "79876543905",
            "79876544070",
            "79876543852",
            "79876543343",
            "79876543341",
            "79876543529",
            "79876543892",
            "79876544381",
            "79876543213",
            "79876543455",
            "79876543245",
            "79876544255",
            "79876544425",
            "79876544464",
            "79876543221",
            "79876543980",
            "79876543418",
            "79876543515",
            "79876543409",
            "79876543410",
            "79876543397",
            "79876543392",
            "79876544350",
            "79876544426",
            "79876544428",
            "79876544431",
            "79876544466",
            "79876543446",
            "79876543956",
            "79876543402",
            "79876544468",
            "79876544387",
            "79876544465",
            "79876544412",
            "79876543818",
            "79876543724",
            "79876543670",
            "79876543628",
            "79876543514",
            "79876543506",
            "79876543456",
            "79876543387",
            "79876543358",
            "79876543321",
            "79876543295",
            "79876543234",
            "71234567890",
            "71234567891"
        )

        val phoneDescription = listOf(
            "Телефон личный",
            "Телефон рабочий",
            "Телефон сопровождающего"
        )

//        val passengers = passengerEntityRepository.findAll()
        val passengers = emptyList<PassengerEntity>()
        val passengersPhoneEntities = mutableListOf<PassengerPhoneEntity>()
        passengers.forEach {
            passengersPhoneEntities.add(
                PassengerPhoneEntity(
                    null,
                    passengersPhones.random(),
                    phoneDescription.random(),
                    it
                )
            )
        }

//        passengerPhoneEntityRepository.saveAll(passengersPhoneEntities)

        val startDescriptionList = listOf(
            "Встретить у первого входа",
            "Встретить у третьего входа",
            "Встретить в центре зала",
            "Встретить у пятого входа на станцию",
            "Встретить у второго входа",
            "Встретить у шестого входа",
            "Встретить у встретить у эскалатора",
        )

        val finishDescriptionList = listOf(
            "Отвезти к первому выходу",
            "Отвезти ко второму выходу",
            "Отвезти к третьему выходу",
            "Отвезти к четвертому выходу",
            "Отвезти в центр зала",
            "Отвезти к эскалатору у второго выхода",
        )

        val additionalInfoList = listOf(
            "Пассажир с одним сопровождающим в черной шляпе и синем пальто",
            "Пассажир будет в синей куртке",
            "Пассажир с собакой-поводырем",
            "Пассажир будет в красной футболке",
            "Пассажир высокого роста",
            "Пассажир будет в зеленой куртке",
            "Пассажир будет в синей футболке"
        )

        val baggageList = listOf(
            OrderBaggage("Чемодан и авоська", 4, true),
            OrderBaggage("Чемодан", 3, true),
            OrderBaggage("Авоська", 1, false),
            OrderBaggage("Сумка", 2, false),
            OrderBaggage("Портфель", 1, false),
            OrderBaggage("2 чемодана", 10, true)
        )

        val passengerOrderEntities = passengerOrderEntityRepository.findAll()
        passengerOrderEntities.forEach {
            it.startDescription = startDescriptionList.random()
            it.finishDescription = finishDescriptionList.random()
            it.additionalInfo = additionalInfoList.random()
            it.baggage = baggageList.random()
        }

        passengerOrderEntityRepository.saveAll(passengerOrderEntities)

//        passengerOrderEntityRepository.saveAll(passengerOrderEntities)
    }
}
