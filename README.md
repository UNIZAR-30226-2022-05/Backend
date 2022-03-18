# Backend
Repositorio para el backend

# Notas

- - - - - - - 17/03/2022
propuestas (se puede debatir):
-se ha descartado el campo validado - si no se valida no cuenta como registrado, únicamente está el UsuarioVO en memoria
-es necesario saber quién ha hecho la solicitud de amisatad
-no se deben poner los id en el esquema E/R
-las IAs no deberían ser usuarios, basta con saber el número que había, y solo para el caso de partidas ya jugadas
-las salas en pausa en bd no es necesario que tengan las reglas, pues estarán implícitas en el binario
-para permitir que el historial de partidas se pueda recortar, se guardarán las estadísticas calculadas
-las estadísticas se fusionan en la tabla usuarios para evitar joins
-más sencillo para los triggers guardar el nº de jugadores por debajo en el historial, y si ha ganado o no

FALTA:
- decidir cómo se codifica el modo de juego




spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.url=jdbc:postgresql://localhost:5432/uno_for_all_db?currentSchema=uno_for_all
spring.datasource.username=postgresql
spring.datasource.password=unoforall

spring.jpa.hibernate.dll-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.show-sql=true