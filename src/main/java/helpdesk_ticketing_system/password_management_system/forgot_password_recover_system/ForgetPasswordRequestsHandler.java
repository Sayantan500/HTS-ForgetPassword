package helpdesk_ticketing_system.password_management_system.forgot_password_recover_system;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import helpdesk_ticketing_system.password_management_system.forgot_password_recover_system.entities.PasswordResetEvents;
import helpdesk_ticketing_system.password_management_system.forgot_password_recover_system.entities.ProcessCompleteRequest;
import helpdesk_ticketing_system.password_management_system.forgot_password_recover_system.entities.Response;
import helpdesk_ticketing_system.password_management_system.forgot_password_recover_system.service.PasswordReset;
import helpdesk_ticketing_system.password_management_system.forgot_password_recover_system.service.PasswordResetCognitoImpl;
import helpdesk_ticketing_system.password_management_system.forgot_password_recover_system.utility.CognitoClient;
import software.amazon.awssdk.http.HttpStatusCode;

import java.util.HashMap;
import java.util.Map;

public class ForgetPasswordRequestsHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>
{
    private final PasswordReset passwordResetService;
    private final String EVENT;
    private final String USERNAME;
    private final Gson gson;
    private final Map<String,String> headers;

    public ForgetPasswordRequestsHandler() {
        passwordResetService = new PasswordResetCognitoImpl(
                new CognitoClient()
        );
        this.EVENT = System.getenv("event_header_param_name");
        this.USERNAME = System.getenv("username_path_param_name");
        this.gson = new Gson();
        headers = new HashMap<>();
        headers.put("Content-Type","application/json; charset=utf-8");
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        context.getLogger().log("Received event : " + requestEvent);

        //when the event parameter is missing from the header of the data coming from AWS API Gateway
        if(!requestEvent.getHeaders().containsKey(EVENT))
        {
            context.getLogger().log(
                    String.format("Header '%s' is missing from Input Event Object of Lambda", EVENT)
            );
            return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR);
        }

        String eventName = requestEvent.getHeaders().get(EVENT);
        String username = requestEvent.getPathParameters().get(USERNAME);

        //when the event parameter is present and value tells to start the reset process
        if(eventName.compareToIgnoreCase(PasswordResetEvents.START_RESET_PROCESS.toString())==0)
        {
            //when the username contains data as expected
            Response resetInitResponse = passwordResetService.verifyUserAndSendOTP(username);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(resetInitResponse.getStatus())
                    .withHeaders(headers)
                    .withBody(gson.toJson(resetInitResponse));

        }
        else if (eventName.compareToIgnoreCase(PasswordResetEvents.CONFIRM_AND_COMPLETE_PROCESS.toString())==0)
        {
            String requestEventBodyJson = requestEvent.getBody();
            //deserializing the json request body
            ProcessCompleteRequest processCompleteRequest =
                    gson.fromJson(requestEventBodyJson,ProcessCompleteRequest.class);

            String password = processCompleteRequest.getPassword();
            String confirmationCode = processCompleteRequest.getOtp();

            Response processCompletionResponse =
                    passwordResetService.confirmAndCompletePasswordResetProcess(username,password,confirmationCode);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(processCompletionResponse.getStatus())
                    .withHeaders(headers)
                    .withBody(gson.toJson(processCompletionResponse));
        }

        //if the event name is an Invalid one set by AWS API Gateway
        context.getLogger().log(
                String.format("Header '%s' has an Invalid Value %s", EVENT,eventName)
        );
        return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
}
