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
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTable;

/**
 *
 * @author Edwin Carmona
 */
public class CAPScreen extends javax.swing.JFrame implements ActionListener {
    
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

    /**
     * Creates new form CAPScreen
     * @param cfg
     */
    public CAPScreen(CAPConfig cfg) {
        this.oConfig = cfg;
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
        
        jlTitle.setText("<html><body><div>CAP Access Control <br> " + oConfig.getCompanyData().getCompanyName() + "</div></body></html>");
        jlTitle.setFont(new Font("Verdana", Font.PLAIN, 12));
        
        jTableLog.getColumnModel().getColumn(0).setPreferredWidth(100);
        jTableLog.getColumnModel().getColumn(1).setPreferredWidth(50);
        jTableLog.getColumnModel().getColumn(2).setPreferredWidth(50);
        jTableLog.getColumnModel().getColumn(3).setPreferredWidth(200);
        jTableLog.getColumnModel().getColumn(4).setPreferredWidth(75);
        
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
                        oEnumSource = CAPSource.NOMBRE;
                        actionSearchEmployeeById(oEmployeeUI.getIdEmployee());
                    }
                }
            }
        });
        
        /**
         * Detectar doble click en la lista de la bitácora para visualizar datos.
         */
        jTableLog.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                JTable table = (JTable) evt.getSource();
                Point point = evt.getPoint();
                int row = table.rowAtPoint(point);
                if (evt.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    oLog = lLog.get(row);
                    showLog();
                }
            }
        });
        
        lLog = new ArrayList();
        
        disableSearchByEmployee();
        
        if (oCAPRequest.login() == -1) {
            System.exit(-1);
        }
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
        SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyy HH:mm:ss");
        jTextField1.setFont(new Font("Verdana", Font.BOLD, 16));
        
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
        this.oEnumSource = CAPSource.CODIGO;
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
        oLog.setTimeStamp(tDate);
        showTimestamp(tDate);
        
        // se muestra la info del empleado
        this.showData(response.getEmployee().getNum_employee(), response.getEmployee().getName());
        oLog.setNumEmployee(response.getEmployee().getNum_employee());
        oLog.setNameEmployee(response.getEmployee().getName());
        
        oLog.setSource(oEnumSource);
        
        // se valida el acceso mediante la respuesta del servidor
        if (! validateAccess(response)) {
            //si se niega el acceso se muestra el mensaje en pantalla y se escribe en la bitácora
            oLog.setAuthorized(false);
            lLog.add(0, oLog);
            updateTableLog();
            oEnumSource = null;
            
            return;
        }
        
        // si el acceso al empleado es permitido se muestra la autorización y se escribe el suceso en la biácora
        this.showAutorized(response.getSchedule().getInDateTimeSch(), response.getSchedule().getOutDateTimeSch(), false);
        oLog.setAuthorized(true);
        lLog.add(0, oLog);
        updateTableLog();
        oEnumSource = null;
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
        if (! response.getEmployee().isIs_active() || response.getEmployee().isIs_delete()) {
            String reason = "El empleado está desactivado en el sistema";
            showUnauthorized(reason, "", "", false, false);
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
            showUnauthorized(reasons, sIn, sOut, true, false);
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
            showUnauthorized(reasons, sIn, sOut, true, false);
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
                showUnauthorized(reasons, sIn, sOut, false, false);
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
            showUnauthorized(reasons, sIn, sOut, true, false);
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
     * @param fromLog 
     */
    public void showUnauthorized(String reason, String scheduleIn, String scheduleOut, boolean isNext, boolean fromLog) {
        String text = "<html>"
                        + "<body style='text-align: center; background-color: red;'>"
                            + "<div style='height: 180px; width: 250px; top: 50%;'><br><br>ACCESO<br>DENEGADO</div>"
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
        jlReason.setFont(new Font("Verdana", Font.PLAIN, 11));
        
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
                            + "<div style='height: 200px; width: 180px; top: 50%;'><br><br>ACCESO<br>AUTORIZADO</div>"
                        + "</body>"
                    + "</html>";
        
        jlResultMessage.setText(text);
        jlResultMessage.setFont(new Font("Verdana", Font.PLAIN, 32));
        
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
    }
    
    /**
     * Mostrar valor seleccionado de la bitácora.
     */
    private void actionShowLog() {
        int i = jTableLog.getSelectedRow();
        oLog = this.lLog.get(i);
        showLog();
    }
    
    /**
     * Actualizar bitácora.
     */
    private void updateTableLog() {
        CAPLogUIModel model = (CAPLogUIModel) jTableLog.getModel();
        
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
            Logger.getLogger(CAPMainUI.class.getName()).log(Level.SEVERE, null, ex);
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
        jPanel3 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        jlTitle = new javax.swing.JLabel();
        jPanel14 = new javax.swing.JPanel();
        jLImage = new javax.swing.JLabel();
        jPanel15 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jPanel16 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jPanel17 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jtfNumEmployee = new javax.swing.JTextField();
        jbSearch = new javax.swing.JButton();
        jPanel31 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jbSearchByEmployee = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
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
        jPanel4 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jlImgPhoto = new javax.swing.JLabel();
        jPanel20 = new javax.swing.JPanel();
        jPanel18 = new javax.swing.JPanel();
        jtfNumEmp = new javax.swing.JTextField();
        jPanel19 = new javax.swing.JPanel();
        jtfNameEmp = new javax.swing.JTextField();
        jPanel30 = new javax.swing.JPanel();
        jtfTimestamp = new javax.swing.JTextField();
        jPanel21 = new javax.swing.JPanel();
        jlIn = new javax.swing.JLabel();
        jtfScheduleIn = new javax.swing.JTextField();
        jPanel24 = new javax.swing.JPanel();
        jlOut = new javax.swing.JLabel();
        jtfScheduleOut = new javax.swing.JTextField();
        jPanel11 = new javax.swing.JPanel();
        jPanel22 = new javax.swing.JPanel();
        jlReason = new javax.swing.JLabel();
        jPanel23 = new javax.swing.JPanel();
        jlResultMessage = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jPanel28 = new javax.swing.JPanel();
        jbShowLog = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTableLog = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel2.setLayout(new java.awt.GridLayout(1, 3));

        jPanel3.setLayout(new java.awt.BorderLayout());

        jPanel6.setLayout(new java.awt.GridLayout(2, 1));

        jlTitle.setText("CAP Access Control");
        jPanel13.add(jlTitle);

        jPanel8.add(jPanel13);

        jLImage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLImage.setToolTipText("Foto (tamaño sugerido: 100×100 px)");
        jLImage.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLImage.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLImage.setPreferredSize(new java.awt.Dimension(75, 75));
        jPanel14.add(jLImage);

        jPanel8.add(jPanel14);

        jTextField1.setEditable(false);
        jTextField1.setText("jTextField1");
        jTextField1.setPreferredSize(new java.awt.Dimension(300, 23));
        jPanel15.add(jTextField1);

        jPanel8.add(jPanel15);

        jLabel2.setText("Consulte Empleado");
        jLabel2.setPreferredSize(new java.awt.Dimension(300, 23));
        jPanel16.add(jLabel2);

        jPanel8.add(jPanel16);

        jLabel5.setText("Num. Empleado:");
        jLabel5.setPreferredSize(new java.awt.Dimension(100, 23));
        jPanel17.add(jLabel5);

        jtfNumEmployee.setPreferredSize(new java.awt.Dimension(135, 23));
        jtfNumEmployee.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jtfNumEmployeeonKeyPressedNumEmp(evt);
            }
        });
        jPanel17.add(jtfNumEmployee);

        jbSearch.setText("Buscar");
        jPanel17.add(jbSearch);

        jPanel8.add(jPanel17);

        jLabel4.setPreferredSize(new java.awt.Dimension(150, 23));
        jPanel31.add(jLabel4);

        jbSearchByEmployee.setText("Búsqueda por nombre");
        jPanel31.add(jbSearchByEmployee);

        jPanel8.add(jPanel31);

        jPanel6.add(jPanel8);

        jLabel8.setText("Búsqueda de empleado");
        jLabel8.setPreferredSize(new java.awt.Dimension(150, 23));
        jPanel25.add(jLabel8);

        jPanel9.add(jPanel25);

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

        jPanel9.add(jPanel26);

        jScrollPane.setPreferredSize(new java.awt.Dimension(300, 125));

        jScrollPane.setViewportView(jlistSearchEmployees);

        jPanel27.add(jScrollPane);

        jPanel9.add(jPanel27);

        jbSearchSelectedEmployee.setText("Seleccionar");
        jPanel29.add(jbSearchSelectedEmployee);

        jPanel9.add(jPanel29);

        jPanel6.add(jPanel9);

        jPanel3.add(jPanel6, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel3);

        jPanel4.setLayout(new java.awt.BorderLayout());

        jPanel7.setLayout(new java.awt.GridLayout(2, 1));

        jLabel1.setPreferredSize(new java.awt.Dimension(110, 23));
        jPanel10.add(jLabel1);

        jlImgPhoto.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jlImgPhoto.setText("Foto");
        jlImgPhoto.setToolTipText("Foto (tamaño sugerido: 100×100 px)");
        jlImgPhoto.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jlImgPhoto.setPreferredSize(new java.awt.Dimension(100, 100));
        jPanel10.add(jlImgPhoto);

        jPanel20.setLayout(new java.awt.GridLayout(5, 1));

        jtfNumEmp.setEditable(false);
        jtfNumEmp.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jtfNumEmp.setPreferredSize(new java.awt.Dimension(200, 23));
        jPanel18.add(jtfNumEmp);

        jPanel20.add(jPanel18);

        jtfNameEmp.setEditable(false);
        jtfNameEmp.setPreferredSize(new java.awt.Dimension(200, 23));
        jPanel19.add(jtfNameEmp);

        jPanel20.add(jPanel19);

        jtfTimestamp.setEditable(false);
        jtfTimestamp.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jtfTimestamp.setPreferredSize(new java.awt.Dimension(200, 23));
        jPanel30.add(jtfTimestamp);

        jPanel20.add(jPanel30);

        jlIn.setText("Entrada");
        jlIn.setPreferredSize(new java.awt.Dimension(100, 23));
        jPanel21.add(jlIn);

        jtfScheduleIn.setEditable(false);
        jtfScheduleIn.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jtfScheduleIn.setPreferredSize(new java.awt.Dimension(200, 23));
        jPanel21.add(jtfScheduleIn);

        jPanel20.add(jPanel21);

        jlOut.setText("Salida");
        jlOut.setPreferredSize(new java.awt.Dimension(100, 23));
        jPanel24.add(jlOut);

        jtfScheduleOut.setEditable(false);
        jtfScheduleOut.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jtfScheduleOut.setPreferredSize(new java.awt.Dimension(200, 23));
        jPanel24.add(jtfScheduleOut);

        jPanel20.add(jPanel24);

        jPanel10.add(jPanel20);

        jPanel7.add(jPanel10);

        jPanel22.add(jlReason);

        jPanel11.add(jPanel22);

        jPanel23.add(jlResultMessage);

        jPanel11.add(jPanel23);

        jPanel7.add(jPanel11);

        jPanel4.add(jPanel7, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel4);

        jPanel5.setLayout(new java.awt.BorderLayout());

        jLabel3.setPreferredSize(new java.awt.Dimension(150, 23));
        jPanel12.add(jLabel3);

        jbShowLog.setText("Ver");
        jPanel28.add(jbShowLog);

        jPanel12.add(jPanel28);

        jScrollPane3.setPreferredSize(new java.awt.Dimension(225, 520));

        jTableLog.setModel(new CAPLogUIModel());
        jTableLog.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTableLog.setColumnSelectionAllowed(true);
        jScrollPane3.setViewportView(jTableLog);

        jPanel12.add(jScrollPane3);

        jPanel5.add(jPanel12, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel5);

        jPanel1.add(jPanel2, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jtfNumEmployeeonKeyPressedNumEmp(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jtfNumEmployeeonKeyPressedNumEmp
        if(evt.getKeyCode() == KeyEvent.VK_ENTER) {
            this.actionSearchByEmployeeNum();
        }
    }//GEN-LAST:event_jtfNumEmployeeonKeyPressedNumEmp

    private void jtfSearchEmployeeKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jtfSearchEmployeeKeyReleased
        if (jtfSearchEmployee.getText().length() >= 3) {
            showEmployeesResult();
        }
    }//GEN-LAST:event_jtfSearchEmployeeKeyReleased


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLImage;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
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
    private javax.swing.JPanel jPanel31;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTableLog;
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
    private javax.swing.JLabel jlTitle;
    private javax.swing.JList<CAPEmployeeUI> jlistSearchEmployees;
    private javax.swing.JTextField jtfNameEmp;
    private javax.swing.JTextField jtfNumEmp;
    private javax.swing.JTextField jtfNumEmployee;
    private javax.swing.JTextField jtfScheduleIn;
    private javax.swing.JTextField jtfScheduleOut;
    private javax.swing.JTextField jtfSearchEmployee;
    private javax.swing.JTextField jtfTimestamp;
    // End of variables declaration//GEN-END:variables
}
