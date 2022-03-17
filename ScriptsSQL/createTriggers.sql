CREATE OR REPLACE FUNCTION estadisticas_trigger() RETURNS TRIGGER AS $estadisticas_trigger$
	DECLARE
	BEGIN
		UPDATE usuarios
		SET total_partidas = total_partidas + 1
		WHERE usuarios.id = NEW.usuario;
		
		IF NEW.ha_ganado = true THEN
			UPDATE usuarios
			SET num_victorias = num_victorias + 1
			WHERE usuarios.id = NEW.usuario;
		END IF;
		
		IF NEW.usrs_debajo = 1 THEN
			UPDATE usuarios
			SET puntos = puntos + 5
			WHERE usuarios.id = NEW.usuario;
			
		ELSEIF NEW.usrs_debajo = 2 THEN
			UPDATE usuarios
			SET puntos = puntos + 10
			WHERE usuarios.id = NEW.usuario;
			
		ELSEIF NEW.usrs_debajo = 3 THEN
			UPDATE usuarios
			SET puntos = puntos + 20
			WHERE usuarios.id = NEW.usuario;
		END IF;
		
		RETURN NULL;
	END;
$estadisticas_trigger$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS estadisticas_trigger on ha_jugado;

CREATE TRIGGER estadisticas_trigger AFTER INSERT
ON ha_jugado FOR EACH ROW
EXECUTE PROCEDURE estadisticas_trigger();

