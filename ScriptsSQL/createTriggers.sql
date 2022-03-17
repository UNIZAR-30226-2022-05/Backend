CREATE OR REPLACE FUNCTION alertas_trigger() RETURNS TRIGGER AS $alertas_trigger$
	DECLARE
	BEGIN
		IF EXISTS (SELECT es_miembro.usuario
					FROM es_miembro, notas_g 
					WHERE es_miembro.proyecto=notas_g.proyecto
							AND notas_g.id_nota=NEW.tarea
							AND es_miembro.usuario=NEW.usuario)	THEN
			RETURN NEW;
		ELSE
			RETURN NULL;
		END IF;
	END;
$alertas_trigger$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS alertas_trigger on alertas;

CREATE TRIGGER alertas_trigger BEFORE INSERT OR UPDATE
ON alertas FOR EACH ROW
EXECUTE PROCEDURE alertas_trigger();

----------------------------------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION subtareas_trigger() RETURNS TRIGGER AS $subtareas_trigger$
	DECLARE
	BEGIN
		IF TG_OP = 'DELETE' THEN
			IF NOT EXISTS (SELECT * FROM subtareas
						    WHERE OLD.tarea=subtareas.tarea
								AND completada=FALSE) THEN
				UPDATE tareas SET completada=TRUE WHERE id_nota=OLD.tarea;
			END IF;
		ELSEIF NEW.completada=TRUE THEN
			IF NOT EXISTS (SELECT * FROM subtareas
						    WHERE NEW.tarea=subtareas.tarea
								AND completada=FALSE) THEN
				UPDATE tareas SET completada=TRUE WHERE id_nota=NEW.tarea;
			END IF;
		ELSE
			UPDATE tareas SET completada=FALSE WHERE id_nota=NEW.tarea;
		END IF;
		RETURN NULL;
	END;
$subtareas_trigger$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS subtareas_trigger on subtareas;

CREATE TRIGGER subtareas_trigger AFTER INSERT OR UPDATE OR DELETE
ON subtareas FOR EACH ROW
EXECUTE PROCEDURE subtareas_trigger();

----------------------------------------------------------------------------------------------------

-- Actualizar tamaño de las tareas al modificar subtareas
CREATE OR REPLACE FUNCTION subtareas_size_trigger() RETURNS TRIGGER AS $subtareas_size_trigger$
	DECLARE
	BEGIN
		IF TG_OP = 'INSERT' THEN
			UPDATE notas_g
			SET tamagno=tamagno + (SELECT LENGTH(NEW.texto))
			WHERE NEW.tarea=notas_g.id_nota;
		ELSEIF TG_OP = 'UPDATE' THEN
			UPDATE notas_g
			SET tamagno=tamagno - (SELECT LENGTH(OLD.texto)) + (SELECT LENGTH(NEW.texto))
			WHERE NEW.tarea=notas_g.id_nota;
		ELSE
			UPDATE notas_g
			SET tamagno=tamagno - (SELECT LENGTH(OLD.texto))
			WHERE OLD.tarea=notas_g.id_nota;
		END IF;
		RETURN NULL;
	END;
$subtareas_size_trigger$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS subtareas_size_trigger on subtareas;

CREATE TRIGGER subtareas_size_trigger AFTER INSERT OR UPDATE OR DELETE
ON subtareas FOR EACH ROW
EXECUTE PROCEDURE subtareas_size_trigger();

----------------------------------------------------------------------------------------------------

-- Actualizar tamaño de los proyectos al modificar cualquier tipo de nota o tarea
CREATE OR REPLACE FUNCTION notas_g_size_trigger() RETURNS TRIGGER AS $notas_g_size_trigger$
	DECLARE
	BEGIN
		IF TG_OP = 'INSERT' THEN
			UPDATE proyectos
			SET tamagno=tamagno + NEW.tamagno
			WHERE NEW.proyecto=proyectos.id_proyecto;
		ELSEIF TG_OP = 'UPDATE' THEN
			UPDATE proyectos
			SET tamagno=tamagno - OLD.tamagno + NEW.tamagno
			WHERE NEW.proyecto=proyectos.id_proyecto;
		ELSE
			UPDATE proyectos
			SET tamagno=tamagno - OLD.tamagno
			WHERE OLD.proyecto=proyectos.id_proyecto;
		END IF;
		RETURN NULL;
	END;
$notas_g_size_trigger$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS notas_g_size_trigger on notas_g;

CREATE TRIGGER notas_g_size_trigger AFTER INSERT OR UPDATE OR DELETE
ON notas_g FOR EACH ROW
EXECUTE PROCEDURE notas_g_size_trigger();

----------------------------------------------------------------------------------------------------

-- Actualizar tamaño de las notas normales al modificar su texto
CREATE OR REPLACE FUNCTION notas_size_trigger() RETURNS TRIGGER AS $notas_size_trigger$
	DECLARE
	BEGIN
		IF TG_OP = 'INSERT' THEN
			UPDATE notas_g
			SET tamagno=tamagno + (SELECT LENGTH(NEW.texto))
			WHERE NEW.id_nota=notas_g.id_nota;
		ELSEIF TG_OP = 'UPDATE' THEN
			raise notice 'Old: %', (SELECT LENGTH(OLD.texto));
			raise notice 'New: %', (SELECT LENGTH(NEW.texto));
			UPDATE notas_g
			SET tamagno=tamagno - (SELECT LENGTH(OLD.texto)) + (SELECT LENGTH(NEW.texto))
			WHERE NEW.id_nota=notas_g.id_nota;
		ELSE
			UPDATE notas_g
			SET tamagno=tamagno - (SELECT LENGTH(OLD.texto))
			WHERE OLD.id_nota=notas_g.id_nota;
		END IF;
		RETURN NULL;
	END;
$notas_size_trigger$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS notas_size_trigger on notas;

CREATE TRIGGER notas_size_trigger AFTER INSERT OR UPDATE OR DELETE
ON notas FOR EACH ROW
EXECUTE PROCEDURE notas_size_trigger();

