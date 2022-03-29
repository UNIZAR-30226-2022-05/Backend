package es.unizar.unoforall.apirest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.UUID;

import es.unizar.unoforall.db.GestorPoolConexionesBD;
import es.unizar.unoforall.model.HaJugadoVO;
import es.unizar.unoforall.model.PartidasAcabadasVO;

public class PartidasDAO {

	/**
	 * Método para obtener las partidas acabadas en las que ha participado
	 * un usuario.
	 * @param idUsuario		identificador del usuario en la base de datos.
	 * @return 				Una lista de elementos Partida.
	 */
	public static ListaPartidas getPartidas(UUID idUsuario) {
		ListaPartidas lp = new ListaPartidas(false);
		ArrayList<Partida> partidas = new ArrayList<Partida>();
		Connection conn = null;
		
		try {
			conn = GestorPoolConexionesBD.getConnection();
			
			PreparedStatement sacarPartidas = 
					conn.prepareStatement("SELECT * FROM ha_jugado WHERE usuario = ?;");
			sacarPartidas.setObject(1, idUsuario);
			
			ResultSet rs = sacarPartidas.executeQuery();
			while(rs.next()) { 				//Para cada partida
				//Saca los participantes
				PreparedStatement sacarParticipantes = 
						conn.prepareStatement("SELECT * FROM ha_jugado WHERE partida = ?;");
				sacarParticipantes.setObject(1,(UUID) rs.getObject("partida"));
				ResultSet rs2 = sacarParticipantes.executeQuery();
				ArrayList<HaJugadoVO> listaParticipantes = new ArrayList<HaJugadoVO>();
				while(rs2.next()) {
					listaParticipantes.add(new HaJugadoVO((UUID)rs2.getObject("usuario"),
								(UUID)rs2.getObject("partida"),rs2.getInt("usrs_debajo"),
															rs2.getBoolean("ha_ganado")));
				}
				//Saca los datos de la partida
				PreparedStatement sacarPartida = 
						conn.prepareStatement("SELECT * FROM partidas_acabadas WHERE id = ?;");
				sacarParticipantes.setObject(1,(UUID) rs.getObject("partida"));
				ResultSet rs3 = sacarPartida.executeQuery();
				if(rs3.next()) {
					partidas.add(new Partida(new PartidasAcabadasVO((UUID)rs3.getObject("id"),
							rs3.getDate("fecha_inicio_partida"),rs3.getDate("fecha_fin_partida"),
							rs3.getInt("num_ias"),rs3.getInt("modo_juego")),listaParticipantes));
				}
			}
			lp.setPartidas(partidas);
		}catch(Exception ex) {
			ex.printStackTrace();
			lp.setError("Ha surgido un problema con la base de datos.");
		}finally {
			GestorPoolConexionesBD.releaseConnection(conn);
		}
		
		return lp;
	}
}
