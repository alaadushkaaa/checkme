## Инструкция по запуску kotlinClient:
1. Запустить kotlinClient через gradle run \
(В файле [CheckMe.kt](src/jsMain/kotlin/CheckMe.kt) адрес сервера указан в переменной serverUrl.
Он должен соответствовать фактическому адресу сервера)
2. Запустить kotlinServer \
(Перед запуском проверить адрес клиента в файле 
src\main\kotlin\checkme\web\filters\CorsFilter.kt. 
Он должен соответствовать фактическому адресу клиента. По умолчанию это "http://localhost:8080")