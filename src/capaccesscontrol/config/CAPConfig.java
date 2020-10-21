/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package capaccesscontrol.config;

/**
 *
 * @author Edwin Carmona
 */
public class CAPConfig {
    protected int countLog;
    protected int searchScheduleDays;
    protected int minPrevSchedule;
    protected int minPostSchedule;
    protected String mailCAP;
    protected String pswdCAP;
    protected String urlLogin;
    protected String urlNumEmployee;
    protected String urlIdEmployee;
    protected String urlGetEmployees;
    protected CAPConnection siieConnection;
    protected CAPConnection capConnection;
    protected CAPCompanyData companyData;

    public int getCountLog() {
        return countLog;
    }

    public void setCountLog(int countLog) {
        this.countLog = countLog;
    }

    public int getSearchScheduleDays() {
        return searchScheduleDays;
    }

    public void setSearchScheduleDays(int searchScheduleDays) {
        this.searchScheduleDays = searchScheduleDays;
    }

    public int getMinPrevSchedule() {
        return minPrevSchedule;
    }

    public void setMinPrevSchedule(int minPrevSchedule) {
        this.minPrevSchedule = minPrevSchedule;
    }

    public int getMinPostSchedule() {
        return minPostSchedule;
    }

    public void setMinPostSchedule(int minPostSchedule) {
        this.minPostSchedule = minPostSchedule;
    }

    public String getMailCAP() {
        return mailCAP;
    }

    public void setMailCAP(String mailCAP) {
        this.mailCAP = mailCAP;
    }

    public String getPswdCAP() {
        return pswdCAP;
    }

    public void setPswdCAP(String pswdCAP) {
        this.pswdCAP = pswdCAP;
    }

    public String getUrlLogin() {
        return urlLogin;
    }

    public void setUrlLogin(String urlLogin) {
        this.urlLogin = urlLogin;
    }

    public String getUrlNumEmployee() {
        return urlNumEmployee;
    }

    public void setUrlNumEmployee(String urlNumEmployee) {
        this.urlNumEmployee = urlNumEmployee;
    }

    public String getUrlIdEmployee() {
        return urlIdEmployee;
    }

    public void setUrlIdEmployee(String urlIdEmployee) {
        this.urlIdEmployee = urlIdEmployee;
    }

    public String getUrlGetEmployees() {
        return urlGetEmployees;
    }

    public void setUrlGetEmployees(String urlGetEmployees) {
        this.urlGetEmployees = urlGetEmployees;
    }

    public CAPConnection getSiieConnection() {
        return siieConnection;
    }

    public void setSiieConnection(CAPConnection siieConnection) {
        this.siieConnection = siieConnection;
    }

    public CAPConnection getCapConnection() {
        return capConnection;
    }

    public void setCapConnection(CAPConnection capConnection) {
        this.capConnection = capConnection;
    }

    public CAPCompanyData getCompanyData() {
        return companyData;
    }

    public void setCompanyData(CAPCompanyData companyData) {
        this.companyData = companyData;
    }
    
    
}
