INSERT INTO usuarios VALUES('4c2a49ed-48be-4970-9010-edb1faf918f1', 'prueba.info@gmail.com', 'asdfasdf', 'Ramón', 0, 0, 0);
INSERT INTO usuarios VALUES('53c8cf4a-adbf-11ec-b909-0242ac120002', 'prueba2@gmail.com', '1234', 'Pepe', 0, 0, 0);
INSERT INTO usuarios VALUES('7b0021c2-adff-11ec-b909-0242ac120002', 'c', 'cc', 'Pepe', 0, 0, 0);

INSERT INTO partidas_acabadas VALUES('4c2a49ed-48be-4970-9010-edb1faf918f1', TO_DATE('20170103','YYYYMMDD'), TO_DATE('20170103','YYYYMMDD'), 1, 1);
INSERT INTO ha_jugado VALUES('4c2a49ed-48be-4970-9010-edb1faf918f1', '4c2a49ed-48be-4970-9010-edb1faf918f1', 2, true);

-- lo anterior es para el modelo viejo

INSERT INTO usuarios VALUES('9661b426-b9e4-11ec-8422-0242ac120002', 'correo', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'Pepe', 0, 0, 0, 0, 0, 0); --clave: 1234