package ContractNet_Example;



import es.upv.dsic.gti_ia.architecture.FIPAContractNetResponder;
import es.upv.dsic.gti_ia.architecture.FIPANames;
import es.upv.dsic.gti_ia.architecture.FailureException;
import es.upv.dsic.gti_ia.architecture.MessageTemplate;
import es.upv.dsic.gti_ia.architecture.Monitor;
import es.upv.dsic.gti_ia.architecture.NotUnderstoodException;
import es.upv.dsic.gti_ia.architecture.QueueAgent;
import es.upv.dsic.gti_ia.architecture.RefuseException;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;

public class Concessionaire extends QueueAgent {

	public Concessionaire(AgentID aid) throws Exception {

		super(aid);

	}

	public void execute()
    {
    
		
	Monitor m = new Monitor();
    System.out.printf("%s: Waiting for customers...\n", this.getName());
    


    // Se crea una plantilla que filtre los mensajes a recibir.
    MessageTemplate template = new MessageTemplate(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);

    // A�adimos los comportamientos ante mensajes recibidos
    CrearOferta oferta = new CrearOferta(this, template);
    
    this.addTask(oferta);
    m.waiting();
    /*
    do
    {
    	oferta.action();
    	
    }while(true);*/
    
}

	// Hacemos una simulaci�n para que pueda dar que existe o no coche (sobre un
	// 80% probab).
	private boolean existeCoche() {
		return (Math.random() * 100 > 20);
	}

	// Calculamos un precio para el coche aleatoriamente (estar� entre 8000 y
	// 30000).
	private int obtenerPrecio() {
		return (int) (Math.random() * 22000) + 8000;
	}

	// Simula fallos en el c�lculo de precios.
	private boolean devolverPrecio() {
		return (int) (Math.random() * 10) > 1;
	}

	private class CrearOferta extends FIPAContractNetResponder {
		public CrearOferta(QueueAgent agente, MessageTemplate plantilla) {
			super(agente, plantilla);
		}

		protected ACLMessage prepareResponse(ACLMessage cfp)
				throws NotUnderstoodException, RefuseException {
			System.out
					.printf("%s: Request offer received from %s.\n",
							getName(), cfp.getSender()
									.getLocalName());

			// Comprobamos si existen ofertas disponibles
			if (Concessionaire.this.existeCoche()) {
				// Proporcionamos la informaci�n necesaria
				int precio = Concessionaire.this.obtenerPrecio();
				System.out.printf("%s: Preparing Offer (%d euros).\n",
						getName(), precio);

				// Se crea el mensaje
				ACLMessage oferta = cfp.createReply();
				oferta.setPerformative(ACLMessage.PROPOSE);
				oferta.setContent(String.valueOf(precio));
				return oferta;
			} else {
				// Si no hay ofertas disponibles rechazamos el propose
				System.out.printf(
						"%s: We have no offers available.\n",
						getName());
				throw new RefuseException("I fail in the evaluation.");
			}
		}

		protected ACLMessage prepareResultNotification(ACLMessage cfp,
				ACLMessage propose, ACLMessage accept) throws FailureException {
			// Hemos recibido una aceptaci�n de nuestra oferta, enviamos el
			// albar�n
			System.out.printf("%s: There is a possible offer.\n",
					getName());

			if (devolverPrecio()) {
				System.out.printf("%s: Sending purchase contract.\n",
						getName());
			

				ACLMessage inform = accept.createReply();

				inform.setPerformative(ACLMessage.INFORM);
				return inform;
			} else {
				System.out.printf(
						"%s: OHH!, has failed to send the contract.\n",
						getName());
				throw new FailureException("Error on having sent contract.");
			}
		}

		protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose,
				ACLMessage reject) {
			// Nuestra oferta por el coche ha sido rechazada
			System.out.printf(
					"%s: Offer rejected by his excessive price.\n",
					getName());
		}
	}

}