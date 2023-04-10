package helpdesk_ticketing_system.password_management_system.forgot_password_recover_system.entities;

public class ProcessCompleteRequest{
    private String password;
    private String otp;

    public ProcessCompleteRequest() {
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
