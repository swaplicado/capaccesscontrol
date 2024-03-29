/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package capaccesscontrol.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.ImageIcon;

/**
 *
 * @author Edwin Carmona
 */
public class CAPLogUI {
    
    private SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    
    private String numEmployee;
    private String nameEmployee;
    private String nameEmpJob;
    private String nameEmpDept;
    private Date timeStamp;
    private ImageIcon photo;
    private boolean authorized;
    private String reasons;
    private CAPSource source;

    public String getNumEmployee() {
        return numEmployee;
    }

    public void setNumEmployee(String numEmployee) {
        this.numEmployee = numEmployee;
    }

    public String getNameEmployee() {
        return nameEmployee;
    }

    public void setNameEmployee(String nameEmployee) {
        this.nameEmployee = nameEmployee;
    }

    public String getNameEmpJob() {
        return nameEmpJob;
    }

    public void setNameEmpJob(String nameEmpJob) {
        this.nameEmpJob = nameEmpJob;
    }

    public String getNameEmpDept() {
        return nameEmpDept;
    }

    public void setNameEmpDept(String nameEmpDept) {
        this.nameEmpDept = nameEmpDept;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public ImageIcon getPhoto() {
        return photo;
    }

    public void setPhoto(ImageIcon photo) {
        this.photo = photo;
    }

    public boolean isAuthorized() {
        return authorized;
    }

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }

    public String getReasons() {
        return reasons;
    }

    public void setReasons(String reasons) {
        this.reasons = reasons;
    }

    public CAPSource getSource() {
        return source;
    }

    public void setSource(CAPSource source) {
        this.source = source;
    }
    
    public String[] toRow() {
        return new String[] {
            this.numEmployee,
            this.nameEmployee,
            this.nameEmpJob,
            this.nameEmpDept,
            format.format(this.timeStamp),
            this.authorized ? "AUTORIZADO" : "DENEGADO",
            this.source.toString()
        };
    }

    @Override
    public String toString() {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        return df.format(timeStamp) + " - " + numEmployee + "-" + nameEmployee + " / " + (authorized ? "AUTORIZADO" : "DENEGADO") + " - " + source;
    }
    
    
}
