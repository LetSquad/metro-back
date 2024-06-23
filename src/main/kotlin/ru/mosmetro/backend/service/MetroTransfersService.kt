package ru.mosmetro.backend.service

import jakarta.annotation.PostConstruct
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.graph.DefaultDirectedWeightedGraph
import org.jgrapht.graph.DefaultWeightedEdge
import org.jgrapht.graph.concurrent.AsSynchronizedGraph
import org.springframework.stereotype.Service
import ru.mosmetro.backend.mapper.MetroStationMapper
import ru.mosmetro.backend.mapper.MetroStationTransferMapper
import ru.mosmetro.backend.model.domain.MetroStation
import ru.mosmetro.backend.model.domain.MetroStationTransfer
import ru.mosmetro.backend.model.domain.OrderTransfers
import ru.mosmetro.backend.model.dto.order.OrderTransfersRequestDTO
import ru.mosmetro.backend.model.dto.order.OrderTransfersResponseDTO
import ru.mosmetro.backend.repository.MetroStationEntityRepository
import ru.mosmetro.backend.repository.MetroStationTransferEntityRepository
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@Service
class MetroTransfersService(
    private val stationMapper: MetroStationMapper,
    private val stationTransferMapper: MetroStationTransferMapper,
    stationRepository: MetroStationEntityRepository,
    private val stationTransferEntityRepository: MetroStationTransferEntityRepository
) {

    private val allStations: Map<Long, MetroStation> = stationRepository.findAll()
        .map { stationMapper.entityToDomain(it) }
        .associateBy { it.id!! }

    private val graph = AsSynchronizedGraph(DefaultDirectedWeightedGraph<Long, DefaultWeightedEdge>(DefaultWeightedEdge::class.java))

    private val transfersDurationCache = ConcurrentHashMap<Pair<Long, Long>, Long>()

    @PostConstruct
    fun init() {
        val allTransfers: List<MetroStationTransfer> = stationTransferEntityRepository.findAll()
            .map { stationTransferMapper.entityToDomain(it) }

        for (station in allStations.values) {
            graph.addVertex(station.id)
        }

        for (transfer in allTransfers) {
            val duration: Double = if (transfer.isCrosswalking) {
                transfer.duration.toSeconds().toDouble() * 2
            } else {
                transfer.duration.toSeconds().toDouble()
            }
            graph.addEdge(transfer.startStation.id, transfer.finishStation.id)
                ?.let { graph.setEdgeWeight(it, duration) }
            graph.addEdge(transfer.finishStation.id, transfer.startStation.id)
                ?.let { graph.setEdgeWeight(it, duration) }
        }
    }

    fun calculateTransfers(request: OrderTransfersRequestDTO): OrderTransfersResponseDTO {
        val result = calculateTransfers(request.startStation, request.finishStation)
        return OrderTransfersResponseDTO(
            duration = result.duration,
            transfers = result.transfers.map { stationTransferMapper.domainToDto(it) }
        )
    }

    fun calculateMetroStationTransfersDuration(
        start: MetroStation,
        finish: MetroStation,
    ): Long {
        val cachedDuration: Long? = transfersDurationCache[start.id!! to finish.id!!]
        if (cachedDuration != null) return cachedDuration

        return calculateTransfers(start.id, finish.id).duration
    }

    fun calculateTransfers(
        startStationId: Long,
        finishStationId: Long
    ): OrderTransfers {
        val pathfinder = DijkstraShortestPath(graph)
        val fullPath = pathfinder.getPath(startStationId, finishStationId)
        val pathEdges: List<DefaultWeightedEdge> = fullPath.edgeList

        lateinit var startStation: MetroStation
        var transferDuration = 0L

        val resultPath = ArrayList<MetroStationTransfer>()
        for (edge in pathEdges) {
            val currentStation: MetroStation = graph.getEdgeSource(edge)
                .let { allStations.getValue(it) }
            if (edge == pathEdges.first()) {
                startStation = currentStation
            }

            val nextStation: MetroStation = graph.getEdgeTarget(edge)
                .let { allStations.getValue(it) }

            val duration: Long = graph.getEdgeWeight(edge).toLong()

            when {
                currentStation.line != nextStation.line -> {
                    if (startStation != currentStation) {
                        resultPath.add(
                            MetroStationTransfer(
                                startStation = startStation,
                                finishStation = currentStation,
                                duration = Duration.ofSeconds(transferDuration),
                                isCrosswalking = false
                            )
                        )
                    }

                    resultPath.add(
                        MetroStationTransfer(
                            startStation = currentStation,
                            finishStation = nextStation,
                            duration = Duration.ofSeconds(duration),
                            isCrosswalking = true
                        )
                    )

                    startStation = nextStation
                    transferDuration = 0
                }
                edge == pathEdges.last() -> {
                    resultPath.add(
                        MetroStationTransfer(
                            startStation = startStation,
                            finishStation = nextStation,
                            duration = Duration.ofSeconds(transferDuration + duration),
                            isCrosswalking = false
                        )
                    )
                }
                else -> transferDuration += duration
            }
        }

        val duration: Long = fullPath.weight.toLong()
        transfersDurationCache[startStationId to finishStationId] = duration

        return OrderTransfers(
            duration = duration,
            transfers = resultPath
        )
    }
}
