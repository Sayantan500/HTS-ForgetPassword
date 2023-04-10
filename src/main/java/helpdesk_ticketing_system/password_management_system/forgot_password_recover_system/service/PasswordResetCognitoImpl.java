package helpdesk_ticketing_system.password_management_system.forgot_password_recover_system.service;

import helpdesk_ticketing_system.password_management_system.forgot_password_recover_system.entities.Response;
import helpdesk_ticketing_system.password_management_system.forgot_password_recover_system.utility.CognitoClient;
import helpdesk_ticketing_system.password_management_system.forgot_password_recover_system.utility.HashingUtils;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;
import software.amazon.awssdk.utils.StringUtils;

public class PasswordResetCognitoImpl implements PasswordReset
{
    private final CognitoClient cognitoClient;

    public PasswordResetCognitoImpl(CognitoClient cognitoClient) {
        this.cognitoClient = cognitoClient;
    }

    @Override
    public Response verifyUserAndSendOTP(String username) {
        //if the username is blank or empty
        if(!isParameterValid(username))
            return new Response(HttpStatusCode.BAD_REQUEST,"Username must not be empty.");

        //if the username is incorrect or invalid
        if(!isUsernameFound(username))
            return new Response(HttpStatusCode.NOT_FOUND,"USER NOT FOUND");

        ForgotPasswordRequest forgotPasswordRequest =
                ForgotPasswordRequest.builder()
                        .clientId(cognitoClient.getClientID())
                        .username(username)
                        .secretHash(HashingUtils.computeSecretHash(
                                cognitoClient.getClientID(),
                                cognitoClient.getClientSecret(),
                                username
                        ))
                        .build();

        try{
            SdkHttpResponse forgotPasswordResponse =
                    cognitoClient.getCognitoIdentityProviderClient().forgotPassword(forgotPasswordRequest).sdkHttpResponse();
            String message = forgotPasswordResponse.statusText().isPresent()?forgotPasswordResponse.statusText().get() : null;
            return new Response(forgotPasswordResponse.statusCode(),message);
        }
        catch (Exception exception){
            return new Response(HttpStatusCode.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @Override
    public Response confirmAndCompletePasswordResetProcess(String username, String password, String confirmationCode) {
        //if all the required parameters are valid strings i.e. not null, without whitespaces or not blank
        if(isParameterValid(username) && isParameterValid(password) && isParameterValid(confirmationCode))
        {
            ConfirmForgotPasswordRequest confirmForgotPasswordRequest =
                    ConfirmForgotPasswordRequest.builder()
                            .clientId(cognitoClient.getClientID())
                            .confirmationCode(confirmationCode)
                            .username(username)
                            .password(password)
                            .secretHash(HashingUtils.computeSecretHash(
                                    cognitoClient.getClientID(),
                                    cognitoClient.getClientSecret(),
                                    username
                            ))
                            .build();

            try {
                cognitoClient.getCognitoIdentityProviderClient().confirmForgotPassword(confirmForgotPasswordRequest);
                return new Response(HttpStatusCode.OK,"New Password Saved.");
            }
            catch (InvalidPasswordException invalidPasswordException) {
                return new Response(
                        HttpStatusCode.BAD_REQUEST,
                        "INVALID PASSWORD : " + invalidPasswordException.getMessage()
                );
            }
            catch (UserNotFoundException | NotAuthorizedException exception){
                return new Response(HttpStatusCode.NOT_FOUND,"USER NOT FOUND.");
            }
            catch (ExpiredCodeException expiredCodeException){
                return new Response(HttpStatusCode.BAD_REQUEST, "OTP EXPIRED.");
            }
            catch (CodeMismatchException codeMismatchException){
                return new Response(HttpStatusCode.UNAUTHORIZED, "INVALID OTP PROVIDED.");
            }
            catch (Exception e){
                System.out.println(e.getClass());
                System.out.println(e.getMessage());
                return new Response(HttpStatusCode.INTERNAL_SERVER_ERROR,"INTERNAL_SERVER_ERROR");
            }
        }
        return new Response(HttpStatusCode.BAD_REQUEST,"Username, Password or OTP must not be empty.");
    }

    private boolean isParameterValid(String parameter) {
        return StringUtils.isNotBlank(parameter);
    }

    private boolean isUsernameFound(String username) {
        try{
            cognitoClient.getCognitoIdentityProviderClient()
                    .adminGetUser(AdminGetUserRequest.builder()
                            .username(username)
                            .userPoolId(cognitoClient.getUserPoolId())
                            .build());
            return true;
        }catch (UserNotFoundException userNotFoundException){
            return false;
        }
    }
}
