package es.unizar.unoforall.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.UUID;

import es.unizar.unoforall.model.PartidasAcabadasVO;
import es.unizar.unoforall.model.partidas.HaJugadoVO;
import es.unizar.unoforall.model.partidas.ListaPartidas;
import es.unizar.unoforall.model.partidas.PartidaJugada;

public class PartidasDAO {

	/**
	 * Método para obtener las partidas acabadas en las que ha participado
	 * un usuario.
	 * @param idUsuario		identificador del usuario en la base de datos.
	 * @return 				Una lista de elementos Partida.
	 */
	public static ListaPartidas getPartidas(UUID idUsuario) {
		ListaPartidas lp = new ListaPartidas(false);
		ArrayList<PartidaJugada> partidas = new ArrayList<PartidaJugada>();
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
					partidas.add(new PartidaJugada(new PartidasAcabadasVO((UUID)rs3.getObject("id"),
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
	
	/**
	 * Método para insertar una nueva partida acabada y sus participantes.
	 * @param partida		contiene los datos de la partida a insertar y de sus participantes.
	 * @return 				"nulo" si no hay problemas, y un mensaje de error en caso de que los
	 * 						haya.
	 */
	public static String insertarPartidaAcabada(PartidaJugada partida) {
		String error = "nulo";
		Connection conn = null;
		
		try {
			conn = GestorPoolConexionesBD.getConnection();
			
			PreparedStatement insertarPartida = 
					conn.prepareStatement("Insert Into partidas_acabadas Values(?,?,?,?,?);");
			insertarPartida.setObject(1, partida.getPartida().getId());
			insertarPartida.setDate(2, partida.getPartida().getFechaInicioPartida());
			insertarPartida.setDate(3, partida.getPartida().getFechaFinPartida());
			insertarPartida.setInt(4, partida.getPartida().getNumIas());
			insertarPartida.setInt(5, partida.getPartida().getModoJuego());
			
			int rows = insertarPartida.executeUpdate();
			if(rows==1) {
				int numInsertados=0;
				for(HaJugadoVO j : partida.getParticipantes()) {
					PreparedStatement insertarParticipante = 
							conn.prepareStatement("Insert Into ha_jugado Values(?,?,?,?);");
					insertarParticipante.setObject(1, j.getUsuario());
					insertarParticipante.setObject(2, j.getPartida());
					insertarParticipante.setInt(3, j.getUsrsDebajo());
					insertarParticipante.setBoolean(4, j.isHaGanado());
					rows = insertarParticipante.executeUpdate();
					if(rows!=1) {
						error = "Ha habido un problema al insertar el jugado de UUID <"+j.getUsuario()+
								"> en la tabla ha_jugado. Se han insertado <"+numInsertados+"> participantes";
						break;
					}
					numInsertados++;
				}
			} else {
				error = "No se ha podido insertar la partida de UUID <"+partida.getPartida().getId()+
																					 "> en la base de datos.";
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			error = "Ha surgido un problema con la base de datos.";
		}finally { 
			GestorPoolConexionesBD.releaseConnection(conn);
		}
		
		return error;
	}
}
