package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * IVR Webhook Controller — handles all Plivo XML callback endpoints.
 *
 * Call Flow:
 *   1. /ivr/answer        → Prompts for OTP (4-digit DDMM birthdate)
 *   2. /ivr/verify-otp    → Validates OTP, re-prompts if wrong, proceeds to language menu
 *   3. /ivr/language-menu → Level 1: Language selection (English / Spanish)
 *   4. /ivr/main-menu     → Level 2: Play audio message or connect to associate
 */
@RestController
@RequestMapping("/ivr")
public class IvrController {

    @Value("${ivr.otp}")
    private String correctOtp;

    @Value("${app.base.url}")
    private String baseUrl;

    @Value("${ivr.associate.number}")
    private String associateNumber;

    @Value("${plivo.phone.number}")
    private String plivoNumber;

    // ─── A publicly hosted sample audio file ───
    private static final String SAMPLE_AUDIO_URL =
            "https://s3.amazonaws.com/plivocloud/Phloops.mp3";

    // ─────────────────────────────────────────────
    //  STEP 1: Answer URL — Prompt for OTP
    // ─────────────────────────────────────────────
    /**
     * When the call is answered, this endpoint is hit by Plivo.
     * It prompts the caller to enter their 4-digit OTP.
     */
    @PostMapping(value = "/answer", produces = MediaType.APPLICATION_XML_VALUE)
    public String answerCall() {
        System.out.println("📞 Call answered — prompting for OTP");
        return buildOtpPromptXml();
    }

    // ─────────────────────────────────────────────
    //  STEP 2: Verify OTP
    // ─────────────────────────────────────────────
    /**
     * Receives the digits the caller entered.
     * If correct → proceed to language menu.
     * If wrong  → re-prompt until correct.
     */
    @PostMapping(value = "/verify-otp", produces = MediaType.APPLICATION_XML_VALUE)
    public String verifyOtp(@RequestParam(value = "Digits", required = false) String digits) {
        System.out.println("🔐 OTP received: " + digits);

        if (digits != null && digits.equals(correctOtp)) {
            // OTP correct → go to language selection
            System.out.println("✅ OTP verified successfully");
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                 + "<Response>"
                 +   "<Speak voice=\"WOMAN\">Authentication successful. Welcome to InspireWorks.</Speak>"
                 +   "<Redirect method=\"POST\">" + baseUrl + "/ivr/language-menu</Redirect>"
                 + "</Response>";
        } else {
            // OTP wrong → re-prompt
            System.out.println("❌ Incorrect OTP — re-prompting");
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                 + "<Response>"
                 +   "<Speak voice=\"WOMAN\">Incorrect code. Please try again.</Speak>"
                 +   buildGetDigitsBlock(
                         baseUrl + "/ivr/verify-otp",
                         4,
                         "Please enter your 4 digit authentication code."
                     )
                 +   "<Speak voice=\"WOMAN\">We did not receive any input. Goodbye.</Speak>"
                 + "</Response>";
        }
    }

    // ─────────────────────────────────────────────
    //  STEP 3: Language Selection (Level 1 IVR)
    // ─────────────────────────────────────────────
    /**
     * Level 1 menu: Select language.
     * Press 1 → English
     * Press 2 → Spanish
     */
    @PostMapping(value = "/language-menu", produces = MediaType.APPLICATION_XML_VALUE)
    public String languageMenu() {
        System.out.println("🌐 Language menu presented");
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
             + "<Response>"
             +   "<GetDigits action=\"" + baseUrl + "/ivr/main-menu\" method=\"POST\" "
             +     "numDigits=\"1\" timeout=\"10\" retries=\"2\" validDigits=\"12\">"
             +     "<Speak voice=\"WOMAN\">"
             +       "Press 1 for English. Press 2 for Spanish."
             +     "</Speak>"
             +   "</GetDigits>"
             +   "<Speak voice=\"WOMAN\">No input received. Goodbye.</Speak>"
             + "</Response>";
    }

