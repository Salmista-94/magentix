package es.upv.dsic.gti_ia.organization;


import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import es.upv.dsic.gti_ia.architecture.FIPARequestInitiator;
import es.upv.dsic.gti_ia.architecture.QueueAgent;
import es.upv.dsic.gti_ia.architecture.FIPANames.InteractionProtocol;
import es.upv.dsic.gti_ia.cAgents.CAgent;
import es.upv.dsic.gti_ia.cAgents.CProcessor;
import es.upv.dsic.gti_ia.cAgents.CProcessorFactory;
import es.upv.dsic.gti_ia.cAgents.protocols.FIPA_REQUEST_Initiator;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.BaseAgent;




public class THOMASProxy {
	
	
	
	//******************************************VARIABLES*********************************************

	static Logger logger = Logger.getLogger(THOMASProxy.class);
	
	
	BaseAgent agent = null;
	Oracle oracle;
	
	Configuration c;
	
	String call,thomasAgent,clientProvider,serviceName,ErrorValue,status,ServiceDescriptionLocation;
	String value = ""; //Valor devuelto
	
	int Quantity;
	String[] elements;
	
	Hashtable<AgentID, String> agents = new Hashtable<AgentID, String>();
	ArrayList<String> listResults = new ArrayList<String>();
	
	
	ProcessDescription processDescripcion;
	ProfileDescription profileDescription;
	
	boolean isgenericSerice = false;
	
	private Hashtable<String, String> genericServiceList = new Hashtable<String, String>();
	private String[] agentGetProcess;
	private boolean Status = true;
	
	private ArrayList<String> serviceType1 = new ArrayList<String>();//Devuelven String
	private ArrayList<String> serviceType2 = new ArrayList<String>();//Devuelven Array de strings
	private ArrayList<String> serviceType3 = new ArrayList<String>();//Devuelven Entero
	private ArrayList<String> serviceType4 = new ArrayList<String>();//Devuelven Hastable<AgentID, String>
	
	
	
	
	//**********************************CONSTRUCTORS******************************************
	
	/**
	 * This class gives us the support to accede to the services of the OMS and SF
	 * @param agent,
	 *            is a BaseAgent, this agent implemented the communication
	 *            protocol
	 *            
	 * @param thomasAgent, is a OMS or SF.
	 * @param ServiceDesciptionLocation
	 *            The URL where the owl's document is located.
	 */
	public THOMASProxy(BaseAgent agent, String thomasAgent,String ServiceDescriptionLocation) {
		this.agent = agent;
		this.ServiceDescriptionLocation = ServiceDescriptionLocation;
	}

	/**
	 * This class gives us the support to access to the services of the OMS and SF.
	 * Checked that the data contained in the file configuration/Settings.xml, the URL
	 * ServiceDescriptionLocation is not empty and is the correct path.
	 * 
	 * @param agent,
	 *            is a BaseAgent, this agent implemented the communication
	 *            protocol
	 * @param thomasAgent, is a OMS or SF.
	 * 
	 */
	public THOMASProxy(BaseAgent agent, String thomasAgent) {
		
		this.agent = agent;
		this.thomasAgent = thomasAgent;
		c = Configuration.getConfiguration();
		this.initialize();
		
	}
	

	private void addElementToList(String element) {
		this.listResults.add(element);
	}

	private void setQuantity(int Quantity) {
		this.Quantity = Quantity;
	}
	
