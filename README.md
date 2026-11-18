| Метод | Путь | Доступ | Описание |
|---|---|---|---|
| POST | /auth/register | открыт | регистрация (пароль -> bcrypt) |
| POST | /auth/login | открыт | выдача JWT |
| GET | /api/data[?search=] | JWT | список постов (поиск параметризованным запросом) |
| POST | /api/posts | JWT | создание поста |

## Запуск
```bash
export DB_PASSWORD='...'
export JWT_SECRET="$(openssl rand -base64 32)"
mvn spring-boot:run
```