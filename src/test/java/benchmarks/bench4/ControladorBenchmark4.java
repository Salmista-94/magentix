package benchmarks.bench4;


import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.qpid.transport.Connection;
import org.apache.qpid.transport.MessageTransfer;

import es.upv.dsic.gti_ia.fipa.ACLMessage;
import es.upv.dsic.gti_ia.fipa.AgentID;
import es.upv.dsic.gti_ia.magentix2.SingleAgent;

public class ControladorBenchmark4 extends SingleAgent {
	
    ACLMessage msg,msg2;
	LinkedBlockingQueue<MessageTransfer> internalQueue;
	int ntotal, nagents=0, nacabats = 0;
	Vector<String> agents = new Vector<String>();
	long t1,t2;
	
	public ControladorBenchmark4(AgentID aid, Connection connection, int ntotal) {
		super(aid, connection);
		this.ntotal = ntotal;
	}
	
	public void execute(){
		//Esperem a rebre el Ready de tots els agents emisors
		while(nagents < ntotal){
			msg = this.receiveACLMessage();
			
			try{
				
				agents.addElement(msg.getSender().name);
		
			}catch(java.lang.NullPointerException e){System.out.println("No s'ha pogut afegir el Sender");}

			nagents++;
		}
		
		t1 = System.currentTimeMillis();
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.setContent("Start!");
		msg.setSender(this.getAid());
		AgentID receiver = new AgentID();
		receiver.protocol = "http";
		receiver.port = "8080";
		//enviem un missatge a cada emisor per a que comencen a emetre missatges
		Iterator<String> iterator=agents.iterator();

		while(iterator.hasNext())
			receiver.name = iterator.next();
		    msg2.add_receiver(receiver);


		this.send_multicast(msg);
				
		
		//esperem a que ens responguen tots amb ok
		while(nacabats < ntotal){
			receiveACLMessage();
			nacabats++;
		}
		
		//Mostrem resultat per pantalla
		System.out.println("Prova acabada!");
		t2 = System.currentTimeMillis();
		System.out.println("Bench Time (s): "+ (float) (t2 - t1)/1000);
	}
}