package ru.mosmetro.backend.exception

//TODO сделать общее исключение для всех типов ошибок при получении данных из бд
class EntityNotFoundException(id: Long) : MetroException("No such entity with id = $id")