	void initialize()
	{
		
		//Añade cada servicio al tipo que le corresponde.
		serviceType1.add("LeaveRoleProcess");
		serviceType1.add("AcquireRoleProcess");
		serviceType1.add("RegisterNormProcess");
		serviceType1.add("RegisterRoleProcess");
		serviceType1.add("RegisterUnitProcess");
		serviceType1.add("DeregisterNormProcess");
		serviceType1.add("DeregisterRoleProcess");
		serviceType1.add("DeregisterUnitProcess");
		serviceType1.add("ExpulseProcess");
		serviceType1.add("RemoveProviderProcess");
		serviceType1.add("ModifyProcessProcess");
		serviceType1.add("ModifyProfileProcess");
		serviceType1.add("DeregisterProfileProcess");
		serviceType1.add("GetProfileProcess");
		serviceType1.add("RegisterProfileProcess");
		serviceType1.add("RegisterProcessProcess");
		
		
		serviceType2.add("InformAgentRoleProcess");
		serviceType2.add("InformMembersProcess");
		serviceType2.add("InformRoleNormsProcess");
		serviceType2.add("InformRoleProfilesProcess");
		serviceType2.add("InformUnitProcess");
		serviceType2.add("InformUnitRolesProcess");
		serviceType2.add("SearchServiceProcess");

		
		
		serviceType3.add("QuantityMembersProcess");
		
		serviceType4.add("GetProcessProcess");
		

		
		
	}
	//****************************************Common methods***************************************************
	
	
	Object sendInform() {

		this.reset();
		
		ACLMessage requestMsg = new ACLMessage(ACLMessage.REQUEST);
		requestMsg.setSender(agent.getAid());
		requestMsg.setContent(call);
		requestMsg.setProtocol(InteractionProtocol.FIPA_REQUEST);
		if (isgenericSerice)//Se enviara al proveedor del servicio
		{
			requestMsg.setReceiver(new AgentID(clientProvider));
		}
		else
		{
			requestMsg.setReceiver(new AgentID(thomasAgent));	
		}
		
		
		logger.info("[QueryAgent]Sms to send: " + requestMsg.getContent());
		logger.info("[QueryAgent]Sending... ");
		
		initProxyProtocol(requestMsg);
		
		return returnResult();
	}
	
	void reset()
	{
		this.setValue("");	
		this.Status = true;
		this.listResults.clear();
		this.agents.clear();
		ErrorValue = "";
		status = "";	
	}
	//Esta función se encarga de devolver el resultado, mostrando un error si no ha salido correctamente la operación o 
	//creando un nuevo objeto con el resultado de la operación.
	Object returnResult()
	{

		//Servicios que devuelven un string.
		if (serviceType1.contains(serviceName))
		{
			if (!Status) {
				logger.error("["+agent.getName()+"] "+ serviceName+": " + this.value);
				return "";
			} else
			{
				return this.value;
			}
		}//Servicios que devuelven un ArrayList<String>()
		else if (serviceType2.contains(serviceName))
		{

			if (!Status) {
				logger.error("["+agent.getName()+"] "+ serviceName+": " + this.value);
				return new ArrayList<String>();
			} else
			{
				return new ArrayList<String>(this.listResults);
			}
		}//Servicios que devuelven un entero.
		else  if (serviceType3.contains(serviceName))
		{
			if (!Status) {
				logger.error("["+agent.getName()+"] "+ serviceName+": " + this.value);
				return 0;
			} else
				return this.Quantity;
		}//Servicios que devuelven una Hashtable
		else  if (serviceType4.contains(serviceName))
		{
			if (!Status)
			{
				logger.error("["+agent.getName()+"] "+ serviceName + this.value);
				return new Hashtable<AgentID, String>();
			}

			else
				return this.agents;

		}
		else //Este es el servicio de tipo genericService 
		{

			//para este tipo si que tendremos que tener en cuenta que es genericService, ya que no existe como tal ese servicio.
			if (!Status) {
				logger.error("["+agent.getName()+"] "+ "Error in generic funcion: "+ this.value);
				return new Hashtable<String, String>();
			} else
				return genericServiceList;

		}

	}
	
	
	//Aqui es donde debemos diferenciar entre si es un agente QueueAgent o un agente CAgent. Cada tipo ejecutará
	//de manera diferente el protocolo.
	void initProxyProtocol(ACLMessage requestMsg)
	{
		if (agent instanceof QueueAgent)
		{
			THOMASQAgentRequest test = new THOMASQAgentRequest((QueueAgent)agent, requestMsg, this);

			do {
				test.action();
			} while (!test.finished());
			
		}
		else if (agent instanceof CAgent)
		{
			
		//Inicialización del protocolo / conversación request.
			CAgent myAgent = (CAgent)agent;
			THOMASCAgentRequest protocol = new THOMASCAgentRequest(this);
			CProcessorFactory talk = protocol.newFactory("THOMASRequest", null, requestMsg, 1, myAgent, 0);
			myAgent.addFactoryAsInitiator(talk);
			myAgent.startSyncConversation(talk.getName());
			myAgent.removeFactory(talk.getName());
		}
		
		
	}
	
