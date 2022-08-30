/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package capaccesscontrol;

import capaccesscontrol.config.CAPConfig;
import capaccesscontrol.config.CAPConfigReader;
import capaccesscontrol.core.CAPDigitalPersona;
import capaccesscontrol.ui.CAPScreen;
import capaccesscontrol.ui.CAPScreenNV;
import javax.swing.JFrame;

/**
 *
 * @author Edwin Carmona
 */
public class CAPAccessControl {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        CAPConfigReader cr = new CAPConfigReader();
        CAPConfig config = cr.readConfig();
//        
//        CAPMainUI ui = new CAPMainUI(config);

//        ui.setSize(960, 620);
//        ui.setResizable(false);
//        ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        ui.setLocationRelativeTo(null);
//        
//        CAPDigitalPersona obj = new CAPDigitalPersona(ui);
//        
//        ui.setVisible(true);
        
//        CAPScreen ui2 = new CAPScreen(config);
//        ui2.setTitle("CAP Access Control - " + config.getCompanyData().getCompanyName());
//        ui2.setSize(1080, 620);
//        ui2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        ui2.setLocationRelativeTo(null);
//        
//        CAPDigitalPersona obj = new CAPDigitalPersona(ui2);
//        
//        ui2.setVisible(true);
        
        CAPScreenNV ui2 = new CAPScreenNV(config);
        ui2.setTitle("CAP Access Control - " + config.getCompanyData().getCompanyName());
        ui2.setSize(1080, 720);
        ui2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ui2.setLocationRelativeTo(null);
        
        CAPDigitalPersona obj = new CAPDigitalPersona(ui2);
        
        ui2.setVisible(true);
    }
    
}
