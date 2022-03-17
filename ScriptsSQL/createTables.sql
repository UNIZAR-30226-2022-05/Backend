CREATE TABLE IF NOT EXISTS usuarios(
			id_usuario UUID PRIMARY KEY,
			correo VARCHAR(200) UNIQUE NOT NULL,
			nombre VARCHAR(200) NOT NULL,
			password VARCHAR(200) NOT NULL,
			es_admin BOOL NOT NULL,
			es_premium BOOL NOT NULL);

CREATE TABLE IF NOT EXISTS proyectos(
			id_proyecto UUID PRIMARY KEY,
			titulo VARCHAR(100) NOT NULL,
			descripcion VARCHAR(200),
			color CHAR(6) NOT NULL,
			tamagno BIGINT,
			fecha_mod BIGINT);

CREATE TABLE IF NOT EXISTS notas_g(
			id_nota UUID PRIMARY KEY,
			titulo VARCHAR(100) NOT NULL,
			color CHAR(6) NOT NULL,
			proyecto UUID NOT NULL,
			usuario UUID,
			tamagno BIGINT,
			fecha_mod BIGINT,
			FOREIGN KEY(proyecto) references proyectos(id_proyecto) ON DELETE CASCADE,
			FOREIGN KEY(usuario) references usuarios(id_usuario) ON DELETE SET NULL);

CREATE TABLE IF NOT EXISTS notas(
			id_nota UUID PRIMARY KEY,
			texto TEXT NOT NULL,
			FOREIGN KEY(id_nota) REFERENCES notas_g(id_nota) ON DELETE CASCADE);

CREATE TABLE IF NOT EXISTS tareas(
			id_nota UUID PRIMARY KEY,
			fecha_limite DATE,
			completada BOOL NOT NULL,
			FOREIGN KEY(id_nota) REFERENCES notas_g(id_nota) ON DELETE CASCADE);

CREATE TABLE IF NOT EXISTS alertas(
			tarea UUID,usuario UUID,
			fecha DATE NOT NULL,
			FOREIGN KEY(tarea) REFERENCES tareas(id_nota) ON DELETE CASCADE,
			FOREIGN KEY(usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
			PRIMARY KEY(tarea, usuario));

CREATE TABLE IF NOT EXISTS es_miembro(
			proyecto UUID,
			usuario UUID,
			es_coordinador BOOL NOT NULL,
			FOREIGN KEY(proyecto) REFERENCES proyectos(id_proyecto) ON DELETE CASCADE,
			FOREIGN KEY(usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
			PRIMARY KEY(proyecto, usuario));

CREATE TABLE IF NOT EXISTS subtareas(
			id_subtarea UUID PRIMARY KEY,
			texto TEXT NOT NULL,
			completada BOOL NOT NULL,
			tarea UUID,
			FOREIGN KEY(tarea) REFERENCES tareas(id_nota) ON DELETE CASCADE);




CREATE TABLE IF NOT EXISTS accesos(
			fecha DATE PRIMARY KEY,
			num_accesos INT);


INSERT INTO usuarios VALUES('4c2a49ed-48be-4970-9010-edb1faf918f1', 'ideanote.info@gmail.com', 'IdeaNote Admin', 'sisinf2020', true, true);
