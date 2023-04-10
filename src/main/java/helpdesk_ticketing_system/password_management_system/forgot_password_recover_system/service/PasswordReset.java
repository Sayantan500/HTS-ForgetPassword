package helpdesk_ticketing_system.password_management_system.forgot_password_recover_system.service;

import helpdesk_ticketing_system.password_management_system.forgot_password_recover_system.entities.Response;

public interface PasswordReset
{
    Response verifyUserAndSendOTP(String username);
    Response confirmAndCompletePasswordResetProcess(String username, String password, String confirmationCode);
}
