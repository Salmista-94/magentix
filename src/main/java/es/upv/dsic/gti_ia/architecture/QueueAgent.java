/**
 * Create a new QueueAgent.
 * 
 * @author  Joan Bellver Faus, GTI-IA, DSIC, UPV
 * @version 2009.9.07
 */

package es.upv.dsic.gti_ia.architecture;

import java.util.ArrayList;
import org.apache.log4j.Logger;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.BaseAgent;

/**
 * Class QueueAgent extends BaseAgent
 * 
 * @author jbellver
 * 
 */

public class QueueAgent extends BaseAgent {

	static Logger logger = Logger.getLogger(QueueAgent.class);

	private ArrayList<ACLMessage> messageList = new ArrayList<ACLMessage>();

	private Monitor monitor = null;

	// para poder diferenciar cuando nos llega una conversaci� nueva
	private ArrayList<String> activeConversationsList = new ArrayList<String>();
	// almacena la informacion de los servicios en thomas
	// private ArrayList<SFAgentDescription> agentDescriptions = new
	// ArrayList<SFAgentDescription>();

	private ArrayList<Object> roles = new ArrayList<Object>();

	/**
	 * Create a new QueueAgent.
	 * 
	 * @param aid
	 *            agent ID.
	 * @param connection
	 *            connection with the broker.
	 */

	public QueueAgent(AgentID aid) throws Exception {
		super(aid);

	}

	/**
	 * Function that will be executed when the agent gets a message
	 * 
	 * @param msg
	 */
	protected void onMessage(ACLMessage msg) {

		this.writeQueue(msg);

		// clase encargada de despertar al agente, puede ser del rol responder o
		// del rol iniciator

		if (monitor != null)
			this.monitor.advise();

	}

	private synchronized void writeQueue(ACLMessage msg) {
		messageList.add(msg);
	}

	/**
	 * Method to receive a magentix2 AclMessage
	 * 
	 * @param template
	 * @return an ACLMessage
	 */
	public synchronized ACLMessage receiveACLMessageSimple(
			MessageTemplate template) throws Exception {
		ACLMessage msgselect = null;

		if (template.getProtocol() == "")
			throw new Exception("The protocol field is empty");

		for (ACLMessage msg : messageList) {
			// comparamos los campos protocol y conversaci�nID (para
			// asegurarnos
			// que no es una conversacion existente)00
			if (template.getProtocol().equals(msg.getProtocol())) {
				// comprobar que sea una conversacion nueva, que no este en la
				// lista de conversaciones activas
				msgselect = msg;

			}

		}
		if (msgselect != null) {
			messageList.remove(msgselect);
		}
		return msgselect;
	}

	/**
	 * Method to receive a magentix2 AclMessage
	 * 
	 * @param template
	 * @param tipo
	 *            1 = rol responder other = rol initiator
	 * @return an ACLMessage
	 */
	synchronized ACLMessage receiveACLMessage(MessageTemplate template, int tipo) {
		ACLMessage msgselect = null;

		if (tipo == 1) {
			for (ACLMessage msg : messageList) {
				// comparamos los campos protocol y conversaci�nID (para
				// asegurarnos
				// que no es una conversacion existente)00

				if (template.getProtocol().equals(msg.getProtocol())) {
					// comprobar que sea una conversacion nueva, que no este en
					// la
					// lista de conversaciones activas
					msgselect = msg;
					for (String conv : this.activeConversationsList) {
						// si existe, entonces debera trartalo el rol de
						// iniciador
						if (conv.equals(msg.getConversationId())) {
							msgselect = null;
							break;
						}
					}
				}

			}
		} else {
			for (ACLMessage msg : messageList) {
				// comparamos los campos protocol, idcoversaci�n y sender
				if (template.getProtocol().equals(msg.getProtocol())) {
					// miramos dentro de las conversaciones que tenemos
					for (String conversacion : template.getList_Conversation())
						if (conversacion.equals(msg.getConversationId())) {
							// miramos si pertenece algun agente
							if (template.existReceiver(msg.getSender())) {
								msgselect = msg;
								break;
							}
						}
				}
				if (msgselect != null)
					break;
			}
		}
		if (msgselect != null) {
			messageList.remove(msgselect);
		}
		return msgselect;
	}

	/**
	 * 
	 * @return String name
	 */
	public String getAllName() {
		return this.getAid().toString();
	}

	/**
	 * 
	 * @return int number of roles
	 */
	private synchronized int addRole(Object b) {
		this.roles.add(b);

		// this.nRoles++;
		// return this.nRoles;
		return this.roles.size();
	}

	/**
	 * 
	 * @return int remove a role
	 */
	private synchronized int removeRole(Object b) {
		this.roles.remove(b);
		// this.nRoles--;
		// return this.nRoles;
		return this.roles.size();
	}

	/**
	 * 
	 * @return int number of roles
	 */
	private synchronized int getnRole() {
		// return this.nRoles;
		return this.roles.size();
	}

	/**
	 * Add new monitor
	 * 
	 * @return Monitor
	 */
	synchronized Monitor addMonitor(Object b) {
		this.addRole(b);
		if (this.monitor == null)
			this.monitor = new Monitor();
		return monitor;
	}

	synchronized void deleteMonitor(Object b) {
		this.removeRole(b);
		if (this.roles.size() == 0)
			this.monitor = null;
	}

