/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package capaccesscontrol.packet;

import capaccesscontrol.ui.CAPScreen;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author swaplicado
 */
public class CAPhilo extends Thread{
    @Override
    public void run() {
        URL url;
            try {
                url = new URL("http://localhost/conexionPuerta.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream puerta = connection.getInputStream();
            } catch (MalformedURLException ex) {
                Logger.getLogger(CAPScreen.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(CAPScreen.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
}
