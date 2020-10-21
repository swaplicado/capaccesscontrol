/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package capaccesscontrol.ui;

import capaccesscontrol.config.CAPConfig;
import capaccesscontrol.core.CAPCore;
import capaccesscontrol.db.CAPMySql;
import capaccesscontrol.db.CAPSiieDb;
import capaccesscontrol.packet.CAPAbsenceResponse;
import capaccesscontrol.packet.CAPEmployeeResponse;
import capaccesscontrol.packet.CAPEventResponse;
import capaccesscontrol.packet.CAPRequest;
import capaccesscontrol.packet.CAPResponse;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;

/**
 *
 * @author Edwin Carmona
 */
public class CAPMainUI extends javax.swing.JFrame implements ActionListener {
    
    private final CAPConfig oConfig;
    private CAPMySql oSiieMySql;
    private CAPMySql oCapMySql;
    private CAPRequest oCAPRequest;
    private Date tDate;
    private List<CAPEmployeeResponse> lEmployees;
    private CAPEmployeeUI oEmployeeUI;
    private List<CAPLogUI> lLog;
    private CAPLogUI oLog;

    /**
     * Creates new form CAPMainUI
     * @param config
     */
    public CAPMainUI(CAPConfig config) {
        oConfig = config;
        initComponents();
        initCustom();
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
        jbSearch.addActionListener(this);
        jbSearchByEmployee.addActionListener(this);
        jbSearchSelectedEmployee.addActionListener(this);
        jbShowLog.addActionListener(this);
        
        /**
         * Detectar doble click en la lista empleados para realizar la búsqueda.
         */
        jlistSearchEmployees.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList)evt.getSource();
                if (evt.getClickCount() == 2) {

                    // Double-click detected
                    int index = list.locationToIndex(evt.getPoint());
                    if (index > -1) {
                        oEmployeeUI = (CAPEmployeeUI) list.getModel().getElementAt(index);
                        actionSearchEmployeeById(oEmployeeUI.getIdEmployee());
                    }
                }
            }
        });
        
        /**
         * Detectar doble click en la lista de la bitácora para visualizar datos.
         */
        jlistLog.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList)evt.getSource();
                if (evt.getClickCount() == 2) {

                    // Double-click detected
                    int index = list.locationToIndex(evt.getPoint());
                    if (index > -1) {
                        oLog = (CAPLogUI) list.getModel().getElementAt(index);
                        showLog();
                    }
                }
            }
        });
        
        lLog = new ArrayList();
        
        disableSearchByEmployee();
    }
    
    /**
     * Reiniciar etiquetas y campos de texto.
     */
    private void resetFields() {
        jtfSearching.setText("");
        jtfNumEmployee.setText("");
        jtfNumEmp.setText("");
        jtfNameEmp.setText("");
        jlReason.setText("");
        jlImgPhoto.setText("");
        javax.swing.ImageIcon photoIcon = new ImageIcon();
        jlImgPhoto.setIcon(photoIcon);
    }
    
    /**
     * Este método inactiva la búsqueda por empleado.
     */
    private void disableSearchByEmployee() {
        jtfSearchEmployee.setEditable(false);
        jlistSearchEmployees.setEnabled(false);
        jbSearchSelectedEmployee.setEnabled(false);
        
        jtfNumEmployee.requestFocusInWindow();
    }
    
    /**
     * Activar búsqueda de empleado.
     */
    private void enableSearchByEmployee() {
        jtfSearchEmployee.setEditable(true);
        jlistSearchEmployees.setEnabled(true);
        jbSearchSelectedEmployee.setEnabled(true);
    }
    
    /**
     * Carga la imagen de la empresa configurada en el archivo cfg.json.
     */
    private void setImage() {
        jLImage.setIcon(new javax.swing.ImageIcon(oConfig.getCompanyData().getCompanyImage()));
        jLImage.setPreferredSize(new java.awt.Dimension(75, 75));
    }

    /**
     * Iniciar reloj y visualizar en pantalla.
     */
    private void startClock() {
        SimpleDateFormat format1 = new SimpleDateFormat("dd-MM-yyy HH:mm:ss");
        
        javax.swing.Timer t = new javax.swing.Timer(1000,
                new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Calendar now = Calendar.getInstance();

                jTextField1.setText(format1.format(now.getTime()));
                jTextField1.setHorizontalAlignment(jTextField1.CENTER);
                // Center the text
                jTextField1.getCaret().setVisible(false);
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
        oLog.setTimeStamp(tDate);
        showTimestamp(tDate);
        CAPResponse response = oCAPRequest.requestByNumEmployee(tDate, numEmp, oConfig.getSearchScheduleDays(), oConfig.getUrlNumEmployee());
        
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
        oEmployeeUI = jlistSearchEmployees.getSelectedValue();
        actionSearchEmployeeById(oEmployeeUI.getIdEmployee());
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
        oLog.setTimeStamp(tDate);
        showTimestamp(tDate);
        
        CAPResponse response = oCAPRequest.requestByIdEmployee(tDate, employeeId, oConfig.getSearchScheduleDays(), oConfig.getUrlIdEmployee());
        this.processCAPResponse(response);
        
        DefaultListModel model = new DefaultListModel();
        jlistSearchEmployees.setModel(model);
        jtfSearchEmployee.setText("");
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
        
        // se muestra la info del empleado
        this.showData(response.getEmployee().getNum_employee(), response.getEmployee().getName());
        oLog.setNumEmployee(response.getEmployee().getNum_employee());
        oLog.setNameEmployee(response.getEmployee().getName());
        
        // se valida el acceso mediante la respuesta del servidor
        if (! validateAccess(response)) {
            //si se niega el acceso se muestra el mensaje en pantalla y se escribe en la bitácora
            oLog.setAuthorized(false);
            lLog.add(0, oLog);
            updateLog();
            
            return;
        }
        
        // si el acceso al empleado es permitido se muestra la autorización y se escribe el suceso en la biácora
        this.showAutorized(response.getSchedule().getInDateTimeSch(), response.getSchedule().getOutDateTimeSch());
        oLog.setAuthorized(true);
        lLog.add(0, oLog);
        updateLog();
    }
    
    /**
     * Muestra los resultados que tengas coincidencias con el criterio buscado.
     * 
     * En base a lo escrito en el campo de empleado se filtra de la lista obtenida en el servidor los 
     * empleados que cumplan con el criterio de búsqueda y se muestran en la lista de búsqueda de empleados.
     */
    private void showEmployeesResult() {
        String empText = jtfSearchEmployee.getText();
        CAPEmployeeUI empUi;
        DefaultListModel model = new DefaultListModel();
        
        // se filtran los empleados que contengan en el nombre el texto buscado
        List<CAPEmployeeResponse> result = lEmployees.stream()
                .filter(item -> (item.getName().toLowerCase()).contains(empText.toLowerCase()))
                .collect(Collectors.toList());
        
        if (result.isEmpty()) {
            jlistSearchEmployees.setModel(model);
            return;
        }
        
        for (CAPEmployeeResponse employeeResponse : result) {
            empUi = new CAPEmployeeUI(employeeResponse.getId(), employeeResponse.getName(), employeeResponse.getNum_employee(), employeeResponse.getExternal_id());
            model.addElement(empUi);
        }
        
        // el modelo con la lista de empleados resultante de la búsqueda es fijada en pantalla
        jlistSearchEmployees.setModel(model);
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
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm");
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
            showUnauthorized("No se encontró al empleado en el sistema", "", "", false);
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
        if (! response.getEmployee().isIs_active() || response.getEmployee().isIs_delete()) {
            String reason = "El empleado está desactivado en el sistema";
            showUnauthorized(reason, "", "", false);
            oLog.setReasons(reason);
            
            return false;
        }
        
        String sIn = "";
        String sOut = "";
        
        // Si el empleado tiene eventos programados
        if (response.getEvents() != null && response.getEvents().size() > 0) {
            String reason = "";
            for (CAPEventResponse event : response.getEvents()) {
                reason = reason.isEmpty() ? event.getTypeName() : (reason + ", " + event.getTypeName());
            }
            
            if (response.getNextSchedule() != null) {
                sIn = response.getNextSchedule().getInDateTimeSch();
                sOut = response.getNextSchedule().getOutDateTimeSch();
            }
            String reasons = "El empleado tiene programado: " + reason + " para el día de hoy";
            showUnauthorized(reasons, sIn, sOut, true);
            oLog.setReasons(reasons);
            
            return false;
        }
        
        // Si el empleado tiene incidencias programadas
        if (response.getAbsences() != null && response.getAbsences().size() > 0) {
            String reason = "";
            for (CAPAbsenceResponse abs : response.getAbsences()) {
                reason = reason.isEmpty() ? abs.getType_name() : (reason + ", " + abs.getType_name());
            }
            
            if (response.getNextSchedule() != null) {
                sIn = response.getNextSchedule().getInDateTimeSch();
                sOut = response.getNextSchedule().getOutDateTimeSch();
            }
            
            String reasons = "El empleado tiene incidencias: " + reason + " para el día de hoy";
            showUnauthorized(reasons, sIn, sOut, true);
            oLog.setReasons(reasons);
            
            return false;
        }
        
        // si el empleado tiene un horario
        if (response.getSchedule() != null) {
            // Si el empleado está o no en su horario
            if (! CAPCore.isOnShift(response.getSchedule().getInDateTimeSch(), response.getSchedule().getOutDateTimeSch(), tDate, oConfig.getMinPrevSchedule(), oConfig.getMinPostSchedule())) {
                sIn = response.getSchedule().getInDateTimeSch();
                sOut = response.getSchedule().getOutDateTimeSch();
                
                String reasons = "El empleado está fuera de su horario";
                showUnauthorized(reasons, sIn, sOut, false);
                oLog.setReasons(reasons);
                
                return false;
            }
            else {
                return true;
            }
        }
        else {
            if (response.getNextSchedule() != null) {
                sIn = response.getNextSchedule().getInDateTimeSch();
                sOut = response.getNextSchedule().getOutDateTimeSch();
            }
            
            String reasons = "El empleado no tiene horario asignado para el día de hoy";
            showUnauthorized(reasons, sIn, sOut, true);
            oLog.setReasons(reasons);
            
            return false;
        }
    }
    
    /**
     * Mostrar acceso denegado.
     * 
     * @param reason
     * @param scheduleIn
     * @param scheduleOut
     * @param isNext 
     */
    private void showUnauthorized(String reason, String scheduleIn, String scheduleOut, boolean isNext) {
        String text = "<html>"
                        + "<body style='text-align: center; background-color: red;'>"
                            + "<div style='height: 200px; width: 180px; top: 50%;'><br><br>ACCESO<br>DENEGADO</div>"
                        + "</body>"
                    + "</html>";
        
        jlResultMessage.setText(text);
        jlResultMessage.setFont(new Font("Verdana", Font.PLAIN, 32));
        
        String textReason = "<html>"
                        + "<body style='text-align: center;'>"
                            + "<div style='height: 100%; width: 100%;'><p>" + reason + "</p></div>"
                        + "</body>"
                    + "</html>";
        
        jlReason.setText(textReason);
        jlReason.setFont(new Font("Verdana", Font.PLAIN, 14));
        
        if (! isNext) {
            jlIn.setText("Horario entrada");
            jlOut.setText("Horario salida");
        }
        else {
            jlIn.setText("Próximo horario entrada");
            jlOut.setText("Próximo horario salida");
        }
        
        jtfScheduleIn.setText(scheduleIn);
        jtfScheduleOut.setText(scheduleOut);
    }
    
    /**
     * Mostrar autorización.
     * 
     * @param scheduleIn
     * @param scheduleOut 
     */
    private void showAutorized(String scheduleIn, String scheduleOut) {
        String text = "<html>"
                        + "<body style='text-align: center; background-color: green;'>"
                            + "<div style='height: 200px; width: 180px; top: 50%;'><br><br>ACCESO<br>AUTORIZADO</div>"
                        + "</body>"
                    + "</html>";
        
        jlResultMessage.setText(text);
        jlResultMessage.setFont(new Font("Verdana", Font.PLAIN, 32));
        
        jlIn.setText("Horario entrada");
        jlOut.setText("Horario salida");
        
        jtfScheduleIn.setText(scheduleIn);
        jtfScheduleOut.setText(scheduleOut);
    }
    
    /**
     * Mostrar valor seleccionado de la bitácora.
     */
    private void actionShowLog() {
        oLog = jlistLog.getSelectedValue();
        showLog();
    }
    
    /**
     * Actualizar bitácora.
     */
    private void updateLog() {
        DefaultListModel model = new DefaultListModel();
        
        if (oConfig.getCountLog() < lLog.size()) {
            lLog.remove(lLog.size() - 1);
        }
        
        for (CAPLogUI cAPLogUI : lLog) {
            model.addElement(cAPLogUI);
        }
        
        jlistLog.setModel(model);
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
            showAutorized("", "");
        }
        else {
            showUnauthorized(oLog.getReasons(), "", "", false);
        }
        
        jtfNumEmployee.requestFocusInWindow();
    }
    
    @Override
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() instanceof JButton) {
            JButton button = (JButton) evt.getSource();

            if (button == jbSearch) {
                actionSearchByEmployeeNum();
            }
            else if (button == jbSearchByEmployee) {
                actionSearchByEmployee();
            }
            else if (button == jbSearchSelectedEmployee) {
                actionSearchSelectedEmployee();
            }
            else if (button == jbShowLog) {
                actionShowLog();
            }
        }
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
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jLImage = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jPanel10 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jPanel11 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jtfNumEmployee = new javax.swing.JTextField();
        jPanel12 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jbSearch = new javax.swing.JButton();
        jPanel13 = new javax.swing.JPanel();
        jtfSearching = new javax.swing.JTextField();
        jbSearchByEmployee = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jPanel25 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jPanel26 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jtfSearchEmployee = new javax.swing.JTextField();
        jPanel27 = new javax.swing.JPanel();
        jScrollPane = new javax.swing.JScrollPane();
        jlistSearchEmployees = new javax.swing.JList<>();
        jPanel29 = new javax.swing.JPanel();
        jbSearchSelectedEmployee = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        jPanel15 = new javax.swing.JPanel();
        jlImgPhoto = new javax.swing.JLabel();
        jPanel18 = new javax.swing.JPanel();
        jtfNumEmp = new javax.swing.JTextField();
        jPanel19 = new javax.swing.JPanel();
        jtfNameEmp = new javax.swing.JTextField();
        jPanel30 = new javax.swing.JPanel();
        jtfTimestamp = new javax.swing.JTextField();
        jPanel16 = new javax.swing.JPanel();
        jlResultMessage = new javax.swing.JLabel();
        jPanel17 = new javax.swing.JPanel();
        jPanel20 = new javax.swing.JPanel();
        jlReason = new javax.swing.JLabel();
        jPanel23 = new javax.swing.JPanel();
        jlIn = new javax.swing.JLabel();
        jPanel21 = new javax.swing.JPanel();
        jtfScheduleIn = new javax.swing.JTextField();
        jPanel22 = new javax.swing.JPanel();
        jlOut = new javax.swing.JLabel();
        jPanel24 = new javax.swing.JPanel();
        jtfScheduleOut = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jPanel28 = new javax.swing.JPanel();
        jbShowLog = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jlistLog = new javax.swing.JList<>();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Buscar"));
        jPanel1.setPreferredSize(new java.awt.Dimension(700, 300));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel4.setLayout(new java.awt.GridLayout(1, 2));

        jLabel1.setText("CAP Access Control");
        jPanel7.add(jLabel1);

        jPanel5.add(jPanel7);

        jLImage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLImage.setToolTipText("Foto (tamaño sugerido: 100×100 px)");
        jLImage.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLImage.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLImage.setPreferredSize(new java.awt.Dimension(75, 75));
        jPanel8.add(jLImage);

        jPanel5.add(jPanel8);

        jTextField1.setEditable(false);
        jTextField1.setText("jTextField1");
        jTextField1.setPreferredSize(new java.awt.Dimension(300, 23));
        jPanel9.add(jTextField1);

        jPanel5.add(jPanel9);

        jLabel2.setText("Consulte Empleado");
        jPanel10.add(jLabel2);

        jPanel5.add(jPanel10);

        jLabel5.setText("Num. Empleado:");
        jLabel5.setPreferredSize(new java.awt.Dimension(125, 23));
        jPanel11.add(jLabel5);

        jtfNumEmployee.setPreferredSize(new java.awt.Dimension(175, 23));
        jtfNumEmployee.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                onKeyPressedNumEmp(evt);
            }
        });
        jPanel11.add(jtfNumEmployee);

        jPanel5.add(jPanel11);

        jLabel6.setPreferredSize(new java.awt.Dimension(200, 23));
        jPanel12.add(jLabel6);

        jbSearch.setText("Buscar");
        jPanel12.add(jbSearch);

        jPanel5.add(jPanel12);

        jtfSearching.setEditable(false);
        jtfSearching.setPreferredSize(new java.awt.Dimension(175, 23));
        jPanel13.add(jtfSearching);

        jbSearchByEmployee.setText("Búsqueda por nombre");
        jPanel13.add(jbSearchByEmployee);

        jPanel5.add(jPanel13);

        jPanel4.add(jPanel5);

        jLabel8.setText("Búsqueda de Empleado");
        jLabel8.setPreferredSize(new java.awt.Dimension(150, 23));
        jPanel25.add(jLabel8);

        jPanel6.add(jPanel25);

        jLabel9.setText("Nombre:");
        jLabel9.setPreferredSize(new java.awt.Dimension(75, 23));
        jPanel26.add(jLabel9);

        jtfSearchEmployee.setPreferredSize(new java.awt.Dimension(225, 23));
        jtfSearchEmployee.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jtfSearchEmployeeKeyReleased(evt);
            }
        });
        jPanel26.add(jtfSearchEmployee);

        jPanel6.add(jPanel26);

        jScrollPane.setPreferredSize(new java.awt.Dimension(300, 125));

        jScrollPane.setViewportView(jlistSearchEmployees);

        jPanel27.add(jScrollPane);

        jPanel6.add(jPanel27);

        jbSearchSelectedEmployee.setText("Seleccionar");
        jPanel29.add(jbSearchSelectedEmployee);

        jPanel6.add(jPanel29);

        jPanel4.add(jPanel6);

        jPanel1.add(jPanel4, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Resultado"));
        jPanel2.setPreferredSize(new java.awt.Dimension(700, 300));
        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel14.setLayout(new java.awt.GridLayout(1, 3, 5, 0));

        jlImgPhoto.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jlImgPhoto.setText("Foto");
        jlImgPhoto.setToolTipText("Foto (tamaño sugerido: 100×100 px)");
        jlImgPhoto.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jlImgPhoto.setPreferredSize(new java.awt.Dimension(100, 100));
        jPanel15.add(jlImgPhoto);

        jtfNumEmp.setEditable(false);
        jtfNumEmp.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jtfNumEmp.setPreferredSize(new java.awt.Dimension(200, 23));
        jPanel18.add(jtfNumEmp);

        jPanel15.add(jPanel18);

        jtfNameEmp.setEditable(false);
        jtfNameEmp.setPreferredSize(new java.awt.Dimension(200, 23));
        jPanel19.add(jtfNameEmp);

        jPanel15.add(jPanel19);

        jtfTimestamp.setEditable(false);
        jtfTimestamp.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jtfTimestamp.setPreferredSize(new java.awt.Dimension(200, 23));
        jPanel30.add(jtfTimestamp);

        jPanel15.add(jPanel30);

        jPanel14.add(jPanel15);

        jPanel16.setLayout(new java.awt.BorderLayout());

        jlResultMessage.setText("RESULTADO");
        jPanel16.add(jlResultMessage, java.awt.BorderLayout.CENTER);

        jPanel14.add(jPanel16);

        jlReason.setText("jLabel2");
        jlReason.setPreferredSize(new java.awt.Dimension(200, 92));
        jPanel20.add(jlReason);

        jPanel17.add(jPanel20);

        jlIn.setText("Entrada");
        jlIn.setPreferredSize(new java.awt.Dimension(200, 23));
        jPanel23.add(jlIn);

        jPanel17.add(jPanel23);

        jtfScheduleIn.setEditable(false);
        jtfScheduleIn.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jtfScheduleIn.setPreferredSize(new java.awt.Dimension(200, 23));
        jPanel21.add(jtfScheduleIn);

        jPanel17.add(jPanel21);

        jlOut.setText("Salida");
        jlOut.setPreferredSize(new java.awt.Dimension(200, 23));
        jPanel22.add(jlOut);

        jPanel17.add(jPanel22);

        jtfScheduleOut.setEditable(false);
        jtfScheduleOut.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jtfScheduleOut.setPreferredSize(new java.awt.Dimension(200, 23));
        jPanel24.add(jtfScheduleOut);

        jPanel17.add(jPanel24);

        jPanel14.add(jPanel17);

        jPanel2.add(jPanel14, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 306, -1, 283));

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Bitácora de consulta"));

        jLabel3.setPreferredSize(new java.awt.Dimension(150, 23));
        jPanel3.add(jLabel3);

        jbShowLog.setText("Ver");
        jPanel28.add(jbShowLog);

        jPanel3.add(jPanel28);

        jScrollPane1.setPreferredSize(new java.awt.Dimension(225, 520));

        jScrollPane1.setViewportView(jlistLog);

        jPanel3.add(jScrollPane1);

        getContentPane().add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 0, 240, 589));
    }// </editor-fold>//GEN-END:initComponents

    private void onKeyPressedNumEmp(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_onKeyPressedNumEmp
        if(evt.getKeyCode() == KeyEvent.VK_ENTER) {
            this.actionSearchByEmployeeNum();
        }
    }//GEN-LAST:event_onKeyPressedNumEmp

    private void jtfSearchEmployeeKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jtfSearchEmployeeKeyReleased
        if (jtfSearchEmployee.getText().length() >= 3) {
            showEmployeesResult();
        }
    }//GEN-LAST:event_jtfSearchEmployeeKeyReleased

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        jtfNumEmployee.requestFocusInWindow();
    }//GEN-LAST:event_formWindowOpened

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLImage;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel25;
    private javax.swing.JPanel jPanel26;
    private javax.swing.JPanel jPanel27;
    private javax.swing.JPanel jPanel28;
    private javax.swing.JPanel jPanel29;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel30;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JButton jbSearch;
    private javax.swing.JButton jbSearchByEmployee;
    private javax.swing.JButton jbSearchSelectedEmployee;
    private javax.swing.JButton jbShowLog;
    private javax.swing.JLabel jlImgPhoto;
    private javax.swing.JLabel jlIn;
    private javax.swing.JLabel jlOut;
    private javax.swing.JLabel jlReason;
    private javax.swing.JLabel jlResultMessage;
    private javax.swing.JList<CAPLogUI> jlistLog;
    private javax.swing.JList<CAPEmployeeUI> jlistSearchEmployees;
    private javax.swing.JTextField jtfNameEmp;
    private javax.swing.JTextField jtfNumEmp;
    private javax.swing.JTextField jtfNumEmployee;
    private javax.swing.JTextField jtfScheduleIn;
    private javax.swing.JTextField jtfScheduleOut;
    private javax.swing.JTextField jtfSearchEmployee;
    private javax.swing.JTextField jtfSearching;
    private javax.swing.JTextField jtfTimestamp;
    // End of variables declaration//GEN-END:variables
}
