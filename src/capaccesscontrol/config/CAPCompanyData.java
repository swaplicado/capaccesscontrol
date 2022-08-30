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
public class CAPCompanyData {
    protected String companyName;
    protected String companyImage;
    protected String welcomeImage;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyImage() {
        return companyImage;
    }

    public void setCompanyImage(String companyImage) {
        this.companyImage = companyImage;
    }

    public String getWelcomeImage() {
        return welcomeImage;
    }

    public void setWelcomeImage(String welcomeImage) {
        this.welcomeImage = welcomeImage;
    }
}