    // ─────────────────────────────────────────────
    //  STEP 4: Main Menu (Level 2 IVR)
    // ─────────────────────────────────────────────
    /**
     * Level 2 menu: Based on language choice.
     * Press 1 → Play a short audio message
     * Press 2 → Connect to a live associate
     */
    @PostMapping(value = "/main-menu", produces = MediaType.APPLICATION_XML_VALUE)
    public String mainMenu(@RequestParam(value = "Digits", required = false) String langDigit) {
        System.out.println("🗣️ Language selected: " + langDigit);

        boolean isSpanish = "2".equals(langDigit);
        String actionUrl = isSpanish
                ? baseUrl + "/ivr/action-es"
                : baseUrl + "/ivr/action-en";

        String prompt = isSpanish
                ? "Presione 1 para escuchar un mensaje. Presione 2 para hablar con un asociado."
                : "Press 1 to hear a short message. Press 2 to speak with a live associate.";

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
             + "<Response>"
             +   "<GetDigits action=\"" + actionUrl + "\" method=\"POST\" "
             +     "numDigits=\"1\" timeout=\"10\" retries=\"2\" validDigits=\"12\">"
             +     "<Speak voice=\"WOMAN\">" + prompt + "</Speak>"
             +   "</GetDigits>"
             +   "<Speak voice=\"WOMAN\">"
             +     (isSpanish ? "No se recibio entrada. Adios." : "No input received. Goodbye.")
             +   "</Speak>"
             + "</Response>";
    }

    // ─────────────────────────────────────────────
    //  STEP 5a: Action Handler — English
    // ─────────────────────────────────────────────
    @PostMapping(value = "/action-en", produces = MediaType.APPLICATION_XML_VALUE)
    public String handleActionEnglish(@RequestParam(value = "Digits", required = false) String digit) {
        System.out.println("🎯 English action selected: " + digit);
        return handleAction(digit, false);
    }

    // ─────────────────────────────────────────────
    //  STEP 5b: Action Handler — Spanish
    // ─────────────────────────────────────────────
    @PostMapping(value = "/action-es", produces = MediaType.APPLICATION_XML_VALUE)
    public String handleActionSpanish(@RequestParam(value = "Digits", required = false) String digit) {
        System.out.println("🎯 Spanish action selected: " + digit);
        return handleAction(digit, true);
    }

    // ─────────────────────────────────────────────
    //  Private Helpers
    // ─────────────────────────────────────────────

    /**
     * Handles Level 2 action: play audio or connect to associate.
     */
    private String handleAction(String digit, boolean isSpanish) {
        if ("1".equals(digit)) {
            // Play a short audio message
            String msg = isSpanish
                    ? "Reproduciendo su mensaje ahora."
                    : "Playing your message now.";
            String goodbye = isSpanish
                    ? "Gracias por llamar a InspireWorks. Adios."
                    : "Thank you for calling InspireWorks. Goodbye.";

            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                 + "<Response>"
                 +   "<Speak voice=\"WOMAN\">" + msg + "</Speak>"
                 +   "<Play>" + SAMPLE_AUDIO_URL + "</Play>"
                 +   "<Speak voice=\"WOMAN\">" + goodbye + "</Speak>"
                 + "</Response>";

        } else if ("2".equals(digit)) {
            // Connect to a live associate
            String msg = isSpanish
                    ? "Conectando con un asociado. Por favor espere."
                    : "Connecting you to a live associate. Please hold.";

            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                 + "<Response>"
                 +   "<Speak voice=\"WOMAN\">" + msg + "</Speak>"
                 +   "<Dial callerId=\"" + plivoNumber + "\">"
                 +     "<Number>" + associateNumber + "</Number>"
                 +   "</Dial>"
                 + "</Response>";

        } else {
            // Invalid input → re-prompt by redirecting back to main menu
            String lang = isSpanish ? "2" : "1";
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                 + "<Response>"
                 +   "<Speak voice=\"WOMAN\">"
                 +     (isSpanish ? "Opcion no valida." : "Invalid option.")
                 +   "</Speak>"
                 +   "<Redirect method=\"POST\">" + baseUrl + "/ivr/main-menu?Digits=" + lang + "</Redirect>"
                 + "</Response>";
        }
    }

    /**
     * Builds the initial OTP prompt XML.
     */
    private String buildOtpPromptXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
             + "<Response>"
             +   "<Speak voice=\"WOMAN\">"
             +     "Welcome to InspireWorks. For security, please verify your identity."
             +   "</Speak>"
             +   buildGetDigitsBlock(
                     baseUrl + "/ivr/verify-otp",
                     4,
                     "Please enter your 4 digit authentication code."
                 )
             +   "<Speak voice=\"WOMAN\">We did not receive any input. Goodbye.</Speak>"
             + "</Response>";
    }

    /**
     * Helper to build a GetDigits XML block.
     */
    private String buildGetDigitsBlock(String actionUrl, int numDigits, String prompt) {
        return "<GetDigits action=\"" + actionUrl + "\" method=\"POST\" "
             +   "numDigits=\"" + numDigits + "\" timeout=\"15\" retries=\"3\" finishOnKey=\"\">"
             +   "<Speak voice=\"WOMAN\">" + prompt + "</Speak>"
             + "</GetDigits>";
    }
}