	void setValue(String msg)
	{
		this.value = msg;
		
	}
	
	
	private void addIDSearchService(String id) {

		this.listResults.add(id);

	}

	
	//Funcion de parsing para convertir el string del resultado en variables de retorno.
	void extractInfo(ACLMessage msg)
	{

		//**************************************************SF parsing*******************************************************

		// Sacamos el patron

		if (thomasAgent.equals("SF"))
		{

			String arg1 ="";
			// primer argumento si es un DeregisterProfileProcess no sacaremos
			// el arg1
			if (!serviceName.equals("DeregisterProfileProcess")
					&& !serviceName.equals("ModifyProfileProcess")
					&& !serviceName.equals("ModifyProcessProcess")
					&& !serviceName.equals("RemoveProviderProcess")) {
				arg1 = msg.getContent().substring(
						msg.getContent().indexOf("=") + 1,
						msg.getContent().length());
				arg1 = arg1.substring(arg1.indexOf("=") + 1, arg1.indexOf(","));
			}

			// segundo argumento
			String arg2 = msg.getContent();
			arg2 = arg2.substring((arg2.lastIndexOf("=")) + 1, arg2.length() - 1);

			// si ejecutamos el registerProcess

			if (serviceName.equals("RegisterProcessProcess")) {
				if (arg2.equals("1")) {
					this.processDescripcion.setImplementationID(arg1);
					// this.sf.agent.setSFAgentDescription(this.sf.descripcion);
					this.setValue(arg1);

				} else {
					this.Status = false;
					this.setValue(arg1);
				}

			}

			// si ejecutamos el GetProfile
			if (serviceName.equals("GetProfileProcess")) {
				arg2 = msg.getContent().substring(
						msg.getContent().indexOf(",") + 1,
						msg.getContent().length());
				arg2 = arg2.substring(arg2.indexOf("=") + 1, arg2.indexOf(","));

				if (arg2.equals("1"))// ha ido bien
				{
					this.setValue(arg1);
				} else {
					this.Status = false;
					this.setValue(arg1);
				}

			}

			// si ejecutamos el DeregisterProfile
			if (serviceName.equals("DeregisterProfileProcess")) {

				if (arg2.equals("1"))// ha ido bien
				{
					// elimino del arrayList
					// this.sf.agent.getArraySFAgentDescription().remove(
					// this.sf.descripcion);

					this.setValue(arg2);
				} else // ha ido mal
				{
					this.Status = false;
					this.setValue("The error is caused for there are process associated with the profile or the id profile not exist.");
				}

			}

			// si ejecutamos el GetProcess
			if (serviceName.equals("GetProcessProcess")) {

				agentGetProcess = null;
				if (arg2.equals("0")) {
					this.Status = false;
					this.setValue(arg1);

				} else {
					agentGetProcess = arg1.split(",");
					for (String a : agentGetProcess) {
						// sacamos el url process
						String arg_aux = a.substring(arg1.indexOf(" ") + 1, arg1
								.length());

						arg1 = a.substring(0, arg1.indexOf(" "));
						arg1 = arg1.substring(arg1.indexOf("-") + 1, arg1.length());

						// tenemos que controlar si existe 0, 1 o mas
						// proveedores.

						if (!arg1.equals("null"))// si existe algun provideer
						{

							// a�adimos tantos agentes proveedores como nos
							// devuelva
							// this.agentes.add(new AgentID(arg1));
							this.agents.put(new AgentID(arg1), arg_aux);
						}

					}


				}

			}

			// si ejecutamos el searchService
			if (serviceName.equals("SearchServiceProcess")) {

				agentGetProcess = null;

				if (arg2.equals("1")) {

					// this.sf.addIDSearchService(arg2);
					// } else {
					this.agentGetProcess = arg1.split(",");

					for (String a : agentGetProcess) {
						a = a.substring(0,a.indexOf(" "));
						this.addIDSearchService(a);
					}

				} else
				{
					this.Status = false;

					this.setValue(arg1);
				}

			}

			// solo si ejecutamos el registerProfile
			if (serviceName.equals("RegisterProfileProcess")) {
				if (arg1.equals("1")) {
					// para guardar nuestros ID para poder modificar
					// posteriormente nuestro servicio
					this.profileDescription.setServiceID(arg2);
					this.setValue(arg2);


				} else {
					this.Status = false;
					this.setValue(arg2);
				}

			}

			// Si ejecutamos el ModifyProfile

			if (serviceName.equals("ModifyProfileProcess")) {
				if (arg2.equals("1"))// ha hido todo bien
				{
					this.setValue(arg2);

				} else if (arg2.equals("0"))// existen profile ligados a este
					// process, por tanto no puede
					// modificar-lo
				{
					this.Status = false;
					this.setValue(arg2);
				} else// el id del servicio no es valido
				{

					this.setValue(arg1);
				}

			}

			// Si ejecutamos el ModifyProcess
			if (serviceName.equals("ModifyProcessProcess")) {
				this.setValue(arg2);

			}

			if (serviceName.equals("RemoveProviderProcess")) {
				if (arg2.equals("1"))
					this.setValue(arg2);
				else
					this.setValue("Service process id does not exist");
			}

			// si ejecutamos un servicio generico
			if (this.isgenericSerice) {


				// sino no es un servicio del oms o del sf, segun los outputs
				// sacamos los resultados.
				String sub = msg.getContent().substring(
						msg.getContent().indexOf("=") + 1);
				String[] aux = sub.split(",");

				for (String output : oracle.getOutputs()) {
					for (int i = 0; i < aux.length; i++) {
						String a = aux[i];

						if (i != (aux.length - 1))// menos el ultimo
						{
							if (a.substring(a.indexOf("#") + 1, a.indexOf("="))
									.equals(output)) {
								this.genericServiceList.put(output, a
										.substring(a.indexOf("=") + 1));
							}
						} else {
							if (a.substring(a.indexOf("#") + 1, a.indexOf("="))
									.equals(output)) {
								this.genericServiceList.put(output, a.substring(
										a.indexOf("=") + 1, (a.length() - 1)));

							}

						}
					}
				}

				this.isgenericSerice = false;

			}
		}
		else
		{

	//**********************************************OMS parsing*********************************************
			if (serviceName.equals("InformUnitProcess")) {

				String arg;
				String argAux;


				arg = msg.getContent().substring(
						msg.getContent().indexOf("ParentID") + 9,
						msg.getContent().indexOf(","));
				this.addElementToList(arg);
				argAux = msg.getContent().substring(
						msg.getContent().indexOf("UnitGoal"),
						msg.getContent().length());
				arg = argAux.substring(argAux.indexOf("UnitGoal") + 9, argAux
						.indexOf(","));
				this.addElementToList(arg);
				argAux = argAux.substring(argAux.indexOf("UnitType"), argAux
						.length());
				arg = argAux.substring(argAux.indexOf("UnitType") + 9, argAux
						.indexOf("}"));
				this.addElementToList(arg);

			}

			// sacamos el Status


			int n = msg.getContent().indexOf(",")
			- msg.getContent().indexOf("Status");

			if (n > 0) {
				status = msg.getContent().substring(
						msg.getContent().indexOf("Status") + 7,
						msg.getContent().indexOf(","));
			} else {
				status = msg.getContent().substring(
						msg.getContent().indexOf("Status") + 7,
						msg.getContent().indexOf("}"));

			}

			if (serviceName.equals("InformAgentRoleProcess")
					|| serviceName.equals("InformMembersProcess")
					|| serviceName.equals("InformRoleNormsProcess")
					|| serviceName.equals("InformRoleProfilesProcess")
					|| serviceName.equals("InformUnitRolesProcess")) {

				// recorrer el vector
				String argAux;

				// para los servicios informativos

				// diferenciamos entre los que tienen tuplas y los que no
				// son tuplas

				if (serviceName.equals("InformAgentRoleProcess")
						|| serviceName.equals("InformMembersProcess")) {

					if (!status.equals("Ok")) {
						this.Status = false;
						this.addElementToList("EMPTY");
					} else {
						String arg3 = msg.getContent().substring(
								msg.getContent().indexOf(",") + 1,
								msg.getContent().length());

						if (!arg3.contains("[]"))
						{
							arg3 = arg3.substring(arg3.indexOf("("), arg3.indexOf("]"));

							elements = arg3.split(",");

							int paridad = 0;

							for (String e : elements) {
								if ((paridad % 2) == 0)// es par
								{
									argAux = e
									.substring(e.indexOf("(") + 1, e.length());

								} else {
									argAux = e.substring(0, e.indexOf(")"));
								}
								this.addElementToList(argAux);
								paridad++;
							}
						}

					}
				} else {
					if (!status.equals("Ok")) {
						this.Status = false;
						this.addElementToList("EMPTY");
					} else {
						String arg3 = msg.getContent().substring(
								msg.getContent().indexOf("[") + 1,
								msg.getContent().indexOf("]"));

						elements = arg3.split(",");

						for (String e : elements) {
							this.addElementToList(e);

						}

					}

				}
			}

			ErrorValue = msg.getContent();

			n = msg.getContent().indexOf(",")
			- msg.getContent().indexOf("ErrorValue");

			if (n > 0) {
				ErrorValue = msg.getContent().substring(
						msg.getContent().indexOf("ErrorValue") + 11,
						msg.getContent().indexOf(","));
			} else {
				ErrorValue = msg.getContent().substring(
						msg.getContent().indexOf("ErrorValue") + 11,
						msg.getContent().indexOf("}"));

			}

			if (status.contains("Ok")) {//Cambiado por equals

				if (serviceName.equals("QuantityMembersProcess")) {

					String quantity = msg.getContent().substring(
							msg.getContent().indexOf("Quantity=") + 9,
							msg.getContent().indexOf("}"));
					this.setQuantity(Integer.parseInt(quantity));
				}


				this.setValue(status);
			} else {
				this.Status = false;
				// vemos que tipo de error
				this.setValue(status + " " + ErrorValue);
			}

		}
	}
	
	
	//***************************************Protocol implementation*******************************************
	
