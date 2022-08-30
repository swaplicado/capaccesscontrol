/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package capaccesscontrol.ui;

import capaccesscontrol.config.CAPConfig;
import capaccesscontrol.db.CAPMySql;
import capaccesscontrol.db.CAPSiieDb;
import capaccesscontrol.packet.CAPEmployeeResponse;
import capaccesscontrol.packet.CAPRequest;
import capaccesscontrol.packet.CAPResponse;
import capaccesscontrol.packet.CAPhilo;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author Edwin Carmona
 */
public class CAPScreenNVResp extends javax.swing.JFrame implements ActionListener {
    
    private final CAPConfig oConfig;
    private CAPMySql oSiieMySql;
    private CAPMySql oCapMySql;
    private CAPRequest oCAPRequest;
    private Date tDate;
    private List<CAPEmployeeResponse> lEmployees;
    private CAPEmployeeUI oEmployeeUI;
    private List<CAPLogUI> lLog;
    private CAPLogUI oLog;
    private CAPSource oEnumSource;
    private CAPLogUIModel oModel;
    private boolean autorization;

    /**
     * Creates new form CAPScreen
     * @param cfg
     */
    public CAPScreenNVResp(CAPConfig cfg) {
        this.oConfig = cfg;
        initComponents();
        initCustom();
        autorization = false;
    }
    