	synchronized void setActiveConversation(String agentID) {
		this.activeConversationsList.add(agentID);

	}

	/**
	 * Remove a agentID of the array of the active conversations.
	 * 
	 * @param agentID
	 */
	synchronized void deleteActiveConversation(String agentID) {
		for (String conv : this.activeConversationsList) {
			if (conv.equals(agentID)) {
				this.activeConversationsList.remove(agentID);
				break;
			}
		}
	}

	/**
	 * Remove all active conversations.
	 * 
	 * @return boolean value
	 */
	synchronized boolean deleteAllActiveConversation() {
		this.activeConversationsList.clear();
		if (this.activeConversationsList.size() == 0)
			return true;
		else
			return false;

	}

	/**
	 * Return the monitor
	 * 
	 * @return Monitor
	 */
	synchronized Monitor getMonitor() {
		return this.monitor;
	}

	protected void terminate() {
		// mirar todos los roles activos

		this.finalize();

		if (this.getnRole() == 0)
			logger.info("Finish ,active roles do not exist");
		else {

			for (Object obj : this.roles) {

				String patron;

				patron = obj.getClass().getSuperclass().getName().substring(
						obj.getClass().getSuperclass().getName().lastIndexOf(
								".") + 1,
						obj.getClass().getSuperclass().getName().length());

				if (patron.equals("FIPARequestInitiator"))

				{
					logger.info("Finish with role Resquest Initiator, state:  "
							+ ((FIPARequestInitiator) obj).getState());

				} else if (patron.equals("FIPARequestResponder")) {

					logger
							.info("Finish with role Responder, protocol:Request, state:  "
									+ ((FIPARequestResponder) obj).getState());
				}
				if (patron.equals("FIPAQueryInitiator")) {

					logger
							.info("Finish with role Initiator, protocol:Query, state:  "
									+ ((FIPAQueryInitiator) obj).getState());
				} else if (patron.equals("FIPAQueryResponder")) {

					logger
							.info("Finish with role Responder, protocol:Query, state:  "
									+ ((FIPAQueryResponder) obj).getState());
				}
				if (patron.equals("FIPAContractNetInitiator")) {

					logger
							.info("Finish with role Initiator, protocol:Contract-Net state:  "
									+ ((FIPAContractNetInitiator) obj)
											.getState());
				} else if (patron.equals("FIPAContractNetResponder")) {

					logger
							.info("Finish with role Responder, protocol:Contract-Net state:  "
									+ ((FIPAContractNetResponder) obj)
											.getState());
				}

			}
		}

		super.terminate();

	}

	/**
	 * Adds a new task (FIPA protocol) to the agent,was creating a new thread
	 * 
	 * @param obj
	 *            object of type FIPA protocol
	 */
	public void setTask(Object obj) {

		String patron;

		patron = obj.getClass().getSuperclass().getName().substring(
				obj.getClass().getSuperclass().getName().lastIndexOf(".") + 1,
				obj.getClass().getSuperclass().getName().length());
		if (patron.equals("FIPARequestInitiator"))

		{

			ThreadInitiator h = new ThreadInitiator(obj, 1);
			h.start();

		} else if (patron.equals("FIPARequestResponder")) {

			ThreadResponder h = new ThreadResponder(obj, 1);
			h.start();
		}
		if (patron.equals("FIPAQueryInitiator")) {

			ThreadInitiator h = new ThreadInitiator(obj, 2);
			h.start();
		} else if (patron.equals("FIPAQueryResponder")) {

			ThreadResponder h = new ThreadResponder(obj, 2);
			h.start();
		}
		if (patron.equals("FIPAContractNetInitiator")) {

			ThreadInitiator h = new ThreadInitiator(obj, 3);
			h.start();
		} else if (patron.equals("FIPAContractNetResponder")) {

			ThreadResponder h = new ThreadResponder(obj, 3);
			h.start();
		}

	}

	// #APIDOC_EXCLUDE_BEGIN
	public class ThreadInitiator extends Thread {

		Object iniciador;
		int tipo;

		public ThreadInitiator(Object in, int tipo) {
			iniciador = in;
			this.tipo = tipo;
		}

		public void run() {

			switch (tipo) {
			case 1: {
				do {
					((FIPARequestInitiator) iniciador).action();
				} while (!((FIPARequestInitiator) iniciador).finished());
				break;
			}
			case 2: {
				do {
					((FIPAQueryInitiator) iniciador).action();
				} while (!((FIPAQueryInitiator) iniciador).finished());
				break;
			}
			case 3: {
				do {
					((FIPAContractNetInitiator) iniciador).action();
				} while (!((FIPAContractNetInitiator) iniciador).finished());
				break;
			}
			}
		}
	}

	public class ThreadResponder extends Thread {
		Object responder;
		int tipo;

		public ThreadResponder(Object res, int tipo) {
			responder = res;
			this.tipo = tipo;
		}

		public void run() {
			switch (tipo) {
			case 1: {
				do {
					((FIPARequestResponder) responder).action();
				} while (true);

			}
			case 2: {
				do {
					((FIPAQueryResponder) responder).action();
				} while (true);
			}
			case 3: {
				do {
					((FIPAContractNetResponder) responder).action();
				} while (true);
			}

			}
		}
	}
	// #APIDOC_EXCLUDE_END

}
