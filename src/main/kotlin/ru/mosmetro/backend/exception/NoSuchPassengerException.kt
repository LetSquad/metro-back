package ru.mosmetro.backend.exception

class NoSuchPassengerException(id: Long) : MetroException("No such passenger with id = $id")
