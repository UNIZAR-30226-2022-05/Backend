CREATE INDEX usuarios_correo_index ON usuarios(correo);
CREATE INDEX usuarios_nombre_index ON usuarios(nombre);

CREATE INDEX partidas_fecha_index ON partidas_acabadas(fecha_inicio_partida);

CREATE INDEX amigo_emisor_index ON amigo_de(emisor);
CREATE INDEX amigo_receptor_index ON amigo_de(receptor);

CREATE INDEX miembro_usuario_index ON es_miembro(usuario);
CREATE INDEX miembro_sala_index ON es_miembro(sala);
