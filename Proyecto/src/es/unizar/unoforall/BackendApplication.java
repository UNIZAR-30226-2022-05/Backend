package es.unizar.unoforall;

import es.unizar.unoforall.apirest.ApiRestController;
import es.unizar.unoforall.db.GestorPoolConexionesBD;
import es.unizar.unoforall.gestores.GestorSesiones;
import es.unizar.unoforall.sockets.SocketController;
import me.i2000c.web_utils.controllers.HttpManager;
import me.i2000c.web_utils.multicast_utils.MulticastServer;

public class BackendApplication{
    public static void main(String[] args) {
        GestorPoolConexionesBD.inicializarPool();
        SocketController socketController = new SocketController();
        HttpManager.getManager().register(socketController);
        HttpManager.getManager().register(new ApiRestController(socketController));        
        HttpManager.getManager().start();
        GestorSesiones.inicializar();

        MulticastServer server = new MulticastServer(80);
        server.start();
    }
}