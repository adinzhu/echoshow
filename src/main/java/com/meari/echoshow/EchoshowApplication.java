package com.meari.echoshow;

import com.amazon.speech.Sdk;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration;

@SpringBootApplication(exclude = {GsonAutoConfiguration.class})
public class EchoshowApplication {

    public static void main(String[] args) {

        setAmazonProperties();
        SpringApplication.run(EchoshowApplication.class, args);

    }

    /**
     * Sets system properties which are picked up by the .
     */
    private static void setAmazonProperties() {
        // Disable signature checks for development
        System.setProperty(Sdk.DISABLE_REQUEST_SIGNATURE_CHECK_SYSTEM_PROPERTY, "true");
        // Allow all application ids for development
        //amzn1.ask.skill.3d66031d-bac2-4ef4-88bc-c0dda8097451
        System.setProperty(Sdk.SUPPORTED_APPLICATION_IDS_SYSTEM_PROPERTY, "");
        // Disable timestamp verification for development
        System.setProperty(Sdk.TIMESTAMP_TOLERANCE_SYSTEM_PROPERTY, "");
    }


    /*@Bean
    public ServletRegistrationBean alexaServlet(EchoshowSpeechlet speechlet) {
        SpeechletServlet speechServlet = new SpeechletServlet();
        speechServlet.setSpeechlet(speechlet);

        ServletRegistrationBean servlet = new ServletRegistrationBean(speechServlet, "/alexa");
        servlet.setName("alexa");

        return servlet;
    }*/
}