	/**
	 * THOMASQAgentRequest handles the messages received from the OMS or SF
	 */
	static class THOMASQAgentRequest extends FIPARequestInitiator {

		THOMASProxy thomasProxy;

		protected THOMASQAgentRequest(QueueAgent agent, ACLMessage msg, THOMASProxy thomasProxy) {
			super(agent, msg);
			this.thomasProxy = thomasProxy;
		}
		
		

		protected void handleAgree(ACLMessage msg) {
			logger.info(myAgent.getName() + ": OOH! "
					+ msg.getSender().getLocalName()
					+ " Has agreed to excute the service!");

		}

		protected void handleRefuse(ACLMessage msg) {
			logger.error(myAgent.getName() + ": Oh no! "
					+ msg.getSender().getLocalName()
					+ " has rejected my proposal.");
			this.thomasProxy.setValue("");

		}

		protected void handleInform(ACLMessage msg) {
			logger.info(myAgent.getName() + ":"
					+ msg.getSender().getLocalName()
					+ " has informed me of the status of my request."
					+ " They said : " + msg.getContent());
			this.thomasProxy.extractInfo(msg);

		}

		protected void handleNotUnderstood(ACLMessage msg) {
			logger.error(myAgent.getName() + ":"
					+ msg.getSender().getLocalName()
					+ " has indicated that they didn't understand.");
			this.thomasProxy.setValue("");

		}

