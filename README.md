# Backend
Repositorio para el backend

# Notas
se ha descartado el campo validado - si no se valida no cuenta como registrado, únicamente está el UsuarioVO en memoria
es necesario saber quién ha hecho la solicitud de amisatad
no se deben poner los id en el esquema E/R
las IAs no deberían ser usuarios, basta con saber el número que había, y solo para el caso de partidas ya jugadas
las salas en pausa en bd no es necesario que tengan las reglas, pues estarán implícitas en el binario
para permitir que el historial de partidas se pueda recortar, se guardarán las estadísticas calculadas