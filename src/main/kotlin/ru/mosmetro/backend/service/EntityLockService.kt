package ru.mosmetro.backend.service

import org.springframework.stereotype.Service
import ru.mosmetro.backend.util.getLogger
import java.util.concurrent.ConcurrentHashMap

@Service
class EntityLockService {

    private val employeeLocks = ConcurrentHashMap<Long, String>()
    private val orderLocks = ConcurrentHashMap<Long, String>()
    private val passengerLocks = ConcurrentHashMap<Long, String>()

    fun checkEmployeeLock(employeeId: Long): Boolean {
        return employeeLocks.containsKey(employeeId)
    }

    fun checkOrderLock(orderId: Long): Boolean {
        return orderLocks.containsKey(orderId)
    }

    fun checkPassengerLock(passengerId: Long): Boolean {
        return passengerLocks.containsKey(passengerId)
    }

    fun lockEmployee(employeeId: Long, sessionId: String): Boolean {
        val lockedSessionId: String = employeeLocks.getOrPut(employeeId) { sessionId }
        if (sessionId != lockedSessionId) {
            log.warn("Lock failed: employee with id = $employeeId locked by session with id = $sessionId")
        }
        return sessionId == lockedSessionId
    }

    fun lockOrder(orderId: Long, sessionId: String): Boolean {
        val lockedSessionId: String = orderLocks.getOrPut(orderId) { sessionId }
        if (sessionId != lockedSessionId) {
            log.warn("Lock failed: order with id = $orderId locked by session with id = $sessionId")
        }
        return sessionId == lockedSessionId
    }

    fun lockPassenger(passengerId: Long, sessionId: String): Boolean {
        val lockedSessionId: String = passengerLocks.getOrPut(passengerId) { sessionId }
        if (sessionId != lockedSessionId) {
            log.warn("Lock failed: passenger with id = $passengerId locked by session with id = $sessionId")
        }
        return sessionId == lockedSessionId
    }

    fun unlockEmployee(employeeId: Long, sessionId: String): Boolean {
        val isLockRemoved: Boolean = employeeLocks.remove(employeeId, sessionId)
        if (!isLockRemoved) {
            log.warn("Unlock failed: employee with id = $employeeId locked by session with id = $sessionId")
        }
        return isLockRemoved
    }

    fun unlockOrder(orderId: Long, sessionId: String): Boolean {
        val isLockRemoved: Boolean = orderLocks.remove(orderId, sessionId)
        if (!isLockRemoved) {
            log.warn("Unlock failed: order with id = $orderId locked by session with id = $sessionId")
        }
        return isLockRemoved
    }

    fun unlockPassenger(passengerId: Long, sessionId: String): Boolean {
        val isLockRemoved: Boolean = passengerLocks.remove(passengerId, sessionId)
        if (!isLockRemoved) {
            log.warn("Unlock failed: passenger with id = $passengerId locked by session with id = $sessionId")
        }
        return isLockRemoved
    }

    companion object {
        private val log = getLogger<EntityLockService>()
    }
}
