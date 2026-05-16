# InspireWorks IVR Demo System

This project is a demonstration of Plivo's Voice API capabilities, implementing an interactive voice response (IVR) system with outbound calling, OTP authentication, and multi-level branching menus.

## Features

- **Outbound Calling**: Triggers a call to a specified phone number via the Plivo Voice API.
- **OTP Authentication Layer**: Prompts the caller to enter a 4-digit OTP (hardcoded birthdate in DDMM format). Re-prompts upon incorrect entry.
- **Multi-Level IVR Menu**:
  - **Level 1**: Language selection (Press 1 for English, Press 2 for Spanish).
  - **Level 2**: Action selection (Press 1 to play a short audio message, Press 2 to connect to a live associate).
- **Graceful Error Handling**: Re-prompts the user if invalid input is received at any step.
- **Premium Frontend Dashboard**: A dark-themed, responsive web interface to trigger calls.

## Tech Stack

- **Backend**: Java 17, Spring Boot, Plivo Java SDK
- **Frontend**: HTML5, CSS3 (Vanilla), JavaScript

## Setup Instructions

### Prerequisites
- Java Development Kit (JDK) 17 or higher
- Maven
- A public URL for your local server (e.g., using [ngrok](https://ngrok.com/))

### Configuration
1. Open `src/main/resources/application.properties`.
2. Update the configuration with your Plivo credentials and settings:
   ```properties
   plivo.auth.id=MAYMYZMWEYNMM1YTA2MW
   plivo.auth.token=ZjMwYTI5NmEtMWY2Zi00ZGZkLWEyZGUtZjM5MzZh
   plivo.phone.number=+918035736861

   # IMPORTANT: Replace this with your public ngrok URL (e.g., https://abc1234.ngrok-free.app)
   app.base.url=https://YOUR_NGROK_URL

   # Hardcoded OTP - birthdate in DDMM format (e.g., May 27 = 2705)
   ivr.otp=2705

   # Placeholder number for "connect to live associate"
   ivr.associate.number=+918035736861
   ```

### Running the Application
1. Start your ngrok tunnel on port 8080:
   ```bash
   ngrok http 8080
   ```
2. Copy the forward `https` URL from ngrok and paste it into `app.base.url` in `application.properties`.
3. Build and run the Spring Boot application:
   ```bash
   mvnw spring-boot:run
   ```

## Steps to Run and Test

1. Open your web browser and navigate to `http://localhost:8080`.
2. In the InspireWorks IVR Demo dashboard, enter your destination phone number in E.164 format (e.g., `+919876543210`).
3. Click the **"Make Call"** button. The system will initiate an outbound call using the Plivo API.
4. **Answer the call** on your device.
5. **OTP Authentication**: 
   - The bot will prompt you for a 4-digit code.
   - *Test failure*: Enter a wrong code (e.g., `1234`). The bot will state it's incorrect and re-prompt you.
   - *Test success*: Enter the correct code (`2705`). The bot will confirm authentication.
6. **Level 1 (Language Menu)**: 
   - Press `1` for English or `2` for Spanish.
7. **Level 2 (Action Menu)**:
   - Press `1` to hear a sample audio message played back to you.
   - Press `2` to be forwarded to a live associate placeholder number.

## Notes
- Ensure your target phone number is verified on your Plivo trial account if you are using a trial, otherwise the call may fail.
- Receiver phone number used for testing: **[INSERT YOUR PHONE NUMBER HERE]**
