CREATE INDEX usuarios_correo_index ON usuarios(correo);
CREATE INDEX usuarios_es_admin_index ON usuarios(es_admin);

CREATE INDEX notas_g_proyecto_index ON notas_g(proyecto);
CREATE INDEX notas_g_usuario_index ON notas_g(usuario);

CREATE INDEX subtareas_tarea_index ON subtareas(tarea);
