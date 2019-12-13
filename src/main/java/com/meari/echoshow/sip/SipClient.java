package com.meari.echoshow.sip;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

import java.net.InetAddress;
import java.text.ParseException;
import java.util.*;
import javax.sip.*;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.header.*;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import com.meari.echoshow.await.MessageAwait;
import com.meari.echoshow.control.LoginController;
import com.meari.echoshow.pojo.Message;
import gov.nist.javax.sip.DialogTimeoutEvent;
import gov.nist.javax.sip.ListeningPointExt;
import gov.nist.javax.sip.SipListenerExt;
import gov.nist.javax.sip.SipStackExt;
import gov.nist.javax.sip.SipStackImpl;
import gov.nist.javax.sip.clientauthutils.AuthenticationHelper;
import gov.nist.javax.sip.clientauthutils.UserCredentials;
import gov.nist.javax.sip.stack.NioMessageProcessorFactory;
import gov.nist.javax.sip.stack.SIPTransactionStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @ClassName SipClient
 * @Description TODO
 * @Author huangqh
 * @CreateDate: 2019/4/25 13:46
 **/
public class SipClient implements SipListenerExt {
    private static Logger log = LoggerFactory.getLogger(SipClient.class);
    /**
     * Objects used to communicate to the JAIN SIP API.
     */
    private SipFactory sipFactory;
    private SipStack sipStack;
    private SipProvider sipProvider;
    private MessageFactory messageFactory;
    private HeaderFactory headerFactory;
    private AddressFactory addressFactory;
    private ListeningPoint listeningPoint;
    private Properties properties;

    private static SipClient sipClient;

    private String ip;
    private int port;
    private String protocol = "udp";
    private String sipServerIp;
    private int tag = (new Random()).nextInt();
    private Address contactAddress;
    private ContactHeader contactHeader;

    public static void buildSipClient(int port,String ip){
        sipClient = new SipClient(port,ip);
    }

    @Override
    public void processRequest(RequestEvent requestEvent) {
        System.out.println("RequestEvent "+requestEvent.getServerTransaction().getDialog());
    }

