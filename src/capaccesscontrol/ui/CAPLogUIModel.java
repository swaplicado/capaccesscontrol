/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package capaccesscontrol.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Edwin Carmona
 */
public class CAPLogUIModel extends AbstractTableModel  {

    public CAPLogUIModel() {
        lModelLog = new  ArrayList();
        format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    }

    private SimpleDateFormat format;
    
    private String[] columnNames = {
                                        "Fecha/hora", 
                                        "Acceso",
                                        "Número",
                                        "Nombre",
                                        "Puesto",
                                        "Departamento",
                                        "Origen"
                                    };
    
    private List<CAPLogUI> lModelLog;

    public List<CAPLogUI> getlModelLog() {
        return lModelLog;
    }

    public void setlModelLog(List<CAPLogUI> lModelLog) {
        this.lModelLog = lModelLog;
    }
    
    @Override
    public String getColumnName(int index) {
        return columnNames[index];
    }
    
    @Override
    public int getRowCount() {
        return lModelLog.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }
    
    @Override
    public Class getColumnClass(int col) {
        switch (col) {
            case 1:
                return Icon.class;
            default:
                return super.getColumnClass(col);
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object value = "??";
        CAPLogUI logRow = lModelLog.get(rowIndex);
        switch (columnIndex) {
            case 0:
                value = format.format(logRow.getTimeStamp());
                break;
            case 1:
//                value = logRow.isAuthorized() ? "AUTORIZADO" : "DENEGADO";
                value = logRow.isAuthorized() ? new ImageIcon("img/success.png") : new ImageIcon("img/wrong.png");
                break;
            case 2:
                value = logRow.getNumEmployee();
                break;
            case 3:
                value = logRow.getNameEmployee();
                break;
            case 4:
                value = logRow.getNameEmpJob();
                break;
            case 5:
                value = logRow.getNameEmpDept();
                break;
            case 6:
                value = logRow.getSource().toString();
                break;
        }

        return value;
    }
    
}
