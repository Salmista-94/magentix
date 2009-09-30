/**
 * La clase FIPAContractNetInitiator permite ejecutar el protocolo FIPA-CONTRACT-NET por la parte del iniciador.
 * 
 * @author  Joan Bellver Faus, GTI-IA, DSIC, UPV
 * @version 2009.9.07
 */

package es.upv.dsic.gti_ia.proto;

import java.util.Vector;
import java.util.Date;
import java.util.logging.*;

import es.upv.dsic.gti_ia.fipa.AgentID;
import es.upv.dsic.gti_ia.proto.FIPANames.InteractionProtocol;
import es.upv.dsic.gti_ia.magentix2.QueueAgent;

import es.upv.dsic.gti_ia.fipa.ACLMessage;

import java.util.*;

public class FIPAContractNetInitiator {

	private Adviser sin = null;

	private final static int PREPARE_MSG_STATE = 0;
	private final static int SEND_MSG_STATE = 1;
	private final static int RECEIVE_REPLY_STATE = 2;
	private final static int SEND_2ND_REPLY_STATE = 3;
	private final static int RECEIVE_2ND_REPLY_STATE = 4;
	private final static int ALL_REPLIES_RECEIVED_STATE = 5;
	private final static int ALL_RESULT_NOTIFICATION_RECEIVED_STATE = 6;

	private MessageTemplate template = null;
	private int state = PREPARE_MSG_STATE;
	protected QueueAgent myAgent;
	private ACLMessage requestmsg;
	private ACLMessage requestsentmsg;

	private boolean finish = false;
	String conversationID = null;
	private long timeout = -1;
	private long endingtime = 0;

	ArrayList<ACLMessage> aceptados = new ArrayList<ACLMessage>();
	ArrayList<ACLMessage> respuestas = new ArrayList<ACLMessage>();

	private int nEnviados = 0;
	private int nLeidos = 0;

	public FIPAContractNetInitiator(QueueAgent agent, ACLMessage msg) {
		myAgent = agent;
		requestmsg = msg;
		if (agent.getAdviserIni() == null) {
			agent.setAdviserIni(new Adviser());
		}
		this.sin = agent.getAdviserIni();

	}

	public boolean finalizado() {
		return this.finish;
	}

