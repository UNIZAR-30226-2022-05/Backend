# Notas
cambia las "contrasenya" por "contrasenna" que es lo que habíamos usado hasta ahora
	ya he cambiado algunos
/*RE: Hecho el cambio en las clases encargadas del reestablecimiento y el API REST

haz que UsuarioDAO.cambiarContrasenna use internamente UsuarioDAO.modificarUsuario por no hacer otra consulta distinta a la bd
/*RE: Considero que eso es marear la perdiz, porque tendría que hacer una consulta adicional para sacar el resto de valores
/*	  que debería conservar.

ya quedaremos para reorganizar paquetes

ahora Mail.sendMail devuelve false también si no se ha encontrado el fichero de credenciales
	recoge el valor devuelto siempre que lo uses y devuelve un error al frontend como ya se hace en GestorRegistros
	
	
FALTA: añadir un id a la tabla de usuarios en la bd para guardar su avatar



##### CAMBIOS IMPORTANTES
La sesión se gestiona íntegrametne con websockets, si no es mucho lío tener dos id de sesión y con websockets es muy fácil saber cuándo se cierra la conexión
Por tanto, ahora para iniciar sesión
 1. se llama al login de apirest para obtener una clave de inicio
 2. nos conectamos a los websockets y enviamos a app/login dicha clave. Nos devolverá el id de sesión (lo genera websockets)
 3. para las sucesivas llamadas a apirest se deberá enviar dicho id (para websockets va implícito)
 
Ya he hecho los cambios en el gestorSesion. Como ya no hace falta el Timer en este caso (sí para lo del registro y tal), el hashmap es de idSesion - idUsuario (no es necesario el UsuarioVO entero, está en la bd). He cambiado también el resto de llamadas api para adaptarlo a esto (GestorActualizacion ya no necesita el correo viejo, simplemente se apaña con el id del usuario y queda más sencillo)