CREATE TABLE IF NOT EXISTS usuarios(
			id UUID PRIMARY KEY,
			correo VARCHAR(200) UNIQUE NOT NULL,
			contrasenna VARCHAR(200) NOT NULL,
			nombre VARCHAR(200) NOT NULL,
			puntos INT NOT NULL,
			total_partidas INT NOT NULL,
			num_victorias INT NOT NULL,
			avatar INT NOT NULL,
			aspectoTablero INT NOT NULL,
			aspectoCartas INT NOT NULL);

CREATE TABLE IF NOT EXISTS amigo_de(
			emisor UUID NOT NULL,
			receptor UUID NOT NULL,
			aceptada BOOLEAN NOT NULL,
			FOREIGN KEY(emisor) REFERENCES usuarios(id) ON DELETE CASCADE,
			FOREIGN KEY(receptor) REFERENCES usuarios(id) ON DELETE CASCADE,
			PRIMARY KEY(emisor, receptor));

CREATE TABLE IF NOT EXISTS partidas_acabadas(
			id UUID PRIMARY KEY,
			fecha_inicio_partida BIGINT NOT NULL,
			fecha_fin_partida BIGINT NOT NULL,
			num_ias INT NOT NULL,
			modo_juego INT NOT NULL);

CREATE TABLE IF NOT EXISTS ha_jugado(
			usuario UUID NOT NULL,
			partida UUID NOT NULL,
			usrs_debajo INT NOT NULL,
			ha_ganado BOOLEAN NOT NULL,
			FOREIGN KEY(usuario) REFERENCES usuarios(id) ON DELETE CASCADE,
			FOREIGN KEY(partida) REFERENCES partidas_acabadas(id) ON DELETE CASCADE,
			PRIMARY KEY(usuario, partida));
			
CREATE TABLE IF NOT EXISTS es_miembro(
			usuario UUID NOT NULL,
			sala UUID NOT NULL,
			FOREIGN KEY(usuario) REFERENCES usuarios(id) ON DELETE CASCADE,
			FOREIGN KEY(sala) REFERENCES salas(id) ON DELETE CASCADE,
			PRIMARY KEY(usuario, sala));
