package TestOMS;

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;
import es.upv.dsic.gti_ia.organization.OMS;
import es.upv.dsic.gti_ia.organization.OMSProxy;
import es.upv.dsic.gti_ia.organization.SF;
import es.upv.dsic.gti_ia.organization.exception.ForbiddenNormException;

/** 
 * @author Jose Alemany Bordera  -  jalemany1@dsic.upv.es
 * 
 */

public class TestRegisterNormProhibitionDomain {

	OMSProxy omsProxy = null;
	DatabaseAccess dbA = null;


	Agent agent = null;

	
	OMS oms = null;
	SF sf = null;

	Process qpid_broker;
	
	@After
	public void tearDown() throws Exception {

		//------------------Clean Data Base -----------//
		dbA.executeSQL("DELETE FROM agentPlayList");
		dbA.executeSQL("DELETE FROM agentList");
		
		dbA.executeSQL("DELETE FROM actionNormParam");
		
		dbA.executeSQL("DELETE FROM normList");
		dbA.executeSQL("DELETE FROM roleList WHERE idroleList != 1");
		dbA.executeSQL("DELETE FROM unitHierarchy WHERE idChildUnit != 1");
		dbA.executeSQL("DELETE FROM unitList WHERE idunitList != 1");
		//--------------------------------------------//

		dbA = null;
		omsProxy = null;

		agent.terminate();
		agent = null;

		oms.Shutdown();
		sf.Shutdown();
		
		oms.await();
		sf.await();
		
		oms = null;
		sf = null;

		AgentsConnection.disconnect();
		qpidManager.UnixQpidManager.stopQpid(qpid_broker);
	}
	
	@Before
	public void setUp() throws Exception {

		qpid_broker = qpidManager.UnixQpidManager.startQpid(Runtime.getRuntime(), qpid_broker);
		AgentsConnection.connect();

		oms = new OMS(new AgentID("OMS"));
		sf =  new SF(new AgentID("SF"));

		oms.start();
		sf.start();


		agent = new Agent(new AgentID("pruebas"));

		omsProxy = new OMSProxy(agent);

		dbA = new DatabaseAccess();

		//------------------Clean Data Base -----------//
		dbA.executeSQL("DELETE FROM agentPlayList");
		dbA.executeSQL("DELETE FROM agentList");
		
		dbA.executeSQL("DELETE FROM actionNormParam");
		
		dbA.executeSQL("DELETE FROM normList");
		dbA.executeSQL("DELETE FROM roleList WHERE idroleList != 1");
		dbA.executeSQL("DELETE FROM unitHierarchy WHERE idChildUnit != 1");
		dbA.executeSQL("DELETE FROM unitList WHERE idunitList != 1");
		//--------------------------------------------//
		
		dbA.executeSQL("INSERT INTO `agentList` (`agentName`) VALUES ('pruebas')");
	}
	
