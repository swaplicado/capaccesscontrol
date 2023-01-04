/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package capaccesscontrol.ui;

import capaccesscontrol.config.CAPConfig;
import capaccesscontrol.db.CAPMySql;
import capaccesscontrol.db.CAPSiieDb;
import capaccesscontrol.packet.CAPRequest;
import capaccesscontrol.packet.CAPResponse;
import capaccesscontrol.packet.CAPhilo;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class CAPScreenNV extends javax.swing.JFrame implements ActionListener {
    
    private final CAPConfig oConfig;
    private CAPMySql oSiieMySql;
    private CAPRequest oCAPRequest;
    private Date tDate;
    private CAPLogUI oLog;
    private CAPSource oEnumSource;
    private java.util.Timer timer;
    private CAPScreenLog moScreenLog;
    private boolean autorization;

    /**
     * Creates new form CAPScreen
     * @param cfg
     */
    public CAPScreenNV(CAPConfig cfg) {
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
        
        // Objeto para realizar peticiones a CAP.
        oCAPRequest = new CAPRequest(oConfig.getMailCAP(), oConfig.getPswdCAP(), oConfig.getUrlLogin());
        
        resetFields();
        
        jlTitle.setText("<html>"
                        + "<body>"
                            + "<div><b>CAP Access Control " + oConfig.getCompanyData().getCompanyName() + "</b>"
                            + "</div>"
                        + "</body>"
                    + "</html>");
        
        jlTitle.setFont(new Font("Verdana", Font.PLAIN, 18));
        
        jbSearch.addActionListener(this);
        jbShowLog.addActionListener(this);
        jbShowLog2.addActionListener(this);
        
        moScreenLog = new CAPScreenLog();
        
        disableSearchByEmployee();
        
        if (oCAPRequest.login() == -1) {
            System.exit(-1);
        }
        
        String LAF = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
        try {
            UIManager.setLookAndFeel(LAF);
        }
        catch (ClassNotFoundException ex) {
            Logger.getLogger(CAPScreenNV.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (InstantiationException ex) {
            Logger.getLogger(CAPScreenNV.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IllegalAccessException ex) {
            Logger.getLogger(CAPScreenNV.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(CAPScreenNV.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        SwingUtilities.updateComponentTreeUI(this);
        setWelcomeImage();
        jPAccess.remove(jPWelcome);
        
        showWelcome();
    }
    
    /**
     * Reiniciar etiquetas y campos de texto.
     */
    public void resetFields() {
        moTextSearchNum.setValue("");
        moTextNumEmp.setValue("");
        moTextNameEmp.setValue("");
        moTextJob.setValue("");
        moTextDepartment.setValue("");
        jlReason.setText("");
        jlImgPhoto.setText("");
        moTextScheduleIn.setValue("");
        moTextScheduleOut.setValue("");
        moTextTimestamp.setValue("");
        
        javax.swing.ImageIcon photoIcon = new ImageIcon();
        jlImgPhoto.setIcon(photoIcon);
        jlAuthorized.setIcon(photoIcon);
        jlUnauthorized.setIcon(photoIcon);
    }
    
    /**
     * Este método inactiva la búsqueda por empleado.
     */
    private void disableSearchByEmployee() {
        moTextSearchNum.requestFocusInWindow();
    }
    
    /**
     * Carga la imagen de la empresa configurada en el archivo cfg.json.
     */
    private void setImage() {
        jLImage.setIcon(new javax.swing.ImageIcon(oConfig.getCompanyData().getCompanyImage()));
        jLImage.setPreferredSize(new java.awt.Dimension(120, 120));
    }
    
    /**
     * Carga la imagen de la empresa configurada en el archivo cfg.json.
     */
    private void setWelcomeImage() {
        jlWelcomeImg.setIcon(new javax.swing.ImageIcon(oConfig.getCompanyData().getWelcomeImage()));
        jlWelcomeImg.setPreferredSize(new java.awt.Dimension(500, 300));
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
    
    private void showWelcome() {
        jPAccess.remove(jPphoto);
        jPAccess.remove(jPdata);
        jPAccess.add(jPWelcome);
    }
    
    private void showAccess() {
        jPAccess.remove(jPWelcome);
        jPAccess.add(jPphoto);
        jPAccess.add(jPdata);
    }
    
    /**
     * Buscar info de empleado por número de empleado.
     * Obtiene el número de empleado del jtfield
     */
    private void actionSearchByEmployeeNum() {
        String numEmp = moTextSearchNum.getText();
        
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
        showAccess();
        
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
        this.showData(response.getEmployee().getNum_employee(), response.getEmployee().getName(), response.getEmployee().getJob_name(), response.getEmployee().getDept_name());
        oLog.setNumEmployee(response.getEmployee().getNum_employee());
        oLog.setNameEmployee(response.getEmployee().getName());
        oLog.setNameEmpJob(response.getEmployee().getJob_name());
        oLog.setNameEmpDept(response.getEmployee().getDept_name());
        
        oLog.setSource(oEnumSource);
        
        // se valida el acceso mediante la respuesta del servidor
        boolean respuesta = validateAccess(response);
        
        if (timer != null) {
            timer.cancel();
        }
        
        timer = new java.util.Timer();
        timer.schedule(
            new java.util.TimerTask() {
                @Override
                public void run() {
                    resetFields();
                    showWelcome();
                }
            },
            oConfig.getSecondsAccessScreen() * 1000
        );
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
        if (photoIcon.getIconHeight() > 300) {
            ImageIcon imageIcon = new ImageIcon(photoIcon.getImage().getScaledInstance(300, 300, Image.SCALE_DEFAULT));
            jlImgPhoto.setIcon(imageIcon);
        }
        else {
            jlImgPhoto.setIcon(photoIcon);
        }
        jlImgPhoto.setPreferredSize(new java.awt.Dimension(300, 300));
    }
    
    /**
     * Muestra en pantalla número y nombre de empleado.
     * 
     * @param numEmployee
     * @param nameEmployee 
     */
    private void showData(String numEmployee, String nameEmployee, String empJob, String empDept) {
        moTextNumEmp.setValue(numEmployee == null ? "NA" : numEmployee.toUpperCase());
        moTextNameEmp.setValue(nameEmployee == null ? "NA" : nameEmployee.toUpperCase());
        moTextJob.setValue(empJob == null ? "NA" : empJob.toUpperCase());
        moTextDepartment.setValue(empDept == null ? "NA" : empDept.toUpperCase());
    }
    
    /**
     * Muestra en pantalla el timestamp de la fecha y hora de consulta.
     * @param dtDate 
     */
    private void showTimestamp(Date dtDate) {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String sDate = df.format(dtDate);
        
        moTextTimestamp.setText(sDate);
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
        
        moScreenLog.getlLog().add(0, oLog);
        moScreenLog.updateTableLog();
        oEnumSource = null;
        
        return response.isAuthorized();
    }
    
    public void showNotFound() {
        resetFields();
        showAccess();
        
        jlReason.setText("LA HUELLA LEÍDA NO HA SIDO IDENTIFICADA");
        
        if (timer != null) {
            timer.cancel();
        }
        
        timer = new java.util.Timer();
        timer.schedule(
            new java.util.TimerTask() {
                @Override
                public void run() {
                    resetFields();
                    showWelcome();
                }
            },
            oConfig.getSecondsAccessScreen() * 1000
        );
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
        String textReason = reason == null ? "" : reason.toUpperCase();
        
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
        
        moTextScheduleIn.setText(schIn);
        moTextScheduleOut.setText(schOut);
        
        jlUnauthorized.setIcon(new javax.swing.ImageIcon("img/no_auth.png"));
        jlUnauthorized.setPreferredSize(new java.awt.Dimension(225, 225));
    }
    
    /**
     * Mostrar autorización.
     * 
     * @param scheduleIn
     * @param scheduleOut 
     */
    private void showAutorized(String scheduleIn, String scheduleOut, boolean fromLog) {
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
        
        moTextScheduleIn.setText(schIn);
        moTextScheduleOut.setText(schOut);
        
        jlAuthorized.setIcon(new javax.swing.ImageIcon("img/auth.png"));
        jlAuthorized.setPreferredSize(new java.awt.Dimension(225, 225));
        
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
     * Mostrar datos de bitácora.
     */
    private void showLog() {
        moScreenLog.setVisible(true);
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
            Logger.getLogger(CAPScreenNV.class.getName()).log(Level.SEVERE, null, ex);
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
            else if (button == jbShowLog || button == jbShowLog2) {
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
        jPGeneral = new javax.swing.JPanel();
        jPanel32 = new javax.swing.JPanel();
        jPanel36 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        moTextSearchNum = new sa.lib.gui.bean.SBeanFieldText();
        jbSearch = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLImage = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jlTitle = new javax.swing.JLabel();
        jlClock = new javax.swing.JLabel();
        jPAccess = new javax.swing.JPanel();
        jPphoto = new javax.swing.JPanel();
        jlImgPhoto = new javax.swing.JLabel();
        jbShowLog = new javax.swing.JButton();
        jPdata = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        jlAuthorized = new javax.swing.JLabel();
        jlUnauthorized = new javax.swing.JLabel();
        jPanel11 = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        jPanel18 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        moTextTimestamp = new sa.lib.gui.bean.SBeanFieldText();
        jPanel19 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        moTextNameEmp = new sa.lib.gui.bean.SBeanFieldText();
        jPanel20 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        moTextNumEmp = new sa.lib.gui.bean.SBeanFieldText();
        jlJob = new javax.swing.JLabel();
        moTextJob = new sa.lib.gui.bean.SBeanFieldText();
        jPanel22 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        moTextDepartment = new sa.lib.gui.bean.SBeanFieldText();
        jPanel21 = new javax.swing.JPanel();
        jlIn = new javax.swing.JLabel();
        moTextScheduleIn = new sa.lib.gui.bean.SBeanFieldText();
        jlOut = new javax.swing.JLabel();
        moTextScheduleOut = new sa.lib.gui.bean.SBeanFieldText();
        jPanel10 = new javax.swing.JPanel();
        jlReason = new javax.swing.JLabel();
        jPWelcome = new javax.swing.JPanel();
        jLWelcome = new javax.swing.JLabel();
        jlWelcomeImg = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jbShowLog2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel32.setLayout(new java.awt.BorderLayout());

        jPanel36.setLayout(new java.awt.BorderLayout());

        jPanel7.setLayout(new java.awt.GridLayout(1, 1));

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel1.setText("Registro acceso:");
        jPanel9.add(jLabel1);

        moTextSearchNum.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        moTextSearchNum.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        moTextSearchNum.setPreferredSize(new java.awt.Dimension(150, 23));
        moTextSearchNum.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                moTextSearchNumKeyPressed(evt);
            }
        });
        jPanel9.add(moTextSearchNum);

        jbSearch.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jbSearch.setText("Buscar");
        jbSearch.setPreferredSize(new java.awt.Dimension(125, 25));
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
        jLImage.setPreferredSize(new java.awt.Dimension(140, 140));
        jPanel5.add(jLImage);

        jPanel3.add(jPanel5);

        jPanel6.setLayout(new java.awt.GridLayout(2, 1, 0, 10));

        jlTitle.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jlTitle.setText("CAP Control de Acceso");
        jlTitle.setPreferredSize(new java.awt.Dimension(450, 23));
        jPanel6.add(jlTitle);

        jlClock.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jPanel6.add(jlClock);

        jPanel3.add(jPanel6);

        jPanel36.add(jPanel3, java.awt.BorderLayout.PAGE_START);

        jPanel32.add(jPanel36, java.awt.BorderLayout.PAGE_START);

        jPGeneral.add(jPanel32);

        jPphoto.setLayout(new java.awt.BorderLayout());

        jlImgPhoto.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jlImgPhoto.setText("Foto");
        jlImgPhoto.setToolTipText("Foto (tamaño sugerido: 100×100 px)");
        jlImgPhoto.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jlImgPhoto.setPreferredSize(new java.awt.Dimension(300, 300));
        jPphoto.add(jlImgPhoto, java.awt.BorderLayout.CENTER);

        jbShowLog.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jbShowLog.setText("Ver bitácora");
        jbShowLog.setPreferredSize(new java.awt.Dimension(125, 25));
        jPphoto.add(jbShowLog, java.awt.BorderLayout.PAGE_END);

        jPAccess.add(jPphoto);

        jPdata.setLayout(new java.awt.BorderLayout());

        jlAuthorized.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jlAuthorized.setToolTipText("Foto (tamaño sugerido: 100×100 px)");
        jlAuthorized.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jlAuthorized.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jlAuthorized.setPreferredSize(new java.awt.Dimension(225, 225));
        jPanel12.add(jlAuthorized);

        jlUnauthorized.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jlUnauthorized.setToolTipText("Foto (tamaño sugerido: 100×100 px)");
        jlUnauthorized.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jlUnauthorized.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jlUnauthorized.setPreferredSize(new java.awt.Dimension(225, 225));
        jPanel12.add(jlUnauthorized);

        jPdata.add(jPanel12, java.awt.BorderLayout.CENTER);

        jPanel14.setLayout(new java.awt.GridLayout(6, 1));

        jPanel18.setToolTipText("Fecha-hora de registro");

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel2.setText("Fecha-hora registro");
        jPanel18.add(jLabel2);

        moTextTimestamp.setEditable(false);
        moTextTimestamp.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        moTextTimestamp.setText("23/08/2022 09:20 am");
        moTextTimestamp.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        moTextTimestamp.setPreferredSize(new java.awt.Dimension(250, 30));
        jPanel18.add(moTextTimestamp);

        jPanel14.add(jPanel18);

        jPanel19.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("Nombre:");
        jLabel5.setPreferredSize(new java.awt.Dimension(120, 23));
        jPanel19.add(jLabel5);

        moTextNameEmp.setEditable(false);
        moTextNameEmp.setBorder(null);
        moTextNameEmp.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        moTextNameEmp.setPreferredSize(new java.awt.Dimension(500, 30));
        jPanel19.add(moTextNameEmp);

        jPanel14.add(jPanel19);

        jPanel20.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel9.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setText("Número empleado:");
        jLabel9.setPreferredSize(new java.awt.Dimension(120, 23));
        jPanel20.add(jLabel9);

        moTextNumEmp.setEditable(false);
        moTextNumEmp.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        moTextNumEmp.setFont(new java.awt.Font("Tahoma", 0, 20)); // NOI18N
        jPanel20.add(moTextNumEmp);

        jlJob.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jlJob.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jlJob.setText("Puesto:");
        jlJob.setPreferredSize(new java.awt.Dimension(90, 23));
        jPanel20.add(jlJob);

        moTextJob.setEditable(false);
        moTextJob.setFont(new java.awt.Font("Tahoma", 0, 20)); // NOI18N
        moTextJob.setPreferredSize(new java.awt.Dimension(300, 23));
        jPanel20.add(moTextJob);

        jPanel14.add(jPanel20);

        jPanel22.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel8.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel8.setText("Departamento:");
        jLabel8.setPreferredSize(new java.awt.Dimension(120, 23));
        jPanel22.add(jLabel8);

        moTextDepartment.setEditable(false);
        moTextDepartment.setBorder(null);
        moTextDepartment.setFont(new java.awt.Font("Tahoma", 0, 20)); // NOI18N
        moTextDepartment.setPreferredSize(new java.awt.Dimension(500, 23));
        jPanel22.add(moTextDepartment);

        jPanel14.add(jPanel22);

        jPanel21.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jlIn.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jlIn.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jlIn.setText("Horario entrada:");
        jlIn.setPreferredSize(new java.awt.Dimension(120, 23));
        jPanel21.add(jlIn);

        moTextScheduleIn.setEditable(false);
        moTextScheduleIn.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        moTextScheduleIn.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        moTextScheduleIn.setPreferredSize(new java.awt.Dimension(200, 30));
        jPanel21.add(moTextScheduleIn);

        jlOut.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jlOut.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jlOut.setText("Horario salida:");
        jlOut.setPreferredSize(new java.awt.Dimension(90, 23));
        jPanel21.add(jlOut);

        moTextScheduleOut.setEditable(false);
        moTextScheduleOut.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        moTextScheduleOut.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        moTextScheduleOut.setPreferredSize(new java.awt.Dimension(200, 30));
        jPanel21.add(moTextScheduleOut);

        jPanel14.add(jPanel21);

        jlReason.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jlReason.setForeground(new java.awt.Color(204, 0, 0));
        jlReason.setPreferredSize(new java.awt.Dimension(500, 23));
        jPanel10.add(jlReason);

        jPanel14.add(jPanel10);

        jPanel11.add(jPanel14);

        jPdata.add(jPanel11, java.awt.BorderLayout.PAGE_START);

        jPAccess.add(jPdata);

        jPWelcome.setLayout(new java.awt.BorderLayout());

        jLWelcome.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        jLWelcome.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLWelcome.setText("Bienvenido a AETH");
        jLWelcome.setPreferredSize(new java.awt.Dimension(400, 100));
        jPWelcome.add(jLWelcome, java.awt.BorderLayout.PAGE_START);

        jlWelcomeImg.setPreferredSize(new java.awt.Dimension(500, 300));
        jPWelcome.add(jlWelcomeImg, java.awt.BorderLayout.CENTER);

        jbShowLog2.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jbShowLog2.setText("Ver bitácora");
        jbShowLog2.setPreferredSize(new java.awt.Dimension(125, 25));
        jPanel2.add(jbShowLog2);

        jPWelcome.add(jPanel2, java.awt.BorderLayout.PAGE_END);

        jPAccess.add(jPWelcome);

        jPGeneral.add(jPAccess);

        jPanel1.add(jPGeneral, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void moTextSearchNumKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_moTextSearchNumKeyPressed
        if(evt.getKeyCode() == KeyEvent.VK_ENTER) {
            this.actionSearchByEmployeeNum();
        }
    }//GEN-LAST:event_moTextSearchNumKeyPressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLImage;
    private javax.swing.JLabel jLWelcome;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPAccess;
    private javax.swing.JPanel jPGeneral;
    private javax.swing.JPanel jPWelcome;
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
    private javax.swing.JPanel jPanel36;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPdata;
    private javax.swing.JPanel jPphoto;
    private javax.swing.JButton jbSearch;
    private javax.swing.JButton jbShowLog;
    private javax.swing.JButton jbShowLog2;
    private javax.swing.JLabel jlAuthorized;
    private javax.swing.JLabel jlClock;
    private javax.swing.JLabel jlImgPhoto;
    private javax.swing.JLabel jlIn;
    private javax.swing.JLabel jlJob;
    private javax.swing.JLabel jlOut;
    private javax.swing.JLabel jlReason;
    private javax.swing.JLabel jlTitle;
    private javax.swing.JLabel jlUnauthorized;
    private javax.swing.JLabel jlWelcomeImg;
    private sa.lib.gui.bean.SBeanFieldText moTextDepartment;
    private sa.lib.gui.bean.SBeanFieldText moTextJob;
    private sa.lib.gui.bean.SBeanFieldText moTextNameEmp;
    private sa.lib.gui.bean.SBeanFieldText moTextNumEmp;
    private sa.lib.gui.bean.SBeanFieldText moTextScheduleIn;
    private sa.lib.gui.bean.SBeanFieldText moTextScheduleOut;
    private sa.lib.gui.bean.SBeanFieldText moTextSearchNum;
    private sa.lib.gui.bean.SBeanFieldText moTextTimestamp;
    // End of variables declaration//GEN-END:variables
}
