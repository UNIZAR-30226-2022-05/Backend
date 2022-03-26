# Backend

Repositorio para el backend

# Notas
cambia las "contrasenya" por "contrasenna" que es lo que habíamos usado hasta ahora
	ya he cambiado algunos
haz que UsuarioDAO.cambiarContrasenna use internamente UsuarioDAO.modificarUsuario por no hacer otra consulta distinta a la bd
ya quedaremos para reorganizar paquetes

ahora Mail.sendMail devuelve false también si no se ha encontrado el fichero de credenciales
	recoge el valor devuelto siempre que lo uses y devuelve un error al frontend como ya se hace en GestorRegistros