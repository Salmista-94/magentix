package TestCore;

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;

/**
 * Tests for AgentID class
 * 
 * @author David Fernández - dfernandez@dsic.upv.es
 */

public class TestAgentID {

	AgentID agent;
	Process qpid_broker;

	@Before
	public void setUp() throws Exception {

		/**
		 * Setting the configuration
		 */
		DOMConfigurator.configure("configuration/loggin.xml");

		/**
		 * Instantiating the AgentID
		 */
		agent = new AgentID();

	}

	/**
	 * Testing AgentID empty constructor
	 * 
	 */
	@Test(timeout = 5000)
	public void testEmptyConstructor() {
		// agent is initialize in SetUp() with empty constructor by default

		assertEquals(agent.name, "");
		assertEquals(agent.protocol, "");
		assertEquals(agent.host, "");
		assertEquals(agent.port, "");
	}

	/**
	 * Testing AgentID full constructor
	 * 
	 * Constructor with all the atributes of the class
	 */
	@Test(timeout = 5000)
	public void testFullConstructor() {
		// agent is initialize in SetUp() with empty constructor by default

		String name = "David";
		String protocol = "FIPA";
		String host = "16400";
		String port = "2840";

		agent = new AgentID(name, protocol, host, port);
		assertEquals(agent.name, name);
		assertEquals(agent.protocol, protocol);
		assertEquals(agent.host, host);
		assertEquals(agent.port, port);
	}

	/**
	 * Testing AgentID id constructor
	 * 
	 * Constructor with the ID of the agent in a common name format
	 */
	@Test(timeout = 5000)
	public void testIDNameConstructor() {
		// agent is initialize in SetUp() with empty constructor by default

		String name = "David";
		String protocol = "qpid"; // Default in the constructor
		String host = "localhost"; // Default in the constructor
		String port = "8080"; // Default in the constructor

		agent = new AgentID(name);
		assertEquals(agent.name, name);
		assertEquals(agent.protocol, protocol);
		assertEquals(agent.host, host);
		assertEquals(agent.port, port);
	}

	/**
	 * Testing AgentID id constructor
	 * 
	 * Constructor with the ID of the agent in an address format
	 */
	@Test(timeout = 5000)
	public void testIDAddressConstructor() {
		// agent is initialize in SetUp() with empty constructor by default

		String id = "FIPA://David@16400:2840";
		String name = "David";
		String protocol = "FIPA";
		String host = "16400";
		String port = "2840";

		agent = new AgentID(id);
		assertEquals(agent.name, name);
		assertEquals(agent.protocol, protocol);
		assertEquals(agent.host, host);
		assertEquals(agent.port, port);
	}

	/**
	 * Testing AgentID toString()
	 * 
	 * Tested with empty and full cosntructor
	 */
	@Test(timeout = 5000)
	public void testToString() {
		// agent is initialize in SetUp() with empty constructor by default
		assertEquals(agent.toString(), "://@:");

		String name = "David";
		String protocol = "FIPA";
		String host = "16400";
		String port = "2840";

		agent = new AgentID(name, protocol, host, port);

		assertEquals(agent.toString(), protocol + "://" + name + "@" + host
				+ ":" + port);
	}

	/**
	 * Testing AgentID name_all()
	 * 
	 * Similar to toString() but returns a string with a similar format to Jade
	 * 
	 * Tested with empty and full cosntructor
	 */
	@Test(timeout = 5000)
	public void testNameAll() {
		// agent is initialize in SetUp() with empty constructor by default
		assertEquals(agent.name_all(), "@:");

		String name = "David";
		String protocol = "FIPA";
		String host = "16400";
		String port = "2840";

		agent = new AgentID(name, protocol, host, port);

		assertEquals(agent.name_all(), name + "@" + host + ":" + port);
	}

