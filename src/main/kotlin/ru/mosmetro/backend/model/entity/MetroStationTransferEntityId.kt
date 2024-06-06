package ru.mosmetro.backend.model.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.validation.constraints.NotNull
import java.io.Serializable
import java.util.Objects
import org.hibernate.Hibernate

@Embeddable
open class MetroStationTransferEntityId : Serializable {
    @NotNull
    @Column(name = "start_station_id", nullable = false)
    open var startStationId: Long? = null

    @NotNull
    @Column(name = "finish_station_id", nullable = false)
    open var finishStationId: Long? = null
    override fun hashCode(): Int = Objects.hash(startStationId, finishStationId)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

        other as MetroStationTransferEntityId

        return startStationId == other.startStationId &&
                finishStationId == other.finishStationId
    }

    companion object {
        private const val serialVersionUID = 3164572187930527241L
    }
}