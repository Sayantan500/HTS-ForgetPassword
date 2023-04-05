package helpdesk_ticketing_system.authentication.password_management_system.forgot_password_recover_system;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import software.amazon.awssdk.http.HttpStatusCode;

import java.util.Map;

public class ForgetPasswordRequestsHandler implements RequestHandler<Map<String,String>, APIGatewayProxyResponseEvent>
{
    @Override
    public APIGatewayProxyResponseEvent handleRequest(Map<String, String> requestEvent, Context context) {
        context.getLogger().log("Received event : " + requestEvent);
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(HttpStatusCode.OK)
                .withBody(new Gson().toJson(requestEvent));
    }
}