    @Override
    public void processResponse(ResponseEvent responseEvent) {
        log.info("<response>{}",responseEvent.getResponse());

        try{
            Response response = responseEvent.getResponse();
            String cseq = response.getHeader("CSeq").toString();
            if(cseq != null && cseq.contains(Request.INVITE) && response.getStatusCode() ==Response.OK){
                Dialog dialog = responseEvent.getClientTransaction().getDialog();
                Request ack =  dialog.createAck(2);
                dialog.sendAck( ack );
                String sdp = new String((byte[])response.getContent());
                String messageId = response.getHeader("Call-ID").toString();
                MessageAwait.signal(new Message(sdp).messageId(messageId));
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void processTimeout(TimeoutEvent timeoutEvent) {
        Transaction transaction;
        if (timeoutEvent.isServerTransaction()) {
            transaction = timeoutEvent.getServerTransaction();
        } else {
            transaction = timeoutEvent.getClientTransaction();
            System.out.println(timeoutEvent.getTimeout().getValue());
        }
        System.out.println("state = " + transaction.getState());
        System.out.println("dialog = " + transaction.getDialog());
        System.out.println("Transaction Time out");
    }

    @Override
    public void processIOException(IOExceptionEvent exceptionEvent) {
        System.out.println("Expcetion occured "+exceptionEvent.getPort());
    }

    @Override
    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {

    }

    @Override
    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
        System.out.println("processDialogTerminated" +dialogTerminatedEvent);
    }

    public SipClient(int port,String ip){
        super();
        this.port = port;
        this.sipServerIp = ip;
        log.info("sipServerIp: {}",this.sipServerIp);
        init();
    }

    private void init() {
        try {
            // Get the local IP address.
            this.ip = InetAddress.getLocalHost().getHostAddress();
            // Create the SIP factory and set the path name.
            this.sipFactory = SipFactory.getInstance();
            this.sipFactory.setPathName("gov.nist");
            // Create and set the SIP stack properties.
            this.properties = new Properties();
            this.properties.setProperty("javax.sip.STACK_NAME", "stack");
            // Create the SIP stack.
            this.sipStack = this.sipFactory.createSipStack(this.properties);
            // Create the SIP message factory.
            this.messageFactory = this.sipFactory.createMessageFactory();
            // Create the SIP header factory.
            this.headerFactory = this.sipFactory.createHeaderFactory();
            // Create the SIP address factory
            this.addressFactory = this.sipFactory.createAddressFactory();
            // Create the SIP listening point and bind it to the local IP address, port and protocol.
            this.listeningPoint = this.sipStack.createListeningPoint(this.ip, this.port, this.protocol);
            // Create the SIP provider.
            this.sipProvider = this.sipStack.createSipProvider(this.listeningPoint);
            // Add our application as a SIP listener.
            this.sipProvider.addSipListener(this);
            // Create the contact address used for all SIP messages.
            this.contactAddress = this.addressFactory.createAddress("sip:" + this.ip + ":" + this.port);
            // Create the contact header used for all SIP messages.
            this.contactHeader = this.headerFactory.createContactHeader(contactAddress);

        }
        catch(Exception e) {
            e.printStackTrace();
        }

    }

    private Request makeRequest(String fromAccount,String toAccount,String method,long cseq){
        try {
            Address addressTo = this.addressFactory.createAddress("sip:" + toAccount + "@" + sipServerIp);

            // Create the request URI for the SIP message.
            javax.sip.address.URI requestURI = addressTo.getURI();
            log.info("sendRegister: {}",requestURI);
            // The "Via" headers.
            ArrayList viaHeaders = new ArrayList();
            ViaHeader viaHeader = this.headerFactory.createViaHeader(this.ip, this.port, "udp", null);
            viaHeaders.add(viaHeader);
            // The "Max-Forwards" header.
            MaxForwardsHeader maxForwardsHeader = this.headerFactory.createMaxForwardsHeader(70);
            // The "Call-Id" header.
            CallIdHeader callIdHeader = this.sipProvider.getNewCallId();
            // The "CSeq" header.
            CSeqHeader cSeqHeader = this.headerFactory.createCSeqHeader(cseq,method);

            // Create the contact address used for all SIP messages.
            Address contactAddress = this.addressFactory.createAddress("sip:" + fromAccount + "@" + this.ip + ":" + this.port);
            // Create the contact header used for all SIP messages.
            ContactHeader contactHeader = this.headerFactory.createContactHeader(contactAddress);
            // The "From" header.
            FromHeader fromHeader = this.headerFactory.createFromHeader(contactAddress, String.valueOf(this.tag));
            // The "To" header.
            ToHeader toHeader = this.headerFactory.createToHeader(addressTo, null);

            // Create the REGISTER request.
            Request request = this.messageFactory.createRequest(
                    requestURI,
                    method,
                    callIdHeader,
                    cSeqHeader,
                    fromHeader,
                    toHeader,
                    viaHeaders,
                    maxForwardsHeader);
            // Add the "Contact" header to the request.
            request.addHeader(contactHeader);

            return request;

        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static SipClient getInstance(){
        return sipClient;
    }

    public void sendRegister(String account) {

        try {
            Request request = this.makeRequest(account,account,Request.REGISTER,1L);

            // Send the request statelessly through the SIP provider.
            this.sipProvider.sendRequest(request);

        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public String sendMessage(String fromAccount,String toAccount,String message,String type) {

        try {
            Request request = null;
            if(Request.INVITE.equals(type)){
                request = this.makeRequest(fromAccount,toAccount,Request.INVITE,2L);
                ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("application", "sdp");
                request.setContent(message, contentTypeHeader);
            }else if(Request.BYE.equals(type)){
                request = this.makeRequest(fromAccount,toAccount,Request.BYE,1000L);
            }

            if(request != null){
                // Create a new SIP client transaction.
                ClientTransaction transaction = this.sipProvider.getNewClientTransaction(request);
                // Send the request statefully, through the client transaction.
                transaction.sendRequest();
                return request.getHeader("Call-ID").toString();
            }

        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void processDialogTimeout(DialogTimeoutEvent arg0) {
        // TODO Auto-generated method stub

    }


}
