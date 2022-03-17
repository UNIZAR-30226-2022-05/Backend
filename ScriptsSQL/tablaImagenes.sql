CREATE TABLE IF NOT EXISTS imagenes(
			nota UUID,
			num_img INT NOT NULL,
			datos BYTEA NOT NULL,
			FOREIGN KEY(nota) REFERENCES notas(id_nota) ON DELETE CASCADE,
			PRIMARY KEY(nota, num_img));

DROP TABLE IF EXISTS imagenes CASCADE;


-- Actualizar tamaño de las notas normales al modificar sus imágenes
CREATE OR REPLACE FUNCTION imagenes_size_trigger() RETURNS TRIGGER AS $imagenes_size_trigger$
	DECLARE
	BEGIN
		IF TG_OP = 'INSERT' THEN
			UPDATE notas_g
			SET tamagno=tamagno + (SELECT LENGTH(NEW.datos))
			WHERE NEW.id_nota=notas_g.id_nota;
		ELSEIF TG_OP = 'UPDATE' THEN
			UPDATE notas_g
			SET tamagno=tamagno - (SELECT LENGTH(OLD.datos)) + (SELECT LENGTH(NEW.datos))
			WHERE NEW.id_nota=notas_g.id_nota;
		ELSE
			UPDATE notas_g
			SET tamagno=tamagno - (SELECT LENGTH(OLD.datos))
			WHERE OLD.id_nota=notas_g.id_nota;
		END IF;
		RETURN NULL;
	END;
$imagenes_size_trigger$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS imagenes_size_trigger on imagenes;

CREATE TRIGGER imagenes_size_trigger AFTER INSERT OR UPDATE OR DELETE
ON imagenes FOR EACH ROW
EXECUTE PROCEDURE imagenes_size_trigger();


public static List<byte[]> getImagenesNota(NotaVO nota){
		List<byte[]> result = new ArrayList<>();
		Connection conn = null;
		
		try {
			conn = ConnectionManager.getConnection();
			
			PreparedStatement selectImagenes = conn.prepareStatement("SELECT * FROM imagenes WHERE nota = ? ORDER BY num_img;");
			selectImagenes.setObject(1, nota.getIdNota());
			
			ResultSet rs = selectImagenes.executeQuery();
				
			while(rs.next()) {
				byte[] datos = rs.getBytes("datos");					
				result.add(datos);
			}
			
		}catch(Exception ex) {
			ex.printStackTrace();
		}finally {
			ConnectionManager.releaseConnection(conn);
		}
		
		return result;
		
	}