    /**
     * Inicia objetos y componentes necesarios para el funcionamiento de la clase.
     */
    private void initCustom() {
        startClock();
        setImage();
        
        // Objeto para conexión a base de datos de SIIE.
        oSiieMySql = new CAPMySql(oConfig.getSiieConnection().getNameDb(), 
                                oConfig.getSiieConnection().getHostDb(),
                                oConfig.getSiieConnection().getPortDb(), 
                                oConfig.getSiieConnection().getUserDb(), 
                                oConfig.getSiieConnection().getPswdDb());
        
        // Objeto para conexión a base de datos de CAP.
        oCapMySql = new CAPMySql(oConfig.getCapConnection().getNameDb(), 
                                oConfig.getCapConnection().getHostDb(),
                                oConfig.getCapConnection().getPortDb(), 
                                oConfig.getCapConnection().getUserDb(), 
                                oConfig.getCapConnection().getPswdDb());
        
        // Objeto para realizar peticiones a CAP.
        oCAPRequest = new CAPRequest(oConfig.getMailCAP(), oConfig.getPswdCAP(), oConfig.getUrlLogin());
        
        resetFields();
        
        jlTitle.setText("<html><body><div>CAP Access Control " + oConfig.getCompanyData().getCompanyName() + "</div></body></html>");
        jlTitle.setFont(new Font("Verdana", Font.PLAIN, 16));
        
        jbSearch.addActionListener(this);
        jbShowLog.addActionListener(this);
        
        lLog = new ArrayList();
        
        disableSearchByEmployee();
        
        if (oCAPRequest.login() == -1) {
            System.exit(-1);
        }
        
        String LAF = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
        try {
            UIManager.setLookAndFeel(LAF);
        }
        catch (ClassNotFoundException ex) {
            Logger.getLogger(CAPScreenNVResp.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (InstantiationException ex) {
            Logger.getLogger(CAPScreenNVResp.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IllegalAccessException ex) {
            Logger.getLogger(CAPScreenNVResp.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(CAPScreenNVResp.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        SwingUtilities.updateComponentTreeUI(this);
    }
    
    /**
     * Reiniciar etiquetas y campos de texto.
     */
    public void resetFields() {
        jtfNumEmployee.setText("");
        jtfNumEmp.setText("");
        jtfNameEmp.setText("");
        jlReason.setText("");
        jlImgPhoto.setText("");
        javax.swing.ImageIcon photoIcon = new ImageIcon();
        jlImgPhoto.setIcon(photoIcon);
        jtfTimestamp.setText("");
    }
    
    /**
     * Este método inactiva la búsqueda por empleado.
     */
    private void disableSearchByEmployee() {
        jtfNumEmployee.requestFocusInWindow();
    }
    
    /**
     * Activar búsqueda de empleado.
     */
    private void enableSearchByEmployee() {
    }
    
    /**
     * Carga la imagen de la empresa configurada en el archivo cfg.json.
     */
    private void setImage() {
        jLImage.setIcon(new javax.swing.ImageIcon(oConfig.getCompanyData().getCompanyImage()));
        jLImage.setPreferredSize(new java.awt.Dimension(110, 110));
    }

    /**
     * Iniciar reloj y visualizar en pantalla.
     */
    private void startClock() {
        SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyy HH:mm:ss");
//        jlClock.setFont(new Font("Verdana", Font.BOLD, 16));
        
        javax.swing.Timer t = new javax.swing.Timer(1000,
                new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Calendar now = Calendar.getInstance();

                jlClock.setText("<html><body><div>" + format1.format(now.getTime()) + "</div></body></html>");
                jlClock.setHorizontalAlignment(jlClock.CENTER);
                // Center the text
//                jlClock.getCaret().setVisible(false);
                // Hide the Cursor in JTextField
            }
        });
        
        t.start();
    }
    
    /**
     * Buscar info de empleado por número de empleado.
     * Obtiene el número de empleado del jtfield
     */
    private void actionSearchByEmployeeNum() {
        String numEmp = jtfNumEmployee.getText();
        
        if (numEmp.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Debe introducir un número de empleado", "Eror", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (! this.isStringInt(numEmp)) {
            return;
        }
        
        tDate = new Date();
        
        oLog = new CAPLogUI();
        this.oEnumSource = CAPSource.CODIGO;
        CAPResponse response = oCAPRequest.requestByNumEmployee(tDate, numEmp, oConfig.getSearchScheduleDays(), oConfig.getMinPrevSchedule(), oConfig.getMinPostSchedule(), oConfig.getUrlNumEmployee());
        
        this.processCAPResponse(response);
    }
    
    /**
     * Activar búsqueda de empleados, solicitar empleados activos y no eliminados de CAP.
     */
    private void actionSearchByEmployee() {
        CAPResponse response = oCAPRequest.requestEmployees(oConfig.getUrlGetEmployees());
        
        if (response.getEmployees() == null) {
            return;
        }
        
        enableSearchByEmployee();
        
        lEmployees = response.getEmployees();
    }
    
    /**
     * Obtener empleado seleccionado y solicitar búsqueda por id de empleado.
     */
    private void actionSearchSelectedEmployee() {
        
        actionSearchEmployeeById(oEmployeeUI.getIdEmployee());
        this.oEnumSource = CAPSource.NOMBRE;
    }
    
    /**
     * Búsqueda de empleado por id.
     * Realiza la petición al servidor de CAP
     * 
     * @param employeeId 
     */
    public void actionSearchEmployeeById(int employeeId) {
        tDate = new Date();
        oLog = new CAPLogUI();
        
        CAPResponse response = oCAPRequest.requestByIdEmployee(tDate, employeeId, oConfig.getSearchScheduleDays(), oConfig.getMinPrevSchedule(), oConfig.getMinPostSchedule(), oConfig.getUrlIdEmployee());
        this.processCAPResponse(response);
        
        DefaultListModel model = new DefaultListModel();
        disableSearchByEmployee();
    }
    
    /**
     * Procesar respuesta del servidor.
     * Este método valida los datos de la respuesta y delega a los siguientes métodos
     * para mostrar info de empleado y si se le permite o no la entrada a la empresa
     * 
     * @param response
     */
    private void processCAPResponse(CAPResponse response) {
        resetFields();
        
        if (! validateResponse(response)) {
            return;
        }
        
        /**
         * si la respuesta del servidor es válida muestra la foto del empleado que requiere acceso
         */
        ImageIcon icon = CAPSiieDb.getPhoto(oSiieMySql.connectMySQL(), response.getEmployee().getExternal_id());
        oLog.setPhoto(icon);
        showPhoto(icon);
        oLog.setTimeStamp(tDate);
        showTimestamp(tDate);
        
        // se muestra la info del empleado
        this.showData(response.getEmployee().getNum_employee(), response.getEmployee().getName());
        oLog.setNumEmployee(response.getEmployee().getNum_employee());
        oLog.setNameEmployee(response.getEmployee().getName());
        oLog.setNameEmpJob(response.getEmployee().getJob_name());
        oLog.setNameEmpDept(response.getEmployee().getDept_name());
        
        oLog.setSource(oEnumSource);
        
        // se valida el acceso mediante la respuesta del servidor
        boolean respuesta = validateAccess(response);
    }
    
    /**
     * Devuelve true si la cadena recibida es un entero.
     * 
     * @param s
     * @return
     */
    private boolean isStringInt(String s)
    {
        try
        {
            Integer.parseInt(s);
            return true;
        }
        catch (NumberFormatException ex) {
            return false;
        }
    }
    
    /**
     * Muestra en pantalla la imagen del empleado recibida.
     * 
     * @param photoIcon 
     */
    private void showPhoto(javax.swing.ImageIcon photoIcon) {
        jlImgPhoto.setIcon(photoIcon);
    }
    
    /**
     * Muestra en pantalla número y nombre de empleado.
     * 
     * @param numEmployee
     * @param nameEmployee 
     */
    private void showData(String numEmployee, String nameEmployee) {
        jtfNumEmp.setText(numEmployee);
        jtfNameEmp.setText(nameEmployee);
    }
    
    /**
     * Muestra en pantalla el timestamp de la fecha y hora de consulta.
     * @param dtDate 
     */
    private void showTimestamp(Date dtDate) {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String sDate = df.format(dtDate);
        
        jtfTimestamp.setText(sDate);
    }
    
    /**
     * Validación de respuesta del servidor
     * 
     * @param response
     * @return 
     */
    private boolean validateResponse(CAPResponse response) {
        if (response.getEmployee() == null) {
            showUnauthorized("No se encontró al empleado en el sistema", "", "", false, false);
            return false;
        }
        
        return true;
    }
    
    /**
     * Valida si el empleado tiene o no permitido el acceso.
     * 
     * Para que el empleado se le permita ingresar no debe estar desactivado o eliminado, 
     * no debe de tener programada ninguna incidencia, debe tener un horario programado para
     * el día en cuestión y además debe estar dentro de este.
     * 
     * @param response
     * @return 
     */
    private boolean validateAccess(CAPResponse response) {
        if (response.isAuthorized()) {
            // si el acceso al empleado es permitido se muestra la autorización y se escribe el suceso en la biácora
            this.showAutorized(response.getSchedule().getInDateTimeSch(), response.getSchedule().getOutDateTimeSch(), false);
            oLog.setReasons(response.getMessage());
            oLog.setAuthorized(true);
        }
        else {
            String sIn = "";
            String sOut = "";
            boolean isNext = false;
            if (response.getSchedule() != null) {
                isNext = false;
                sIn = response.getSchedule().getInDateTimeSch();
                sOut = response.getSchedule().getOutDateTimeSch();
            }
            else {
                if (response.getNextSchedule() != null) {
                    sIn = response.getNextSchedule().getInDateTimeSch();
                    sOut = response.getNextSchedule().getOutDateTimeSch();
                }
            }
            
            showUnauthorized(response.getMessage(), sIn, sOut, isNext, false);
            oLog.setReasons(response.getMessage());
            oLog.setAuthorized(false);
        }
        
        lLog.add(0, oLog);
        updateTableLog();
        oEnumSource = null;
        
        return response.isAuthorized();
    }
    
    /**
     * Mostrar acceso denegado.
     * 
     * @param reason
     * @param scheduleIn
     * @param scheduleOut
     * @param isNext 
     * @param fromLog 
     */
    public void showUnauthorized(String reason, String scheduleIn, String scheduleOut, boolean isNext, boolean fromLog) {
        String text = "<html>"
                        + "<body style='text-align: center; background-color: red;'>"
                            + "<div style='height: 180px; width: 250px; top: 50%;'><br><br>ACCESO<br>DENEGADO</div>"
                        + "</body>"
                    + "</html>";
        
//        jlResultMessage.setText(text);
//        jlResultMessage.setFont(new Font("Verdana", Font.PLAIN, 32));
        
//        String textReason = "<html>"
//                        + "<body style='text-align: center;'>"
//                            + "<div style='height: 100%; width: 100%;'><p>" + reason + "</p></div>"
//                        + "</body>"
//                    + "</html>";
        
        String textReason = reason;
        
        jlReason.setText(textReason);
        
        if (! isNext) {
            jlIn.setText("Horario entrada:");
            jlOut.setText("Horario salida:");
        }
        else {
            jlIn.setText("Próxima entrada:");
            jlOut.setText("Próxima salida:");
        }
        
        String schIn = "";
        String schOut = "";
        
        if (! scheduleIn.isEmpty()) {
            schIn = this.formatStringDate(scheduleIn);
            schOut = this.formatStringDate(scheduleOut);
        }
        else if (fromLog) {
            schIn = "N/A";
            schOut = "N/A";
        }
        else {
            schIn = "Sin horario";
            schOut = "Sin horario";
        }
        
        jtfScheduleIn.setText(schIn);
        jtfScheduleOut.setText(schOut);
        
        jlUnauthorized.setIcon(new javax.swing.ImageIcon("img/no_auth.png"));
        jlUnauthorized.setPreferredSize(new java.awt.Dimension(200, 200));
    }
    
    /**
     * Mostrar autorización.
     * 
     * @param scheduleIn
     * @param scheduleOut 
     */
    private void showAutorized(String scheduleIn, String scheduleOut, boolean fromLog) {
        String text = "<html>"
                        + "<body style='text-align: center; background-color: green;'>"
                            + "<div style='height: 180px; width: 250px; top: 50%;'><br><br>ACCESO<br>AUTORIZADO</div>"
                        + "</body>"
                    + "</html>";
        
//        jlResultMessage.setText(text);
//        jlResultMessage.setFont(new Font("Verdana", Font.PLAIN, 32));
        
        jlIn.setText("Horario entrada:");
        jlOut.setText("Horario salida:");
        
        String schIn = "";
        String schOut = "";
        
        if (! scheduleIn.isEmpty()) {
            schIn = this.formatStringDate(scheduleIn);
            schOut = this.formatStringDate(scheduleOut);
        }
        else if (fromLog) {
            schIn = "N/A";
            schOut = "N/A";
        }
        else {
            schIn = "Sin horario";
            schOut = "Sin horario";
        }
        
        jtfScheduleIn.setText(schIn);
        jtfScheduleOut.setText(schOut);
        
        jlAuthorized.setIcon(new javax.swing.ImageIcon("img/auth.png"));
        jlAuthorized.setPreferredSize(new java.awt.Dimension(200, 200));
        
        CAPhilo hilo = new CAPhilo();
        hilo.start();
    }
    
    /**
     * Mostrar valor seleccionado de la bitácora.
     */
    private void actionShowLog() {
        showLog();
    }
    
    /**
     * Actualizar bitácora.
     */
    private void updateTableLog() {
//        CAPLogUIModel model = (CAPLogUIModel) jTableLog.getModel();
        CAPLogUIModel model = new CAPLogUIModel();
        
        if (oConfig.getCountLog() < lLog.size()) {
            lLog.remove(lLog.size() - 1);
        }
        
        model.setlModelLog(lLog);
        model.fireTableDataChanged();
    }
    
    /**
     * Mostrar datos de bitácora.
     */
    private void showLog() {
        resetFields();
        
        showPhoto(oLog.getPhoto());
        showData(oLog.getNumEmployee(), oLog.getNameEmployee());
        showTimestamp(oLog.getTimeStamp());
        
        if (oLog.isAuthorized()) {
            showAutorized("", "", true);
        }
        else {
            showUnauthorized(oLog.getReasons(), "", "", false, true);
        }
        
        jtfNumEmployee.requestFocusInWindow();
    }
    
    /**
     * Formato de string yyyy-MM-dd HH:mm a dd/MM/yyy HH:mm:ss.
     * 
     * @param dtDate
     * @return 
     */
    private String formatStringDate(String dtDate) {
        SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyy HH:mm:ss");
        
        try {  
            Date date1 = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(dtDate);
            
            return format1.format(date1);
        } catch (ParseException ex) {
            Logger.getLogger(CAPScreenNVResp.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return "";
    }
    
    @Override
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() instanceof JButton) {
            JButton button = (JButton) evt.getSource();

            if (button == jbSearch) {
                actionSearchByEmployeeNum();
            }
            else if (button == jbShowLog) {
                actionShowLog();
            }
        }
    }

    public void setoEnumSource(CAPSource oEnumSource) {
        this.oEnumSource = oEnumSource;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel32 = new javax.swing.JPanel();
        jPanel36 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jtfNumEmployee = new sa.lib.gui.bean.SBeanFieldText();
        jbSearch = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLImage = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jlTitle = new javax.swing.JLabel();
        jlClock = new javax.swing.JLabel();
        jPanel33 = new javax.swing.JPanel();
        jPanel34 = new javax.swing.JPanel();
        jlImgPhoto = new javax.swing.JLabel();
        jbShowLog = new javax.swing.JButton();
        jPanel35 = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        jlAuthorized = new javax.swing.JLabel();
        jlUnauthorized = new javax.swing.JLabel();
        jPanel11 = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        jPanel18 = new javax.swing.JPanel();
        jtfTimestamp = new sa.lib.gui.bean.SBeanFieldText();
        jPanel19 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jtfNameEmp = new sa.lib.gui.bean.SBeanFieldText();
        jPanel20 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jtfNumEmp = new sa.lib.gui.bean.SBeanFieldText();
        jlJob = new javax.swing.JLabel();
        jtfNumEmp1 = new sa.lib.gui.bean.SBeanFieldText();
        jPanel22 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        sBeanFieldText17 = new sa.lib.gui.bean.SBeanFieldText();
        jPanel21 = new javax.swing.JPanel();
        jlIn = new javax.swing.JLabel();
        jtfScheduleIn = new sa.lib.gui.bean.SBeanFieldText();
        jlOut = new javax.swing.JLabel();
        jtfScheduleOut = new sa.lib.gui.bean.SBeanFieldText();
        jPanel10 = new javax.swing.JPanel();
        jlReason = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel32.setLayout(new java.awt.BorderLayout());

        jPanel36.setLayout(new java.awt.BorderLayout());

        jPanel7.setLayout(new java.awt.GridLayout(2, 1));

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("Consulte empleado:");
        jLabel4.setPreferredSize(new java.awt.Dimension(700, 23));
        jPanel8.add(jLabel4);

        jPanel7.add(jPanel8);

        jLabel7.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel7.setText("Por número:");
        jPanel9.add(jLabel7);

        jtfNumEmployee.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jtfNumEmployee.setPreferredSize(new java.awt.Dimension(300, 23));
        jPanel9.add(jtfNumEmployee);

        jbSearch.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jbSearch.setText("Buscar");
        jPanel9.add(jbSearch);

        jPanel7.add(jPanel9);

        jPanel4.add(jPanel7);

        jPanel36.add(jPanel4, java.awt.BorderLayout.PAGE_END);

        jLabel6.setPreferredSize(new java.awt.Dimension(100, 23));
        jPanel5.add(jLabel6);

        jLImage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLImage.setToolTipText("Foto (tamaño sugerido: 100×100 px)");
        jLImage.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLImage.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLImage.setPreferredSize(new java.awt.Dimension(120, 120));
        jPanel5.add(jLImage);

        jPanel3.add(jPanel5);

        jPanel6.setLayout(new java.awt.GridLayout(2, 1, 0, 10));

        jlTitle.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jlTitle.setText("CAP Control de Acceso");
        jlTitle.setPreferredSize(new java.awt.Dimension(400, 23));
        jPanel6.add(jlTitle);

        jlClock.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jPanel6.add(jlClock);

        jPanel3.add(jPanel6);

        jPanel36.add(jPanel3, java.awt.BorderLayout.PAGE_START);

        jPanel32.add(jPanel36, java.awt.BorderLayout.PAGE_START);

        jPanel2.add(jPanel32);

        jPanel34.setLayout(new java.awt.BorderLayout());

        jlImgPhoto.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jlImgPhoto.setText("Foto");
        jlImgPhoto.setToolTipText("Foto (tamaño sugerido: 100×100 px)");
        jlImgPhoto.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jlImgPhoto.setPreferredSize(new java.awt.Dimension(300, 300));
        jPanel34.add(jlImgPhoto, java.awt.BorderLayout.CENTER);

        jbShowLog.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jbShowLog.setText("Ver bitácora");
        jPanel34.add(jbShowLog, java.awt.BorderLayout.PAGE_END);

        jPanel33.add(jPanel34);

        jPanel35.setLayout(new java.awt.BorderLayout());

        jlAuthorized.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jlAuthorized.setToolTipText("Foto (tamaño sugerido: 100×100 px)");
        jlAuthorized.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jlAuthorized.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jlAuthorized.setPreferredSize(new java.awt.Dimension(200, 200));
        jPanel12.add(jlAuthorized);

        jlUnauthorized.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jlUnauthorized.setToolTipText("Foto (tamaño sugerido: 100×100 px)");
        jlUnauthorized.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jlUnauthorized.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jlUnauthorized.setPreferredSize(new java.awt.Dimension(200, 200));
        jPanel12.add(jlUnauthorized);

        jPanel35.add(jPanel12, java.awt.BorderLayout.CENTER);

        jPanel14.setLayout(new java.awt.GridLayout(6, 1));

        jtfTimestamp.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jtfTimestamp.setText("23/08/2022 09:20 am");
        jtfTimestamp.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jtfTimestamp.setPreferredSize(new java.awt.Dimension(250, 23));
        jPanel18.add(jtfTimestamp);

        jPanel14.add(jPanel18);

        jLabel5.setText("Nombre:");
        jPanel19.add(jLabel5);

        jtfNameEmp.setText("NOMBRE DEL EMPLEADO QUE CHECÓ");
        jtfNameEmp.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jtfNameEmp.setPreferredSize(new java.awt.Dimension(500, 23));
        jPanel19.add(jtfNameEmp);

        jPanel14.add(jPanel19);

        jLabel9.setText("Número de empleado:");
        jPanel20.add(jLabel9);

        jtfNumEmp.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jtfNumEmp.setText("990");
        jtfNumEmp.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jPanel20.add(jtfNumEmp);

        jlJob.setText("Puesto:");
        jPanel20.add(jlJob);

        jtfNumEmp1.setText("Puesto");
        jtfNumEmp1.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jtfNumEmp1.setPreferredSize(new java.awt.Dimension(300, 23));
        jPanel20.add(jtfNumEmp1);

        jPanel14.add(jPanel20);

        jLabel8.setText("Departamento");
        jPanel22.add(jLabel8);

        sBeanFieldText17.setText("DEPARTAMENTO DEL EMPLEADO");
        sBeanFieldText17.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        sBeanFieldText17.setPreferredSize(new java.awt.Dimension(500, 23));
        jPanel22.add(sBeanFieldText17);

        jPanel14.add(jPanel22);

        jlIn.setText("Entrada:");
        jPanel21.add(jlIn);

        jtfScheduleIn.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jtfScheduleIn.setText("23/08/2022 09:20 am");
        jtfScheduleIn.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jtfScheduleIn.setPreferredSize(new java.awt.Dimension(175, 23));
        jPanel21.add(jtfScheduleIn);

        jlOut.setText("Salida:");
        jPanel21.add(jlOut);

        jtfScheduleOut.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jtfScheduleOut.setText("23/08/2022 09:20 am");
        jtfScheduleOut.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jtfScheduleOut.setPreferredSize(new java.awt.Dimension(175, 23));
        jPanel21.add(jtfScheduleOut);

        jPanel14.add(jPanel21);

        jlReason.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jlReason.setForeground(new java.awt.Color(204, 0, 0));
        jlReason.setPreferredSize(new java.awt.Dimension(500, 23));
        jPanel10.add(jlReason);

        jPanel14.add(jPanel10);

        jPanel11.add(jPanel14);

        jPanel35.add(jPanel11, java.awt.BorderLayout.PAGE_START);

        jPanel33.add(jPanel35);

        jPanel2.add(jPanel33);

        jPanel1.add(jPanel2, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLImage;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel32;
    private javax.swing.JPanel jPanel33;
    private javax.swing.JPanel jPanel34;
    private javax.swing.JPanel jPanel35;
    private javax.swing.JPanel jPanel36;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JButton jbSearch;
    private javax.swing.JButton jbShowLog;
    private javax.swing.JLabel jlAuthorized;
    private javax.swing.JLabel jlClock;
    private javax.swing.JLabel jlImgPhoto;
    private javax.swing.JLabel jlIn;
    private javax.swing.JLabel jlJob;
    private javax.swing.JLabel jlOut;
    private javax.swing.JLabel jlReason;
    private javax.swing.JLabel jlTitle;
    private javax.swing.JLabel jlUnauthorized;
    private sa.lib.gui.bean.SBeanFieldText jtfNameEmp;
    private sa.lib.gui.bean.SBeanFieldText jtfNumEmp;
    private sa.lib.gui.bean.SBeanFieldText jtfNumEmp1;
    private sa.lib.gui.bean.SBeanFieldText jtfNumEmployee;
    private sa.lib.gui.bean.SBeanFieldText jtfScheduleIn;
    private sa.lib.gui.bean.SBeanFieldText jtfScheduleOut;
    private sa.lib.gui.bean.SBeanFieldText jtfTimestamp;
    private sa.lib.gui.bean.SBeanFieldText sBeanFieldText17;
    // End of variables declaration//GEN-END:variables
}
