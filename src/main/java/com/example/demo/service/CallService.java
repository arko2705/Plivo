package com.example.demo.service;

import com.plivo.api.models.call.Call;
import com.plivo.api.models.call.CallCreateResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Service to initiate outbound calls via Plivo's Voice API.
 */
@Service
public class CallService {

    @Value("${plivo.phone.number}")
    private String fromNumber;

    @Value("${app.base.url}")
    private String baseUrl;

    /**
     * Makes an outbound call to the given phone number.
     * When the call is answered, Plivo will hit the /ivr/answer endpoint.
     *
     * @param toNumber the destination phone number (E.164 format, e.g.
     *                 +919876543210)
     * @return the call request UUID from Plivo
     */
    public String makeCall(String toNumber) throws Exception {
        String answerUrl = baseUrl + "/ivr/answer";

        CallCreateResponse response = Call.creator(
                fromNumber, // from
                Collections.singletonList(toNumber), // to (list of destination numbers)
                answerUrl // url (webhook Plivo hits on answer)
        ).create();

        String callSid = response.getRequestUuid();
        System.out.println("Outbound call initiated -> To: " + toNumber + " | SID: " + callSid);
        return callSid;
    }
}