	/**
	 * Testing AgentID addresses_all()
	 * 
	 * Similar to toString() but returns a string with a similar format to and
	 * URL
	 * 
	 * Tested with empty and full cosntructor
	 */
	@Test(timeout = 5000)
	public void testAddressesAll() {
		// agent is initialize in SetUp() with empty constructor by default
		assertEquals(agent.addresses_all(), "://:");

		String name = "David";
		String protocol = "FIPA";
		String host = "16400";
		String port = "2840";

		agent = new AgentID(name, protocol, host, port);

		assertEquals(agent.addresses_all(), protocol + "://" + host + ":"
				+ port);
	}

	/**
	 * Testing AgentID addresses_single()
	 * 
	 */
	@Test(timeout = 5000)
	public void testAddressesSingle() {
		// agent is initialize in SetUp() with empty constructor by default
		assertEquals(agent.addresses_single(), ":");

		String name = "David";
		String protocol = "FIPA";
		String host = "16400";
		String port = "2840";

		agent = new AgentID(name, protocol, host, port);

		assertEquals(agent.addresses_single(), host + ":" + port);
	}

	/**
	 * Testing AgentID getLocalName()
	 * 
	 * Tested with empty and full constructor
	 */
	@Test(timeout = 5000)
	public void testGetLocalName() {
		// agent is initialize in SetUp() with empty constructor by default
		assertEquals(agent.getLocalName(), "");

		String name = "David";
		String protocol = "FIPA";
		String host = "16400";
		String port = "2840";

		agent = new AgentID(name, protocol, host, port);

		assertEquals(agent.getLocalName(), name);
	}

	/**
	 * Testing AgentID getLocalName()
	 * 
	 * Tested when the name given has an "@"
	 */
	@Test(timeout = 8000)
	public void testGetLocalNameAddress() {
		// agent is initialize in SetUp() with empty constructor by default

		String id = "FIPA://David@16400:2480";
		String name = "David@Fernandez";
		String protocol = "FIPA";
		String host = "16400";
		String port = "2840";

		int namePos = name.lastIndexOf('@');
		String expectedName = name.substring(0, namePos);

		agent = new AgentID(name, protocol, host, port);

		assertEquals(agent.getLocalName(), expectedName);
	}

	/**
	 * Testing AgentID equals()
	 * 
	 * Tested when object parameters are not equals
	 */
	@Test(timeout = 5000)
	public void testEqualsClassParameters() {
		// agent is initialize in SetUp() with empty constructor by default

		String name = "David";
		String protocol = "FIPA";
		String host = "16400";
		String port = "2840";

		agent = new AgentID(name, protocol, host, port);
		AgentID otherAgent;

		// Test for name comparison
		otherAgent = new AgentID("Salem", protocol, host, port);
		assertEquals(agent.equals(otherAgent), false);

		// Test for protocol comparison
		otherAgent = new AgentID(name, "Request", host, port);
		assertEquals(agent.equals(otherAgent), false);

		// Test for host comparison
		otherAgent = new AgentID(name, protocol, "46019", port);
		assertEquals(agent.equals(otherAgent), false);

		// Test for port comparison
		otherAgent = new AgentID(name, protocol, host, "2338");
		assertEquals(agent.equals(otherAgent), false);

		// Test for all parameters equals
		otherAgent = new AgentID(name, protocol, host, port);
		assertEquals(agent.equals(otherAgent), true);
	}

	/**
	 * Testing AgentID equals()
	 * 
	 * Tested when object is not an instance of the same class and when two
	 * objects are the same instance
	 */
	@Test(timeout = 5000)
	public void testEqualsExceptions() {
		// agent is initialize in SetUp() with empty constructor by default

		String name = "David";
		String protocol = "FIPA";
		String host = "16400";
		String port = "2840";

		agent = new AgentID(name, protocol, host, port);
		AgentID otherAgent = agent;
		ACLMessage kindOfAgent = new ACLMessage();

		// Test for object typeOf comparison
		assertEquals(agent.equals(kindOfAgent), false);

		// Test for objects same instance comparison
		assertEquals(agent.equals(otherAgent), true);
	}

	@After
	public void tearDown() throws Exception {
		agent = null;

	}
}