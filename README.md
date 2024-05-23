# REST API банковского сервиса

Данный проект демонстрирует работу системы управления пользователями и счетами в банковском сервисе. Реализованы следующие функциональные возможности системы:
* Создание пользователей и генерация токенов для аутентификации с помощью JWT
* Доступ к методам API по JWT
* Просмотр и изменение данных аутентифицированного клиента
* Отправка денег со счета аутентифицированного клиента
* Увеличение баланса на счетах по расписанию
* Документация OpenAPI, Swagger

## Стэк
* Java 17
* Spring Boot 3
* PostgreSQL
* JsonWebToken
* JUnit
* Testcontainers
* Maven


## Запуск проекта
```
git clone https://github.com/Kofa-Yoh/BankRestApi.git
cd BankRestApi
```
```
# Заменить в application.properties данные подключения к базе данных

spring.datasource.url=jdbc:postgresql://localhost:5432/kotkinaBankDb
spring.datasource.username=postgres
spring.datasource.password=admin
```
```
mvn spring-boot:run
```
Откройте `http://localhost:8081/swagger-ui/index.html`

Ряд эндпойнтов требует аутентификации пользователя. В Swagger UI добавлена кнопка Authorize для ввода сгенерированного токена в поле Bearer Token.

Для получения токена аутентификации создайте пользователя и выполните вход (эндпойнты указаны ниже).

## Эндпойнты

Пользователи
```
# Добавление нового пользователя
[POST] /api/user/new
{
  "login": "123",
  "password": "123",
  "lastname": "Иванов",
  "firstname": "Максим",
  "patronymic": "Владиславович",
  "birthdate": "2000-05-22",
  "email": "ivanov@gmail.com",
  "phone": "+79996563963",
  "deposit": "1000"
}

# Получение токена
[POST] /api/user/login
{
  "login": "123",
  "password": "123"
}

# Выход, деактивация токена
[POST] /api/user/user_logout
```

Клиенты
```
# Получить данные текущего клиента
/api/client [GET]

# Получить данные клиентов, используя фильтры по фамилии/имени/отчеству/почте/телефону/дате рождения
/api/client/filter [GET]
{
  "lastname": "И",
  "firstname": "М",
  "patronymic": "В",
  "birthdate": "2000-05-22",
  "email": "ivanov@gmail.com",
  "phone": "+79996563963",
  "page": 0,
  "size": 3,
  "sort": "fio"
}
- Можно использовать любой из указанных фильтров, а также указать номер страницы поиска, кол-во записей на странице и сортировку
- Варианты сортировки: fio, birthdateASC, birthdateDESC

# Изменить контакты текущего пользователя
[PUT] /api/client/contacts
{
  "email": "new_email@gmail.com",
  "phone": "+71111111111"
}

# Удалить контакт текущего пользователя
[DELETE] /api/client/contacts?type=email
- Варианты заполнения параметра type: email, phone
```

Денежные переводы
```
# Перевод денег от текущего пользователя
[POST] /api/account/transfer?to=user2&amount=100
- to - логин получателя
- amount - сумма
```

## Тесты
Ряд тестов используют Docker containers, поэтому перед запуском тестов следует запустить Docker.
- AccountServiceTest
- AccountChangingServiceTest (требуется Docker)
- AccountControllerTest (требуется Docker)
```
mvn test
```