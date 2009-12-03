package Thomas_Example;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.log4j.xml.DOMConfigurator;
import org.mindswap.owl.EntityFactory;
import org.mindswap.owl.OWLFactory;
import org.mindswap.owl.OWLKnowledgeBase;
import org.mindswap.owls.OWLSFactory;
import org.mindswap.owls.process.Process;
import org.mindswap.owls.process.execution.ProcessExecutionEngine;
import org.mindswap.owls.service.Service;
import org.mindswap.query.ValueMap;

import es.upv.dsic.gti_ia.architecture.FIPARequestResponder;
import es.upv.dsic.gti_ia.architecture.MessageTemplate;
import es.upv.dsic.gti_ia.architecture.QueueAgent;
import es.upv.dsic.gti_ia.architecture.FIPANames.InteractionProtocol;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.organization.OMSProxy;
import es.upv.dsic.gti_ia.organization.Oracle;
import es.upv.dsic.gti_ia.organization.SFProcessDescription;
import es.upv.dsic.gti_ia.organization.SFProxy;

public class AgentProvider extends QueueAgent {

    private OMSProxy omsProxy = new OMSProxy();

    private SFProxy sfProxy = new SFProxy();

    private ArrayList<String> results = new ArrayList<String>();

    private Oracle oracle;

    SFProcessDescription processDescription = new SFProcessDescription(
	    "http://localhost:8080/sfservices/THservices/owl/owls/SearchCheapHotelProcess.owl",
	    "SearchCheapHotel");

    public AgentProvider(AgentID aid) throws Exception {

	super(aid);

    }

    public void escenario1() {

	try {
	    omsProxy.acquireRole(this, "member", "virtual");
	} catch (Exception e) {
	    logger.error(e.getMessage());
	}
    }



    public void escenario3() {

	try {

	    results = sfProxy.searchService(this, "SearchCheapHotel");

	    if (results.size() == 0) {
		System.out.println("no existen profiles similares al SearchCheapHotel");
	    } else {
		// cogemos el primero por ejemplo
		String URLProfile = sfProxy.getProfile(this, results.get(0));

		URL profile;
		try {
		    profile = new URL(URLProfile);
		    oracle = new Oracle(profile);

		} catch (MalformedURLException e) {
		    logger.error("ERROR: Profile URL Malformed!");
		    e.printStackTrace();
		}

	
		omsProxy.acquireRole(this, oracle.getProviderList().get(0), "travelagency");

	    }

	} catch (Exception e) {
	    logger.error(e.getMessage());
	}
    }

    public void escenario4() {
	try {

	    processDescription.setProfileID(results.get(0));

	    sfProxy.registerProcess(this, processDescription);

	} catch (Exception e) {
	    logger.error(e.getMessage());
	}

    }

    public void escenario5(){
	try{
	omsProxy.acquireRole(this,"payee", "travelagency");
	
	}catch(Exception e)
	{
	    logger.error(e.getMessage());
	}
    }
    public void escenario6() {
	// Rol responder
	Responder responder = new Responder(this);

	this.addTask(responder);
    }

    public void execute() {

	DOMConfigurator.configure("configuration/loggin.xml");
	logger.info("Executing, I'm " + getName());
	

	this.escenario1();
	this.escenario3();
	this.escenario4();
	this.escenario5();
	this.escenario6();

	// when we do not have to create more roles we await the expiration
	// of the other roles

	es.upv.dsic.gti_ia.architecture.Monitor mon = new es.upv.dsic.gti_ia.architecture.Monitor();
	mon.waiting();

    }

    /**
     * Manages the messages for the SF services
     */
    public class Responder extends FIPARequestResponder {

	OWLKnowledgeBase kb = OWLFactory.createKB();

	public Responder(QueueAgent agent) {
	    super(agent, new MessageTemplate(InteractionProtocol.FIPA_REQUEST));

	}// SFResponder

