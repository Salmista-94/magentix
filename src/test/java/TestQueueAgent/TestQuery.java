package TestQueueAgent;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;

/**
 * Test class for an example of QueueAgent, using the FIPA Query Protocol
 * 
 * @author David Fernández - dfernandez@dsic.upv.es
 */

public class TestQuery {

	Airport airport;
	Passenger passenger;
	Logger logger;
	Process qpid_broker;
	
	//Method before updating to junit4
	//
	//public TestQuery(String name) {
	//	super(name);
	//}
	@Before
	public void setUp() throws Exception {
		qpid_broker = qpidManager.UnixQpidManager.startQpid(Runtime.getRuntime(), qpid_broker);
		
		try {

			/**
			 * Setting the configuration
			 */
			DOMConfigurator.configure("configuration/loggin.xml");

			/**
			 * Connecting to Qpid Broker, default localhost.
			 */
			AgentsConnection.connect();

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	/*
	 * 
	 * /** Testing Airport agree answer
	 */
	@Test(timeout = 5 * 1000)
	public void testAgreeAnswer() {
		System.out.println("Agree test");

		try {
			/**
			 * Instantiating an Airport agent
			 */
			airport = new Airport(new AgentID("ManisesAirPort"));

			/**
			 * Instantiating a Passenger agent
			 */
			passenger = new Passenger(new AgentID("Veronica"));

		} catch (Exception e1) {
			fail();
		}

		airport.ASSIST_PROB = 0.4;// They will assist the passenger
		airport.start();
		passenger.start();

		// If passenger has not received answer wait
		while (passenger.queryResult.equalsIgnoreCase("")
				|| !airport.finished() || !passenger.finished()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		assertEquals("Wait a moment please, we are looking for in the Database"
				+ airport.getName(), passenger.queryResult);
	}

	/**
	 * Testing Airport refuse answer
	 */
	@Test(timeout = 5 * 1000)
	public void testRefuseAnswer() {
		System.out.println("Comenzando Refuse test");
		try {
			/**
			 * Instantiating an Airport agent
			 */
			airport = new Airport(new AgentID("ManisesAirPort"));

			/**
			 * Instantiating a Passenger agent
			 */
			passenger = new Passenger(new AgentID("Veronica"));

		} catch (Exception e1) {
			fail();
		}
		airport.ASSIST_PROB = 0.5;// They will not assist the passenger
		airport.start();
		passenger.start();

		// If passenger has not received answer wait
		while (passenger.queryResult.equalsIgnoreCase("")
				|| !airport.finished() || !passenger.finished()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		assertEquals(passenger.getName()
				+ ": At the moment all operators are busy. We can not assist"
				+ airport.getName(), passenger.queryResult);
	}

	/**
	 * Testing Airport successfull reservation
	 */
	@Test(timeout = 5 * 1000)
	public void testSuccesfullReservation() {
		try {
			/**
			 * Instantiating an Airport agent
			 */
			airport = new Airport(new AgentID("ManisesAirPort"));

			/**
			 * Instantiating a Passenger agent
			 */
			passenger = new Passenger(new AgentID("Veronica"));

		} catch (Exception e1) {
			fail();
		}
		// Reservation will be successfull as "Veronica" has more than 5
		// caracters
		airport.ASSIST_PROB = 0.4;// They will not assist the passenger
		airport.start();
		passenger.start();

		// If passenger has not received answer wait
		while (passenger.queryResult.equalsIgnoreCase("")
				|| !airport.finished() || !passenger.finished()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

		assertEquals("The operator reports:You have made a reservation",
				passenger.informResult);
	}

	/**
	 * Testing Airport unsuccessfull reservation
	 * 
	 */
	@Test(timeout = 5 * 1000)
	public void testUnsuccesfullReservation() {
		try {
			/**
			 * Instantiating an Airport agent
			 */
			airport = new Airport(new AgentID("ManisesAirPort"));

			/**
			 * Instantiating a Passenger agent
			 */
			passenger = new Passenger(new AgentID("Ana"));

		} catch (Exception e1) {
			fail();
		}
		airport.ASSIST_PROB = 0.4;// They will assist the passenger
		// Reservation will be unsuccessfull as "Ana" has less than 5 caracters
		airport.start();
		passenger.start();

		// If passenger has not received answer wait
		while (passenger.informResult.equalsIgnoreCase("")
				|| !airport.finished() || !passenger.finished()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		assertEquals("The operator reports:You have no reserves",
				passenger.informResult);
	}
	@After
	public void tearDown() throws Exception {
        AgentsConnection.disconnect();

        qpidManager.UnixQpidManager.stopQpid(qpid_broker);

    }
}
