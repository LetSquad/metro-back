# metro-back

Модуль metro-back предназначен для предоставления API сервиса распределения заявок для маломобильных пассажиров метро. Все данные передаются по протоколу HTTPS в формате JSON.

Адрес Swagger UI приложения, развёрнутого в Cloud:

https://let-squad.ru/swagger-ui/

### Инструкция по сборке и запуску

#### Сборка приложения

Команда для сборки:

`mvn clean install`

В случае успешной сборки приложения в рабочей директории будет создана поддиректория target с приложением, а в лог будет выведено сообщение:

`BUILD SUCCESS`

#### Запуск приложения

Для настройки подключения к базе данных и к почтовому серверу в рабочей директории необходимо создать файл application.yml со следующим содержимым:

```yaml
spring:
  datasource:
    url: 'jdbc:postgresql://<DATABASE_HOST>/<DATABASE_NAME>'
    username: <DATABASE_USERNAME>
    password: <DATABASE_PASSWORD>
```

Команда для запуска (из директории target):

`java -jar metro-backend.jar`

При первом запуске приложения автоматически будет создана структура таблиц в базе данных при помощи Liquibase

После успешной инициализации базы данных в лог будут выведены сообщения вида:

`ChangeSet db/changelog/sql/*.sql::raw::includeAll ran successfully`

После успешного подключения к базе данных и запуска приложения в лог будут выведены сообщения вида:

```
Netty started on port 8080
Started MetroApplicationKt in 4.6 seconds (process running for 4.988)
```

По умолчанию приложение запускается на 8080 порту, при необходимости переопределить порт нужно в файл application.yml добавить значение:

```yaml
server:
  port: <SERVER_PORT>
```
