CREATE TABLE IF NOT EXISTS usuarios(
			id UUID PRIMARY KEY,
			correo VARCHAR(200) UNIQUE NOT NULL,
			contrasenna VARCHAR(200) NOT NULL,
			nombre VARCHAR(200) NOT NULL,
			puntos INT NOT NULL,
			total_partidas INT NOT NULL,
			num_victorias INT NOT NULL);

CREATE TABLE IF NOT EXISTS amigo_de(
			emisor UUID NOT NULL,
			receptor UUID NOT NULL,
			aceptada BOOLEAN NOT NULL,
			FOREIGN KEY(emisor) REFERENCES usuarios(id) ON DELETE CASCADE,
			FOREIGN KEY(receptor) REFERENCES usuarios(id) ON DELETE CASCADE,
			PRIMARY KEY(emisor, receptor));

	
CREATE TABLE IF NOT EXISTS partidas_acabadas(
			id UUID PRIMARY KEY,
			fecha_inicio_partida DATE NOT NULL,
			fecha_fin_partida DATE NOT NULL,
			num_ias INT NOT NULL,
			modo_juego INT NOT NULL);

CREATE TABLE IF NOT EXISTS ha_jugado(
			usuario UUID NOT NULL,
			partida UUID NOT NULL,
			posicion INT NOT NULL,
			FOREIGN KEY(usuario) REFERENCES usuarios(id) ON DELETE CASCADE,
			FOREIGN KEY(partida) REFERENCES partidas_acabadas(id) ON DELETE CASCADE,
			PRIMARY KEY(usuario, partida));


CREATE TABLE IF NOT EXISTS salas (
			id UUID PRIMARY KEY,
			max_participantes INT NOT NULL,
			partida BYTEA NOT NULL);
			
CREATE TABLE IF NOT EXISTS es_miembro(
			usuario UUID NOT NULL,
			sala UUID NOT NULL,
			FOREIGN KEY(usuario) REFERENCES usuarios(id) ON DELETE CASCADE,
			FOREIGN KEY(sala) REFERENCES salas(id) ON DELETE CASCADE,
			PRIMARY KEY(usuario, sala));



/* CREATE TABLE IF NOT EXISTS reglas(
			id UUID PRIMARY KEY,
			modo INT NOT NULL,	-- 1, 2, 3
			penaliza_al_negro BOOLEAN NOT NULL,
			acumular_robo BOOLEAN NOT NULL,
			reflejo_robo BOOLEAN NOT NULL,
			jugadas_combinadas BOOLEAN NOT NULL,
			cartas_especiales BOOLEAN NOT NULL); */

/* CREATE TABLE IF NOT EXISTS iconos(
			id UUID PRIMARY KEY,
			imagen BYTEA NOT NULL,	
			coste INT NOT NULL); */
-- FALTA DESBLOQUEADO