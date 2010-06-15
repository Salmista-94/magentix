package es.upv.dsic.gti_ia.cAgents.protocols;

public abstract class FIPA_REQUEST_Participant {
	
	protected void doBegin(CProcessor myProcessor, ACLMessage msg) {
		myProcessor.getInternalData().put("InitialMessage", msg);
	}

	class BEGIN_Method implements BeginStateMethod {
		public String run(CProcessor myProcessor, ACLMessage msg) {
			doBegin(myProcessor, msg);
			return "WAIT";
		};
	}

	protected abstract String doReceiveRequest(CProcessor myProcessor,
			ACLMessage request);

	class RECEIVE_REQUEST_Method implements ReceiveStateMethod {
		public String run(CProcessor myProcessor, ACLMessage messageReceived) {
			return doReceiveRequest(myProcessor, messageReceived);
		}
	}

	protected void doNotUnderstood(CProcessor myProcessor,
			ACLMessage messageToSend) {
		messageToSend.setProtocol("fipa-request");
		messageToSend.setPerformative(ACLMessage.NOT_UNDERSTOOD);
		messageToSend.setSender(myProcessor.getMyAgent().getAid());
		messageToSend.setReceiver(myProcessor.getLastReceivedMessage().getSender());
	}

	class NOT_UNDERSTOOD_Method implements SendStateMethod {
		public String run(CProcessor myProcessor, ACLMessage messageToSend) {
			doNotUnderstood(myProcessor, messageToSend);
			return "FINAL";
		}
	}
	
	protected void doRefuse(CProcessor myProcessor,
			ACLMessage messageToSend) {
		messageToSend.setProtocol("fipa-request");
		messageToSend.setPerformative(ACLMessage.REFUSE);
		messageToSend.setSender(myProcessor.getMyAgent().getAid());
		messageToSend.setReceiver(myProcessor.getLastReceivedMessage().getSender());
	}

	class REFUSE_Method implements SendStateMethod {
		public String run(CProcessor myProcessor, ACLMessage messageToSend) {
			doRefuse(myProcessor, messageToSend);
			return "FINAL";
		}
	}
	
	protected void doAgree(CProcessor myProcessor,
			ACLMessage messageToSend) {
		messageToSend.setProtocol("fipa-request");
		messageToSend.setPerformative(ACLMessage.AGREE);
		messageToSend.setSender(myProcessor.getMyAgent().getAid());
		messageToSend.setReceiver(myProcessor.getLastReceivedMessage().getSender());
	}

	class AGREE_Method implements SendStateMethod {
		public String run(CProcessor myProcessor, ACLMessage messageToSend) {
			doAgree(myProcessor, messageToSend);
			return "ACTION";
		}
	}
	
	protected abstract String doAction(CProcessor myProcessor);
	
	class ACTION_Method implements ActionStateMethod{
		@Override
		public String run(CProcessor myProcessor) {
			return doAction(myProcessor);
		}
		
	}
	
	protected void doFailure(CProcessor myProcessor,
			ACLMessage messageToSend) {
		messageToSend.setProtocol("fipa-request");
		messageToSend.setPerformative(ACLMessage.FAILURE);
		messageToSend.setSender(myProcessor.getMyAgent().getAid());
		messageToSend.setReceiver(myProcessor.getLastReceivedMessage().getSender());
	}

	class FAILURE_Method implements SendStateMethod {
		public String run(CProcessor myProcessor, ACLMessage messageToSend) {
			doFailure(myProcessor, messageToSend);
			return "FINAL";
		}
	}
	
	protected abstract void doInform(CProcessor myProcessor, ACLMessage response);

	class INFORM_Method implements SendStateMethod {
		public String run(CProcessor myProcessor, ACLMessage messageToSend) {

			doInform(myProcessor, messageToSend);
			messageToSend.addReceiver(myProcessor.getLastReceivedMessage()
					.getSender());
			messageToSend.setSender(myProcessor.getMyAgent().getAid());
			return "FINAL";
		}
	}
	
	protected void doFinal(CProcessor myProcessor, ACLMessage messageToSend){
	}
	
	class FINAL_Method implements FinalStateMethod {
		public void run(CProcessor myProcessor, ACLMessage messageToSend) {
			 doFinal(myProcessor, messageToSend);
		}
	}