		protected void handleOutOfSequence(ACLMessage msg) {
			logger.error(myAgent.getName() + ":"
					+ msg.getSender().getLocalName()
					+ " has send me a message which i wasn't"
					+ " expecting in this conversation");
			this.thomasProxy.setValue("");

		}

		protected void handleFailure(ACLMessage msg) {
			logger.error(myAgent.getName() + ":"
					+ msg.getSender().getLocalName()
					+ " has send me a message which i wasn't"
					+ " expecting in this conversation");
			this.thomasProxy.setValue("");

		}
	}
	
	class THOMASCAgentRequest extends FIPA_REQUEST_Initiator {
		
		THOMASProxy thomasProxy;
		
		public THOMASCAgentRequest(THOMASProxy thomasProxy){
			this.thomasProxy = thomasProxy;
			thomasProxy.setValue("");
		}
		
		protected void doInform(CProcessor myProcessor, ACLMessage msg) {
			logger.info(myProcessor.getMyAgent().getName() + ":"
					+ msg.getSender().getLocalName()
					+ " has informed me of the status of my request."
					+ " They said : " + msg.getContent());
			this.thomasProxy.extractInfo(msg);
		}
		
		protected void doAgree(CProcessor myProcessor, ACLMessage msg){
			logger.info(myProcessor.getMyAgent().getName() + ": OOH! "
					+ msg.getSender().getLocalName()
					+ " Has agreed to excute the service!");
		}
		
		protected void doRefuse(CProcessor myProcessor, ACLMessage msg){
			logger.error(myProcessor.getMyAgent().getName() + ": Oh no! "
					+ msg.getSender().getLocalName()
					+ " has rejected my proposal.");
			this.thomasProxy.setValue("");
		}
		
		protected void doNotUnderstood(CProcessor myProcessor, ACLMessage msg){
			logger.error(myProcessor.getMyAgent().getName() + ":"
					+ msg.getSender().getLocalName()
					+ " has indicated that they didn't understand.");
			this.thomasProxy.setValue("");
		}
		
		protected void doFailure(CProcessor myProcessor, ACLMessage msg){
			logger.error(myProcessor.getMyAgent().getName() + ":"
					+ msg.getSender().getLocalName()
					+ " has send me a message which i wasn't"
					+ " expecting in this conversation");
			this.thomasProxy.setValue("");
		}
	}
	

}