	@Test(timeout = 5 * 60 * 1000, expected=ForbiddenNormException.class)
	public void testRegisterNormProhibitionDomain1() throws Exception {
		
		//------------------------------------------- Test Initialization  -----------------------------------------------//
		dbA.executeSQL("INSERT INTO `unitList` (`unitName`,`idunitType`) VALUES ('plana',(SELECT idunitType FROM unitType WHERE unitTypeName = 'flat'))");
		dbA.executeSQL("INSERT INTO `unitHierarchy` (`idParentUnit`,`idChildUnit`) VALUES ((SELECT idunitList FROM unitList WHERE unitName = 'virtual'),(SELECT idunitList FROM unitList WHERE unitName = 'plana'))");
		dbA.executeSQL("INSERT INTO `roleList` (`roleName`,`idunitList`,`idposition`,`idaccessibility`,`idvisibility`) VALUES"+ 
				"('creador',(SELECT idunitList FROM unitList WHERE unitName = 'plana'),"+
				"(SELECT idposition FROM position WHERE positionName = 'creator'), "+
				"(SELECT idaccessibility FROM accessibility WHERE accessibility = 'internal'),"+ 
				"(SELECT idvisibility FROM visibility WHERE visibility = 'private'))");
			
		dbA.executeSQL("INSERT INTO `unitList` (`unitName`,`idunitType`) VALUES ('jerarquia',(SELECT idunitType FROM unitType WHERE unitTypeName = 'hierarchy'))");
		dbA.executeSQL("INSERT INTO `unitHierarchy` (`idParentUnit`,`idChildUnit`) VALUES ((SELECT idunitList FROM unitList WHERE unitName = 'virtual'),(SELECT idunitList FROM unitList WHERE unitName = 'jerarquia'))");
		dbA.executeSQL("INSERT INTO `roleList` (`roleName`,`idunitList`,`idposition`,`idaccessibility`,`idvisibility`) VALUES"+ 
				"('jefe',(SELECT idunitList FROM unitList WHERE unitName = 'jerarquia'),"+
				"(SELECT idposition FROM position WHERE positionName = 'supervisor'), "+
				"(SELECT idaccessibility FROM accessibility WHERE accessibility = 'internal'),"+ 
				"(SELECT idvisibility FROM visibility WHERE visibility = 'private'))");
		dbA.executeSQL("INSERT INTO `roleList` (`roleName`,`idunitList`,`idposition`,`idaccessibility`,`idvisibility`) VALUES"+ 
				"('creador',(SELECT idunitList FROM unitList WHERE unitName = 'jerarquia'),"+
				"(SELECT idposition FROM position WHERE positionName = 'creator'), "+
				"(SELECT idaccessibility FROM accessibility WHERE accessibility = 'internal'),"+ 
				"(SELECT idvisibility FROM visibility WHERE visibility = 'private'))");
		
		dbA.executeSQL("INSERT INTO `agentPlayList` (`idagentList`, `idroleList`) VALUES ((SELECT idagentList FROM agentList WHERE "
				+ "agentName = 'pruebas'),(SELECT idroleList FROM roleList WHERE (roleName = 'creador' AND idunitList = (SELECT "
				+ "idunitList FROM unitList WHERE unitName = 'jerarquia'))))");
		
		dbA.executeSQL("INSERT INTO normList (idunitList, normName, iddeontic, idtargetType, targetValue, idactionNorm, normContent, normRule) " +
				"VALUES ((SELECT idunitList FROM unitList WHERE unitName = 'plana'),'prohibidoRegistrarNorma', (SELECT iddeontic FROM deontic WHERE deonticdesc = 'f'), (SELECT idtargetType FROM " +
				"targetType WHERE targetName = 'agentName'), -1, (SELECT idactionNorm FROM actionNorm WHERE description = 'registerNorm' AND numParams = 7), '@prohibidoRegistrarNorma[f,<agentName:_>,registerNorm(_,plana,_,_,_,_,Agent),isAgent(Agent) & not(playsRole(Agent,_,plana)),_]',"
				+ "'registerNorm(_,plana,_,_,_,_,Agent):-isAgent(Agent) & not(playsRole(Agent,_,plana))')");
		dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'prohibidoRegistrarNorma'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
				"'registerNorm' AND numParams = 7), '_')");
		dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'prohibidoRegistrarNorma'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
				"'registerNorm' AND numParams = 7), 'plana')");
		dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'prohibidoRegistrarNorma'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
				"'registerNorm' AND numParams = 7), '_')");
		dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'prohibidoRegistrarNorma'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
				"'registerNorm' AND numParams = 7), '_')");
		dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'prohibidoRegistrarNorma'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
				"'registerNorm' AND numParams = 7), '_')");
		dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'prohibidoRegistrarNorma'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
				"'registerNorm' AND numParams = 7), '_')");
		dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'prohibidoRegistrarNorma'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
				"'registerNorm' AND numParams = 7), 'Agent')");
		//----------------------------------------------------------------------------------------------------------------//

		String NormaPrueba = "@permisoRegistrarNorma[p,<agentName:pruebas>,registerNorm(_,plana,_,_,_,_,pruebas),_,_]";
		
		omsProxy.registerNorm("plana", NormaPrueba);
		
		fail("Should have return an exception, product of a registerNorm not allowed.");
		
	}
	
	@Test(timeout = 5 * 60 * 1000, expected=ForbiddenNormException.class)
	public void testRegisterNormProhibitionDomain2() throws Exception {
		
		//------------------------------------------- Test Initialization  -----------------------------------------------//
		dbA.executeSQL("INSERT INTO `unitList` (`unitName`,`idunitType`) VALUES ('equipo',(SELECT idunitType FROM unitType WHERE unitTypeName = 'team'))");
		dbA.executeSQL("INSERT INTO `unitHierarchy` (`idParentUnit`,`idChildUnit`) VALUES ((SELECT idunitList FROM unitList WHERE unitName = 'virtual'),(SELECT idunitList FROM unitList WHERE unitName = 'equipo'))");
		dbA.executeSQL("INSERT INTO `roleList` (`roleName`,`idunitList`,`idposition`,`idaccessibility`,`idvisibility`) VALUES"+ 
				"('miembro',(SELECT idunitList FROM unitList WHERE unitName = 'equipo'),"+
				"(SELECT idposition FROM position WHERE positionName = 'member'), "+
				"(SELECT idaccessibility FROM accessibility WHERE accessibility = 'external'),"+ 
				"(SELECT idvisibility FROM visibility WHERE visibility = 'public'))");
		dbA.executeSQL("INSERT INTO `roleList` (`roleName`,`idunitList`,`idposition`,`idaccessibility`,`idvisibility`) VALUES"+ 
				"('creador',(SELECT idunitList FROM unitList WHERE unitName = 'equipo'),"+
				"(SELECT idposition FROM position WHERE positionName = 'creator'), "+
				"(SELECT idaccessibility FROM accessibility WHERE accessibility = 'internal'),"+ 
				"(SELECT idvisibility FROM visibility WHERE visibility = 'private'))");
		
		dbA.executeSQL("INSERT INTO `agentPlayList` (`idagentList`, `idroleList`) VALUES ((SELECT idagentList FROM agentList WHERE "
				+ "agentName = 'pruebas'),(SELECT idroleList FROM roleList WHERE (roleName = 'miembro' AND idunitList = (SELECT "
				+ "idunitList FROM unitList WHERE unitName = 'equipo'))))");
		
		dbA.executeSQL("INSERT INTO normList (idunitList, normName, iddeontic, idtargetType, targetValue, idactionNorm, normContent, normRule) " +
				"VALUES ((SELECT idunitList FROM unitList WHERE unitName = 'equipo'),'prohibidoRegistrarNorma', (SELECT iddeontic FROM deontic WHERE deonticdesc = 'f'), (SELECT idtargetType FROM " +
				"targetType WHERE targetName = 'agentName'), -1, (SELECT idactionNorm FROM actionNorm WHERE description = 'registerNorm' AND numParams = 7), '@prohibidoRegistrarNorma[f,<agentName:_>,registerNorm(_,equipo,_,_,_,_,Agent),isAgent(Agent) & playsRole(Agent,miembro,equipo),_]',"
				+ "'registerNorm(_,equipo,_,_,_,_,Agent):-isAgent(Agent) & playsRole(Agent,miembro,equipo)')");
		dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'prohibidoRegistrarNorma'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
				"'registerNorm' AND numParams = 7), '_')");
		dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'prohibidoRegistrarNorma'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
				"'registerNorm' AND numParams = 7), 'equipo')");
		dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'prohibidoRegistrarNorma'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
				"'registerNorm' AND numParams = 7), '_')");
		dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'prohibidoRegistrarNorma'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
				"'registerNorm' AND numParams = 7), '_')");
		dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'prohibidoRegistrarNorma'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
				"'registerNorm' AND numParams = 7), '_')");
		dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'prohibidoRegistrarNorma'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
				"'registerNorm' AND numParams = 7), '_')");
		dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'prohibidoRegistrarNorma'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
				"'registerNorm' AND numParams = 7), 'Agent')");
		//----------------------------------------------------------------------------------------------------------------//

		String NormaPrueba = "@permisoAdquirirRol[p,<roleName:creador>,acquireRole(creador,equipo,Agent),isAgent(Agent) & playsRole(Agent,subordinado,jerarquia) & isUnit(jerarquia) & hasType(jerarquia,hierarchy) & isRole(subordinado,jerarquia),playsRole(Agent,creador,equipo)]";
		
		omsProxy.registerNorm("equipo", NormaPrueba);
		
		fail("Should have return an exception, product of a registerNorm not allowed.");
		
	}
	
	@Test(timeout = 5 * 60 * 1000, expected=ForbiddenNormException.class)
	public void testRegisterNormProhibitionDomain3() throws Exception {
		
		//------------------------------------------- Test Initialization  -----------------------------------------------//
		dbA.executeSQL("INSERT INTO `unitList` (`unitName`,`idunitType`) VALUES ('jerarquia',(SELECT idunitType FROM unitType WHERE unitTypeName = 'hierarchy'))");
		dbA.executeSQL("INSERT INTO `unitHierarchy` (`idParentUnit`,`idChildUnit`) VALUES ((SELECT idunitList FROM unitList WHERE unitName = 'virtual'),(SELECT idunitList FROM unitList WHERE unitName = 'jerarquia'))");
		dbA.executeSQL("INSERT INTO `roleList` (`roleName`,`idunitList`,`idposition`,`idaccessibility`,`idvisibility`) VALUES"+ 
				"('subordinado',(SELECT idunitList FROM unitList WHERE unitName = 'jerarquia'),"+
				"(SELECT idposition FROM position WHERE positionName = 'subordinate'), "+
				"(SELECT idaccessibility FROM accessibility WHERE accessibility = 'external'),"+ 
				"(SELECT idvisibility FROM visibility WHERE visibility = 'public'))");
		dbA.executeSQL("INSERT INTO `roleList` (`roleName`,`idunitList`,`idposition`,`idaccessibility`,`idvisibility`) VALUES"+ 
				"('jefe',(SELECT idunitList FROM unitList WHERE unitName = 'jerarquia'),"+
				"(SELECT idposition FROM position WHERE positionName = 'supervisor'), "+
				"(SELECT idaccessibility FROM accessibility WHERE accessibility = 'external'),"+ 
				"(SELECT idvisibility FROM visibility WHERE visibility = 'public'))");
		dbA.executeSQL("INSERT INTO `roleList` (`roleName`,`idunitList`,`idposition`,`idaccessibility`,`idvisibility`) VALUES"+ 
				"('creador',(SELECT idunitList FROM unitList WHERE unitName = 'jerarquia'),"+
				"(SELECT idposition FROM position WHERE positionName = 'creator'), "+
				"(SELECT idaccessibility FROM accessibility WHERE accessibility = 'internal'),"+ 
				"(SELECT idvisibility FROM visibility WHERE visibility = 'private'))");
		
		dbA.executeSQL("INSERT INTO `agentPlayList` (`idagentList`, `idroleList`) VALUES ((SELECT idagentList FROM agentList WHERE "
				+ "agentName = 'pruebas'),(SELECT idroleList FROM roleList WHERE (roleName = 'jefe' AND idunitList = (SELECT "
				+ "idunitList FROM unitList WHERE unitName = 'jerarquia'))))");
		
		dbA.executeSQL("INSERT INTO normList (idunitList, normName, iddeontic, idtargetType, targetValue, idactionNorm, normContent, normRule) " +
				"VALUES ((SELECT idunitList FROM unitList WHERE unitName = 'jerarquia'),'prohibidoRegistrarNorma', (SELECT iddeontic FROM deontic WHERE deonticdesc = 'f'), (SELECT idtargetType FROM " +
				"targetType WHERE targetName = 'agentName'), -1, (SELECT idactionNorm FROM actionNorm WHERE description = 'registerNorm' AND numParams = 7), '@prohibidoRegistrarNorma[f,<agentName:_>,registerNorm(_,jerarquia,_,_,_,_,Agent),isAgent(Agent) & playsRole(Agent,jefe,jerarquia),_]',"
				+ "'registerNorm(_,jerarquia,_,_,_,_,Agent):-isAgent(Agent) & playsRole(Agent,jefe,jerarquia)')");
		dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'prohibidoRegistrarNorma'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
				"'registerNorm' AND numParams = 7), '_')");
		dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'prohibidoRegistrarNorma'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
				"'registerNorm' AND numParams = 7), 'jerarquia')");
		dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'prohibidoRegistrarNorma'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
				"'registerNorm' AND numParams = 7), '_')");
		dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'prohibidoRegistrarNorma'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
				"'registerNorm' AND numParams = 7), '_')");
		dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'prohibidoRegistrarNorma'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
				"'registerNorm' AND numParams = 7), '_')");
		dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'prohibidoRegistrarNorma'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
				"'registerNorm' AND numParams = 7), '_')");
		dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'prohibidoRegistrarNorma'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
				"'registerNorm' AND numParams = 7), 'Agent')");
		//----------------------------------------------------------------------------------------------------------------//

		String NormaPrueba = "@prohibidoAdquirirRol[f,<roleName:creador>,acquireRole(_,jerarquia,Agent),isAgent(Agent) & playsRole(Agent,creador,jerarquia),_]";
		
		omsProxy.registerNorm("jerarquia", NormaPrueba);
		
		fail("Should have return an exception, product of a registerNorm not allowed.");
		
	}
}