	public CProcessorFactory newFactory(String name, MessageFilter filter,
			int availableConversations, CAgent myAgent) {

		//MessageFilter filter;
		ACLMessage template;
		// Create factory

		if (filter == null) {
			filter = new MessageFilter("performative = REQUEST");//falta AND protocol = fipa-request
		}
		//template.setProtocol("REQUEST");
		//template.setPerformative(ACLMessage.REQUEST);
		CProcessorFactory theFactory = new CProcessorFactory(name, filter,
				availableConversations, myAgent);

		// Processor template setup

		CProcessor processor = theFactory.cProcessorTemplate();

		// BEGIN State

		BeginState BEGIN = (BeginState) processor.getState("BEGIN");
		BEGIN.setMethod(new BEGIN_Method());
		
		// WAIT State
		processor.registerState(new WaitState("WAIT", 0));
		processor.addTransition("BEGIN", "WAIT");
		
		// RECEIVE_REQUEST State
		ReceiveState RECEIVE_REQUEST = new ReceiveState("RECEIVE_REQUEST");
		RECEIVE_REQUEST.setMethod(new RECEIVE_REQUEST_Method());
		filter = new MessageFilter("performative = REQUEST");
		RECEIVE_REQUEST.setAcceptFilter(filter);
		processor.registerState(RECEIVE_REQUEST);
		processor.addTransition("WAIT", "RECEIVE_REQUEST");
		
		// NOT_UNDERSTOOD State

		SendState NOT_UNDERSTOOD = new SendState("NOT_UNDERSTOOD");
		NOT_UNDERSTOOD.setMethod(new NOT_UNDERSTOOD_Method());
		template = new ACLMessage(ACLMessage.NOT_UNDERSTOOD);
		template.setProtocol("fipa-request");
		template.setPerformative(ACLMessage.INFORM);
		NOT_UNDERSTOOD.setMessageTemplate(template);
		processor.registerState(NOT_UNDERSTOOD);
		processor.addTransition("RECEIVE_REQUEST", "NOT_UNDERSTOOD");
		
		// REFUSE State

		SendState REFUSE = new SendState("REFUSE");
		REFUSE.setMethod(new REFUSE_Method());
		template = new ACLMessage(ACLMessage.REFUSE);
		template.setProtocol("fipa-request");
		template.setPerformative(ACLMessage.INFORM);
		REFUSE.setMessageTemplate(template);
		processor.registerState(REFUSE);
		processor.addTransition("RECEIVE_REQUEST", "REFUSE");
		
		// AGREE State

		SendState AGREE = new SendState("AGREE");
		AGREE.setMethod(new AGREE_Method());
		template = new ACLMessage(ACLMessage.AGREE);
		template.setProtocol("fipa-request");
		template.setPerformative(ACLMessage.AGREE);
		AGREE.setMessageTemplate(template);
		processor.registerState(AGREE);
		processor.addTransition("RECEIVE_REQUEST", "AGREE");
		
		// ACTION State
		ActionState ACTION = new ActionState("ACTION");
		ACTION.setMethod(new ACTION_Method());
		processor.registerState(ACTION);
		processor.addTransition("AGREE", "ACTION");
		
		// FAILURE State

		SendState FAILURE = new SendState("FAILURE");
		FAILURE.setMethod(new FAILURE_Method());
		template = new ACLMessage(ACLMessage.FAILURE);
		template.setProtocol("fipa-request");
		FAILURE.setMessageTemplate(template);
		processor.registerState(FAILURE);
		processor.addTransition("ACTION", "FAILURE");
		
		// INFORM State

		SendState INFORM = new SendState("INFORM");
		INFORM.setMethod(new INFORM_Method());
		template = new ACLMessage(ACLMessage.INFORM);
		template.setProtocol("fipa-request");
		INFORM.setMessageTemplate(template);
		processor.registerState(INFORM);
		processor.addTransition("ACTION", "INFORM");
		

		// FINAL State

		FinalState FINAL = new FinalState("FINAL");
		
		FINAL.setMethod(new FINAL_Method());
		processor.registerState(FINAL);
		processor.addTransition("NOT_UNDERSTOOD", "FINAL");
		processor.addTransition("REFUSE", "FINAL");
		processor.addTransition("FAILURE", "FINAL");
		processor.addTransition("INFORM", "FINAL");

		// Thath's all
		return theFactory;
	}
}
