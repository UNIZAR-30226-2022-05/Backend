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


PERMITIR ENVÍO DE CORREOS
en:   	C:\Program Files\Java\jdk-17.0.2\conf\security
		/usr/lib/jvm/java-11-openjdk-amd64/conf/security/java.security
comentar: jdk.tls.disabledAlgorithms ...
https://stackoverflow.com/questions/67899129/postfix-and-openjdk-11-no-appropriate-protocol-protocol-is-disabled-or-cipher


DECIDIR
buscar amigos por nombre o por correo
	MEJOR POR CORREO


SESIÓN
al hacer login, se envía correo y contraseña (hash), y se devuelve el id de sesión si hay éxito, o null si no 


curl -X POST localhost/api/login -H 'Content-type:application/json' -d '{"correo": "prueba.info@gmail.com", "contrasenna": "asdfasdf"}'
curl -X POST localhost/api/login -H 'Content-Type: application/x-www-form-urlencoded' -d "correo=prueba.info@gmail.com&contrasenna=asdfasdf"