	/**
	 * Receives the messages and takes the message content. Analyzes the
	 * message content and gets the service process and input parameters to
	 * invoke the service. After the service invocation, the SF gets the
	 * answer and sends it to the requester agent.
	 * 
	 * @param
	 * @throws RuntimeException
	 */
	protected ACLMessage prepareResponse(ACLMessage msg) {

	    ACLMessage response = msg.createReply();
	    if (msg != null) {

		try {

		    // read msg content
		    StringTokenizer Tok = new StringTokenizer(msg.getContent());

		    // read in the service description
		    String token_process = Tok.nextElement().toString();

		    System.out.println("[Provider]Doc OWL-S: " + token_process);
		    Service aService = kb.readService(token_process);

		    // get the process for the server
		    Process aProcess = aService.getProcess();

		    System.out.println("AGREE");
		    response.setPerformative(ACLMessage.AGREE);
		    response.setContent(aProcess.getLocalName() + "=Agree");

		    /*
		     * }else{ System.out.println("REFUSE");
		     * response.setPerformative
		     * (jade.lang.acl.ACLMessage.REFUSE);
		     * response.setContent(aProcess.getLocalName()+"=Refuse"); }
		     */

		} catch (Exception e) {

		    System.out.println("EXCEPTION");
		    System.out.println(e);
		    e.printStackTrace();
		    throw new RuntimeException(e.getMessage());

		}

	    } else {

		System.out.println("NOTUNDERSTOOD");
		response.setPerformative(ACLMessage.NOT_UNDERSTOOD);
		response.setContent("NotUnderstood");
	    }

	    System.out.println("[Provider]Sending First message:" + response);

	    return (response);

	} // end prepareResponse

	/**
	 * This callback happens if the SF sent a positive reply to the original
	 * request (i.e. an AGREE) if the SF has agreed to supply the service,
	 * the SF has to inform the other agent that what they have asked is now
	 * complete (or if it failed)
	 * 
	 * @param inmsg
	 * @param outmsg
	 * @throws RuntimeException
	 */
	protected ACLMessage prepareResultNotification(ACLMessage inmsg, ACLMessage outmsg) {

	    ACLMessage msg = inmsg.createReply();

	    // create an execution engine
	    ProcessExecutionEngine exec = OWLSFactory.createExecutionEngine();

	    // read msg content
	    StringTokenizer Tok = new StringTokenizer(inmsg.getContent());

	    // read in the service description
	    String token_process = Tok.nextElement().toString();

	    System.out.println("[Provider]Doc OWL-S: " + token_process);

	    try {
		Service aService = kb.readService(token_process);

		// get the process for the server
		Process aProcess = aService.getProcess();
		// initialize the input values to be empty
		ValueMap values = new ValueMap();

		// get the input values
		// int n = 0;

		// int tokenCount = Tok.countTokens();
		for (int i = 0; i < aProcess.getInputs().size(); i++)
		    values.setValue(aProcess.getInputs().inputAt(i), EntityFactory
			    .createDataValue(""));
		while (Tok.hasMoreElements()) {
		    String token = Tok.nextElement().toString();
		    for (int i = 0; i < aProcess.getInputs().size(); i++) {
			String paramName = aProcess.getInputs().inputAt(i).getLocalName()
				.toLowerCase();
			if (paramName.equalsIgnoreCase(token.split("=")[0].toLowerCase())) {
			    if (token.split("=").length >= 2)
				values.setValue(aProcess.getInputs().inputAt(i), EntityFactory
					.createDataValue(token.split("=")[1]));
			    else
				values.setValue(aProcess.getInputs().inputAt(i), EntityFactory
					.createDataValue(""));
			    break;
			}
		    }
		}// end while

		// execute the service

		System.out.println("[Provider]Executing... " + values.getValues().toString());
		values = exec.execute(aProcess, values);

		System.out.println("[Provider]Values obtained... :" + values.toString());

		System.out.println("[Provider]Creating inform message to send...");

		msg.setPerformative(ACLMessage.INFORM);

		System.out.println("[Provider]Before set message content...");
		msg.setContent(aProcess.getLocalName() + "=" + values.toString());

	    } catch (Exception e) {

		System.out.println("EXCEPTION");
		System.out.println(e);
		e.printStackTrace();
		msg.setPerformative(ACLMessage.FAILURE);
	    }
	    return (msg);
	} // end prepareResultNotification

    }// end class SFResponder

}