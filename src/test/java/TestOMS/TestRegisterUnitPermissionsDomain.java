package TestOMS;

import junit.framework.TestCase;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;
import es.upv.dsic.gti_ia.organization.OMS;
import es.upv.dsic.gti_ia.organization.OMSProxy;
import es.upv.dsic.gti_ia.organization.SF;
import es.upv.dsic.gti_ia.organization.exception.THOMASException;


/** 
 * @author Jose Alemany Bordera  -  jalemany1@dsic.upv.es
 * 
 */


public class TestRegisterUnitPermissionsDomain extends TestCase {
	
	OMSProxy omsProxy = null;
	DatabaseAccess dbA = null;


	Agent agent = null;

	OMS oms = null;
	SF sf = null;

	protected void tearDown() throws Exception {

		//------------------Clean Data Base -----------//
		dbA.executeSQL("DELETE FROM agentPlayList");
		dbA.executeSQL("DELETE FROM agentList");
		dbA.executeSQL("DELETE FROM actionNormParam");
		dbA.executeSQL("DELETE FROM normList");
		dbA.executeSQL("DELETE FROM roleList WHERE idroleList != 1");
		dbA.executeSQL("DELETE FROM unitHierarchy WHERE idChildUnit != 1");
		dbA.executeSQL("DELETE FROM unitList WHERE idunitList != 1");


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

	}
	protected void setUp() throws Exception {
		super.setUp();


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

	
	public void testRegisterUnitPermissionsDomain1() {
		
		try{
			
			//------------------------------------------- Test Initialization  -----------------------------------------------//
			dbA.executeSQL("INSERT INTO `unitList` (`unitName`,`idunitType`) VALUES ('plana',(SELECT idunitType FROM unitType WHERE unitTypeName = 'flat'))");
			dbA.executeSQL("INSERT INTO `unitHierarchy` (`idParentUnit`,`idChildUnit`) VALUES ((SELECT idunitList FROM unitList WHERE unitName = 'virtual'),(SELECT idunitList FROM unitList WHERE unitName = 'plana'))");
			dbA.executeSQL("INSERT INTO `roleList` (`roleName`,`idunitList`,`idposition`,`idaccessibility`,`idvisibility`) VALUES"+ 
					"('miembro',(SELECT idunitList FROM unitList WHERE unitName = 'plana'),"+
					"(SELECT idposition FROM position WHERE positionName = 'member'), "+
					"(SELECT idaccessibility FROM accessibility WHERE accessibility = 'external'),"+ 
					"(SELECT idvisibility FROM visibility WHERE visibility = 'public'))");
			dbA.executeSQL("INSERT INTO `roleList` (`roleName`,`idunitList`,`idposition`,`idaccessibility`,`idvisibility`) VALUES"+ 
					"('creador',(SELECT idunitList FROM unitList WHERE unitName = 'plana'),"+
					"(SELECT idposition FROM position WHERE positionName = 'creator'), "+
					"(SELECT idaccessibility FROM accessibility WHERE accessibility = 'internal'),"+ 
					"(SELECT idvisibility FROM visibility WHERE visibility = 'private'))");
			
			dbA.executeSQL("INSERT INTO `unitList` (`unitName`,`idunitType`) VALUES ('plana2',(SELECT idunitType FROM unitType WHERE unitTypeName = 'flat'))");
			dbA.executeSQL("INSERT INTO `unitHierarchy` (`idParentUnit`,`idChildUnit`) VALUES ((SELECT idunitList FROM unitList WHERE unitName = 'virtual'),(SELECT idunitList FROM unitList WHERE unitName = 'plana2'))");
			dbA.executeSQL("INSERT INTO `roleList` (`roleName`,`idunitList`,`idposition`,`idaccessibility`,`idvisibility`) VALUES"+ 
					"('miembro',(SELECT idunitList FROM unitList WHERE unitName = 'plana2'),"+
					"(SELECT idposition FROM position WHERE positionName = 'member'), "+
					"(SELECT idaccessibility FROM accessibility WHERE accessibility = 'external'),"+ 
					"(SELECT idvisibility FROM visibility WHERE visibility = 'public'))");
			dbA.executeSQL("INSERT INTO `roleList` (`roleName`,`idunitList`,`idposition`,`idaccessibility`,`idvisibility`) VALUES"+ 
					"('creador',(SELECT idunitList FROM unitList WHERE unitName = 'plana2'),"+
					"(SELECT idposition FROM position WHERE positionName = 'creator'), "+
					"(SELECT idaccessibility FROM accessibility WHERE accessibility = 'internal'),"+ 
					"(SELECT idvisibility FROM visibility WHERE visibility = 'private'))");
			
			dbA.executeSQL("INSERT INTO `agentPlayList` (`idagentList`, `idroleList`) VALUES ((SELECT idagentList FROM agentList WHERE "
					+ "agentName = 'pruebas'),(SELECT idroleList FROM roleList WHERE (roleName = 'miembro' AND idunitList = (SELECT "
					+ "idunitList FROM unitList WHERE unitName = 'plana2'))))");

			dbA.executeSQL("INSERT INTO normList (idunitList, normName, iddeontic, idtargetType, targetValue, idactionNorm, normContent, normRule) " +
					"VALUES ((SELECT idunitList FROM unitList WHERE unitName = 'plana'),'permisoRegistrarUnidad', (SELECT iddeontic FROM deontic WHERE deonticdesc = 'p'), (SELECT idtargetType FROM " +
					"targetType WHERE targetName = 'agentName'),(SELECT idagentList FROM agentList WHERE agentName = 'pruebas'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
					"'registerUnit' AND numParams = 5), '@permisoRegistrarUnidad[p,<agentName:pruebas>,registerUnit(_,_,plana,_,_),_,_]', 'registerUnit(_,_,plana,_,_)')");
			dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'permisoRegistrarUnidad'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
					"'registerUnit' AND numParams = 5), '_')");
			dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'permisoRegistrarUnidad'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
					"'registerUnit' AND numParams = 5), '_')");
			dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'permisoRegistrarUnidad'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
					"'registerUnit' AND numParams = 5), 'plana')");
			dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'permisoRegistrarUnidad'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
					"'registerUnit' AND numParams = 5), '_')");
			dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'permisoRegistrarUnidad'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
					"'registerUnit' AND numParams = 5), '_')");
			//----------------------------------------------------------------------------------------------------------------//
			
			String result = omsProxy.registerUnit("plana3", "flat", "plana","creador");
			assertEquals("The message should be:", "plana3 created", result);
			
			boolean res = dbA.executeQuery("SELECT * FROM agentPlayList WHERE idroleList = (SELECT idroleList FROM roleList WHERE roleName = 'creador' and idunitList = (SELECT idunitList FROM unitList WHERE unitName = 'plana3'))");
			assertEquals(true,res);
			
		} catch(THOMASException e) {

			fail(e.getMessage());

		} catch(Exception e) {
			
			fail(e.getMessage());
			
		}
	}
	
	public void testRegisterUnitPermissionsDomain2() {
		
		try{
			
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
					"VALUES ((SELECT idunitList FROM unitList WHERE unitName = 'equipo'),'permisoRegistrarUnidad', (SELECT iddeontic FROM deontic WHERE deonticdesc = 'p'), (SELECT idtargetType FROM " +
					"targetType WHERE targetName = 'positionName'),(SELECT idposition FROM position WHERE positionName = 'member'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
					"'registerUnit' AND numParams = 5), '@permisoRegistrarUnidad[p,<positionName:member>,registerUnit(_,_,equipo,_,_),_,_]', 'registerUnit(_,_,equipo,_,_)')");
			dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'permisoRegistrarUnidad'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
					"'registerUnit' AND numParams = 5), '_')");
			dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'permisoRegistrarUnidad'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
					"'registerUnit' AND numParams = 5), '_')");
			dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'permisoRegistrarUnidad'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
					"'registerUnit' AND numParams = 5), 'equipo')");
			dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'permisoRegistrarUnidad'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
					"'registerUnit' AND numParams = 5), '_')");
			dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'permisoRegistrarUnidad'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
					"'registerUnit' AND numParams = 5), '_')");
			//----------------------------------------------------------------------------------------------------------------//
			
			String result = omsProxy.registerUnit("plana", "flat", "equipo","creador");
			assertEquals("The message should be:", "plana created", result);
			
			boolean res = dbA.executeQuery("SELECT * FROM agentPlayList WHERE idroleList = (SELECT idroleList FROM roleList WHERE roleName = 'creador' and idunitList = (SELECT idunitList FROM unitList WHERE unitName = 'plana'))");
			assertEquals(true,res);
			
		} catch(THOMASException e) {

			fail(e.getMessage());

		} catch(Exception e) {
			
			fail(e.getMessage());
			
		}
	}
	
	public void testRegisterUnitPermissionsDomain3() {
		
		try{
			
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
					+ "agentName = 'pruebas'),(SELECT idroleList FROM roleList WHERE (roleName = 'creador' AND idunitList = (SELECT "
					+ "idunitList FROM unitList WHERE unitName = 'equipo'))))");

			dbA.executeSQL("INSERT INTO normList (idunitList, normName, iddeontic, idtargetType, targetValue, idactionNorm, normContent, normRule) " +
					"VALUES ((SELECT idunitList FROM unitList WHERE unitName = 'equipo'),'permisoRegistrarUnidad', (SELECT iddeontic FROM deontic WHERE deonticdesc = 'p'), (SELECT idtargetType FROM " +
					"targetType WHERE targetName = 'positionName'),(SELECT idposition FROM position WHERE positionName = 'member'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
					"'registerUnit' AND numParams = 5), '@permisoRegistrarUnidad[p,<positionName:member>,registerUnit(_,_,equipo,_,_),_,_]', 'registerUnit(_,_,equipo,_,_)')");
			dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'permisoRegistrarUnidad'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
					"'registerUnit' AND numParams = 5), '_')");
			dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'permisoRegistrarUnidad'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
					"'registerUnit' AND numParams = 5), '_')");
			dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'permisoRegistrarUnidad'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
					"'registerUnit' AND numParams = 5), 'equipo')");
			dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'permisoRegistrarUnidad'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
					"'registerUnit' AND numParams = 5), '_')");
			dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'permisoRegistrarUnidad'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
					"'registerUnit' AND numParams = 5), '_')");
			//----------------------------------------------------------------------------------------------------------------//
			
			String result = omsProxy.registerUnit("plana", "flat", "equipo","creador");
			assertEquals("The message should be:", "plana created", result);
			
			boolean res = dbA.executeQuery("SELECT * FROM agentPlayList WHERE idroleList = (SELECT idroleList FROM roleList WHERE roleName = 'creador' and idunitList = (SELECT idunitList FROM unitList WHERE unitName = 'plana'))");
			assertEquals(true,res);
			
		} catch(THOMASException e) {

			fail(e.getMessage());

		} catch(Exception e) {
			
			fail(e.getMessage());
			
		}
	}
	
	public void testRegisterUnitPermissionsDomain4() {
		
		try{
			
			//------------------------------------------- Test Initialization  -----------------------------------------------//
			dbA.executeSQL("INSERT INTO `unitList` (`unitName`,`idunitType`) VALUES ('jerarquia',(SELECT idunitType FROM unitType WHERE unitTypeName = 'hierarchy'))");
			dbA.executeSQL("INSERT INTO `unitHierarchy` (`idParentUnit`,`idChildUnit`) VALUES ((SELECT idunitList FROM unitList WHERE unitName = 'virtual'),(SELECT idunitList FROM unitList WHERE unitName = 'jerarquia'))");
			dbA.executeSQL("INSERT INTO `roleList` (`roleName`,`idunitList`,`idposition`,`idaccessibility`,`idvisibility`) VALUES"+ 
					"('subordinado',(SELECT idunitList FROM unitList WHERE unitName = 'jerarquia'),"+
					"(SELECT idposition FROM position WHERE positionName = 'subordinate'), "+
					"(SELECT idaccessibility FROM accessibility WHERE accessibility = 'external'),"+ 
					"(SELECT idvisibility FROM visibility WHERE visibility = 'public'))");
			dbA.executeSQL("INSERT INTO `roleList` (`roleName`,`idunitList`,`idposition`,`idaccessibility`,`idvisibility`) VALUES"+ 
					"('supervisor',(SELECT idunitList FROM unitList WHERE unitName = 'jerarquia'),"+
					"(SELECT idposition FROM position WHERE positionName = 'supervisor'), "+
					"(SELECT idaccessibility FROM accessibility WHERE accessibility = 'external'),"+ 
					"(SELECT idvisibility FROM visibility WHERE visibility = 'public'))");
			dbA.executeSQL("INSERT INTO `roleList` (`roleName`,`idunitList`,`idposition`,`idaccessibility`,`idvisibility`) VALUES"+ 
					"('creador',(SELECT idunitList FROM unitList WHERE unitName = 'jerarquia'),"+
					"(SELECT idposition FROM position WHERE positionName = 'creator'), "+
					"(SELECT idaccessibility FROM accessibility WHERE accessibility = 'internal'),"+ 
					"(SELECT idvisibility FROM visibility WHERE visibility = 'private'))");
			
			dbA.executeSQL("INSERT INTO `agentPlayList` (`idagentList`, `idroleList`) VALUES ((SELECT idagentList FROM agentList WHERE "
					+ "agentName = 'pruebas'),(SELECT idroleList FROM roleList WHERE (roleName = 'subordinado' AND idunitList = (SELECT "
					+ "idunitList FROM unitList WHERE unitName = 'jerarquia'))))");

			dbA.executeSQL("INSERT INTO normList (idunitList, normName, iddeontic, idtargetType, targetValue, idactionNorm, normContent, normRule) " +
					"VALUES ((SELECT idunitList FROM unitList WHERE unitName = 'jerarquia'),'permisoRegistrarUnidad', (SELECT iddeontic FROM deontic WHERE deonticdesc = 'p'), (SELECT idtargetType FROM " +
					"targetType WHERE targetName = 'roleName'),-1, (SELECT idactionNorm FROM actionNorm WHERE description = 'registerUnit' AND numParams = 5), "
					+ "'@permisoRegistrarUnidad[p,<roleName:_>,registerUnit(_,_,jerarquia,_,_),_,_]', 'registerUnit(_,_,jerarquia,_,_)')");
			dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'permisoRegistrarUnidad'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
					"'registerUnit' AND numParams = 5), '_')");
			dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'permisoRegistrarUnidad'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
					"'registerUnit' AND numParams = 5), '_')");
			dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'permisoRegistrarUnidad'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
					"'registerUnit' AND numParams = 5), 'jerarquia')");
			dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'permisoRegistrarUnidad'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
					"'registerUnit' AND numParams = 5), '_')");
			dbA.executeSQL("INSERT INTO actionNormParam (idnormList, idactionNorm, value) VALUES ((SELECT idnormList FROM normList WHERE normName = 'permisoRegistrarUnidad'), (SELECT idactionNorm FROM actionNorm WHERE description = " +
					"'registerUnit' AND numParams = 5), '_')");
			//----------------------------------------------------------------------------------------------------------------//
			
			String result = omsProxy.registerUnit("plana", "flat", "jerarquia","creador");
			assertEquals("The message should be:", "plana created", result);
			
			boolean res = dbA.executeQuery("SELECT * FROM agentPlayList WHERE idroleList = (SELECT idroleList FROM roleList WHERE roleName = 'creador' and idunitList = (SELECT idunitList FROM unitList WHERE unitName = 'plana'))");
			assertEquals(true,res);
			
		} catch(THOMASException e) {

			fail(e.getMessage());

		} catch(Exception e) {
			
			fail(e.getMessage());
			
		}
	}
}