	public void action() {

		switch (state) {
		case PREPARE_MSG_STATE: {

			ACLMessage msg = prepareRequest(this.requestmsg);
			this.requestsentmsg = msg;
			state = SEND_MSG_STATE;
			break;
		}
		case SEND_MSG_STATE: {

			ACLMessage request = this.requestsentmsg;
			if (request == null) {
				// finalización del protocolo
				this.finish = true;
				break;
			} else {
				// a�adir el agentId que lo envia, lo hacemos tranparente al
				// usuario
				
				//guardar temporalmente los agentes a enviar
				
				ArrayList<AgentID> agentes = (ArrayList)request.getReceiverList().clone();
				request.setSender(myAgent.getAid());
				// recorrer todos lo receivers
				template = new MessageTemplate(InteractionProtocol.FIPA_CONTRACT_NET);
				for (es.upv.dsic.gti_ia.fipa.AgentID agent : agentes) {
					
		
					// por cada agente que enviamos creamos una idconversacion
					conversationID = "C" + hashCode() + "_"
							+ System.currentTimeMillis();
					request.setConversationId(conversationID);
					template.add_receiver(agent);// TODO, esto esta
													// modificado
					template.addConversacion(conversationID);
					myAgent.setConversacionActiva(conversationID);

					request.setReceiver(agent);
					myAgent.send(request);
				

				}

				this.nEnviados = agentes.size();

				System.out.println("Numero de enviados "+ this.nEnviados);
				// fijamos el el timeout del mensaje
				Date d = request.getReplyByDate();
				if (d != null)
					timeout = d.getTime() - (new Date()).getTime();
				else
					timeout = -1;
				endingtime = System.currentTimeMillis() + timeout;

				state = RECEIVE_REPLY_STATE;

			}
			break;

		}
		case RECEIVE_REPLY_STATE: {

			// nos esperaremos a un timeout o a que termine de llegar todos.

			// el template buscara todos los que tengan un conversacionID
			// determinada, que el protocolo sea cfp y ademas que provenga
			// de un agent en concreto, //lo del agente lo podemos poner para
			// que no se envie un idconversacion y no sea alguno de los que
			// enviado
			ACLMessage firstReply = myAgent.receiveACLMessageI(template);



			if (firstReply != null) {

				switch (firstReply.getPerformativeInt()) {
				case ACLMessage.PROPOSE: {

					respuestas.add(firstReply);
					// aceptados.add(firstReply);
					handlePropose(firstReply, aceptados);
					// Si estan todos salir
					if (this.nEnviados <= this.respuestas.size()) {
						state = SEND_2ND_REPLY_STATE;
						handleAllResponses(respuestas, aceptados);
					}
					break;
				}
				case ACLMessage.REFUSE: {
					respuestas.add(firstReply);
					handleRefuse(firstReply);
					if (this.nEnviados <= this.respuestas.size()) {
						state = SEND_2ND_REPLY_STATE;
						handleAllResponses(respuestas, aceptados);
					}
					break;
				}
				case ACLMessage.NOT_UNDERSTOOD:
					respuestas.add(firstReply);
					{
						handleNotUnderstood(firstReply);
						if (this.nEnviados <= this.respuestas.size()) {
							state = SEND_2ND_REPLY_STATE;
							handleAllResponses(respuestas, aceptados);
						}
						break;

					}
				case ACLMessage.FAILURE: {
					respuestas.add(firstReply);
					handleFailure(firstReply);
					if (this.nEnviados <= this.respuestas.size()) {
						state = SEND_2ND_REPLY_STATE;
						handleAllResponses(respuestas, aceptados);
					}
					break;
				}
				default: {
					respuestas.add(firstReply);
					handleOutOfSequence(firstReply);
					if (this.nEnviados <= this.respuestas.size()) {
						state = SEND_2ND_REPLY_STATE;
						handleAllResponses(respuestas, aceptados);
					}
					break;

				}
				}
				break;
			} else {

				// si hemos a�adido un timeout
				if (timeout > 0) {
					long blocktime = endingtime - System.currentTimeMillis();

					if (blocktime <= 0)// dejamos de leer mensajes, ha
										// terminado el timeout
					{
	

						handleAllResponses(respuestas, aceptados);
						state = SEND_2ND_REPLY_STATE;
						break;
					} else {

						// compruebo que aun quedan mensajes por leer
						if (this.nEnviados > this.respuestas.size()) {

							this.sin.esperar();
							state = RECEIVE_REPLY_STATE;

							break;
						} else// sino, ya hemos leido todos los mensajes.
						{
	
							handleAllResponses(respuestas, aceptados);
							state = SEND_2ND_REPLY_STATE;
							break;
						}

					}

				} else {// TODO sino ha puesto tiempo esperaremos la llegada de
						// todos los mensajes.
					if (this.nEnviados < respuestas.size())// si aun quedan por
															// leer nos
															// esperaremos la
															// llegada de un
															// nuevo mensaje
					{
						this.sin.esperar();
						state = RECEIVE_REPLY_STATE;// state =
													// ALL_REPLIES_RECEIVED_STATE;
						break;
					} else// si hemos leido todos los mensajes pasamos de
							// estado
					{
						handleAllResponses(respuestas, aceptados);
						state = SEND_2ND_REPLY_STATE;// state =
														// ALL_REPLIES_RECEIVED_STATE;
						break;
					}
				}
			}

		}
		case SEND_2ND_REPLY_STATE: {

			// resetemaos el template quitando los receivers y los
			// conversationID
			template.deleteAllConversation();
			template.deleteAllReceiver();
			// borramos las conversaciones activas

			myAgent.deleteTodasConversacionActivas();

			// recorremos el vector de aceptados y lo enviamos
			for (ACLMessage mensaje : aceptados) {

				if (mensaje.getConversationId().equals("")) {
					conversationID = "C" + hashCode() + "_"
							+ System.currentTimeMillis();
					mensaje.setConversationId(conversationID);
				} else {
					conversationID = mensaje.getConversationId();
				}

				template.add_receiver(mensaje.getReceiver());
				template.addConversacion(conversationID);
				myAgent.setConversacionActiva(conversationID);

				mensaje.setSender(myAgent.getAid());

				myAgent.send(mensaje);
				// enviamos

				// modifcamos la performativa para poder recibir el que hemos
				// enviado

			}

			// si la respuesta que enviamos es reject tenemos que terminar

			state = RECEIVE_2ND_REPLY_STATE;
		}
		case RECEIVE_2ND_REPLY_STATE: {

			// Esperamos la llegada de segundo mensaje
			// si existe algun aceptado
			if (this.aceptados.size() == 0)
				state = ALL_RESULT_NOTIFICATION_RECEIVED_STATE;
			else {
					ACLMessage secondReply = myAgent
							.receiveACLMessageI(template);

					if (secondReply != null) {
						this.nLeidos++;
						switch (secondReply.getPerformativeInt()) {
						case ACLMessage.INFORM: {
							handleInform(secondReply);
							break;

						}
						case ACLMessage.FAILURE: {
							handleFailure(secondReply);
							break;

						}
						default: {
							handleOutOfSequence(secondReply);
							break;
						}
						}
						break;
					} else {
						// tendremos que esperar a que nos lleguen todas las
						// propuestas
						if (this.nLeidos < this.aceptados.size())// si aun no
																	// est�n
																	// todos
						{
							this.sin.esperar();
							state = RECEIVE_2ND_REPLY_STATE;
							break;
						} else {
							state = ALL_RESULT_NOTIFICATION_RECEIVED_STATE;
						}
					}
				
			}
		}
		case ALL_REPLIES_RECEIVED_STATE: {
			state = ALL_RESULT_NOTIFICATION_RECEIVED_STATE;
			break;
		}
		case ALL_RESULT_NOTIFICATION_RECEIVED_STATE: {

			this.finish = true;
			this.requestmsg = null;
			this.myAgent.deleteTodasConversacionActivas();
			break;
		}
		}

	}

	/**
	 * This method must return the ACLMessage to be sent. This default
	 * implementation just return the ACLMessage object passed in the
	 * constructor. Programmer might override the method in order to return a
	 * different ACLMessage. Note that for this simple version of protocol, the
	 * message will be just send to the first receiver set.
	 * 
	 * @param msg
	 *            the ACLMessage object passed in the constructor.
	 * @return a ACLMessage.
	 */
	protected ACLMessage prepareRequest(ACLMessage msg) {
		return msg;
	}

	protected void handlePropose(ACLMessage msg, ArrayList accepted) {
	}

	protected void handleRefuse(ACLMessage msg) {

	}

	protected void handleNotUnderstood(ACLMessage msg) {

	}

	protected void handleInform(ACLMessage msg) {

	}

	protected void handleFailure(ACLMessage msg) {

	}

	protected void handleOutOfSequence(ACLMessage msg) {

	}

	protected void handleAllResponses(ArrayList responses, ArrayList accepted) {

	}

}