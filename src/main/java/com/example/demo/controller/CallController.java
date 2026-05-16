package com.example.demo.controller;

import com.example.demo.service.CallService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller to trigger outbound calls from the frontend.
 */
@RestController
@RequestMapping("/api")
public class CallController {

    private final CallService callService;

    public CallController(CallService callService) {
        this.callService = callService;
    }

    /**
     * POST /api/call
     * Body: { "phoneNumber": "+919876543210" }
     *
     * Initiates an outbound call to the given phone number.
     */
    @PostMapping("/call")
    public ResponseEntity<?> initiateCall(@RequestBody Map<String, String> request) {
        String phoneNumber = request.get("phoneNumber");

        if (phoneNumber == null || phoneNumber.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Phone number is required"
            ));
        }

        try {
            String callUuid = callService.makeCall(phoneNumber);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Call initiated successfully",
                "callUuid", callUuid
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to initiate call: " + e.getMessage()
            ));
        }
    }
}
