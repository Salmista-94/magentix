package organization.TestDataBaseInterface;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.junit.*;

import organization.TestDataBaseInterface.DatabaseAccess;
import es.upv.dsic.gti_ia.organization.DataBaseInterface;


/** 
 * @author Jose Alemany Bordera  -  jalemany1@dsic.upv.es
 * 
 */

public class TestGetAgentsVisibilityRolesInUnit {

	DataBaseInterface dbI = null;
	DatabaseAccess dbA = null;
	private Method m = null;
	
	
	@Before
	public void setUp() throws Exception {
		
		Class[] parameterTypes = new Class[1];
	    parameterTypes[0] = java.lang.String.class;
		
	    m = DataBaseInterface.class.getDeclaredMethod("getAgentsVisibilityRolesInUnit", parameterTypes);
		m.setAccessible(true);
		
		dbA = new DatabaseAccess();

		//-------------  Clean Data Base  ------------//
		dbA.executeSQL("DELETE FROM agentPlayList");
		dbA.executeSQL("DELETE FROM agentList");
		dbA.executeSQL("DELETE FROM actionNormParam");
		dbA.executeSQL("DELETE FROM normList");
		dbA.executeSQL("DELETE FROM roleList WHERE idroleList != 1");
		dbA.executeSQL("DELETE FROM unitHierarchy WHERE idChildUnit != 1");
		dbA.executeSQL("DELETE FROM unitList WHERE idunitList != 1");

		//--------------------------------------------//
	}

	@After
	public void tearDown() throws Exception {

		//-------------  Clean Data Base  ------------//
		dbA.executeSQL("DELETE FROM agentPlayList");
		dbA.executeSQL("DELETE FROM agentList");
		dbA.executeSQL("DELETE FROM actionNormParam");
		dbA.executeSQL("DELETE FROM normList");
		dbA.executeSQL("DELETE FROM roleList WHERE idroleList != 1");
		dbA.executeSQL("DELETE FROM unitHierarchy WHERE idChildUnit != 1");
		dbA.executeSQL("DELETE FROM unitList WHERE idunitList != 1");

		//--------------------------------------------//

		dbA = null;
		
		dbI = null;
		
		m = null;
	}
	
	@Test(timeout = 5 * 1000)
	public void testGetAgentsVisibilityRolesInUnit1() {
		
		/**---------------------------------------------------------------------------------
		 * --			1.	
		 * --				- All parameters are correct
		 * --				- Unit has got 2 roles with agents associated
		 * --				- Show correctly the information
		 * ---------------------------------------------------------------------------------
		 */
		
		try {	
			
			//------------------------------------------- Test Initialization  -----------------------------------------------//
			//Test variables
			String unit = "proofUnitFlat";
			String eAgent = "exampleAgent";
			String eAgent2 = "exampleAgent2";
			String eRoleMember = "exampleRoleMember";
			String eRoleCreator = "exampleRoleCreator";
			//Data Base 
			dbA.executeSQL("INSERT INTO `unitList` (`unitName`,`idunitType`) VALUES ('"+ unit +"',(SELECT idunitType FROM unitType WHERE unitTypeName = 'flat'))");
			dbA.executeSQL("INSERT INTO `unitHierarchy` (`idParentUnit`,`idChildUnit`) VALUES ((SELECT idunitList FROM unitList WHERE unitName = 'virtual'),(SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'))");
			dbA.executeSQL("INSERT INTO `roleList` (`roleName`,`idunitList`,`idposition`,`idaccessibility`,`idvisibility`) VALUES"+ 
					"('"+ eRoleMember +"',(SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'),"+
					"(SELECT idposition FROM position WHERE positionName = 'member'), "+
					"(SELECT idaccessibility FROM accessibility WHERE accessibility = 'external'),"+ 
					"(SELECT idvisibility FROM visibility WHERE visibility = 'public'))");
			dbA.executeSQL("INSERT INTO `roleList` (`roleName`,`idunitList`,`idposition`,`idaccessibility`,`idvisibility`) VALUES"+ 
					"('"+ eRoleCreator +"',(SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'),"+
					"(SELECT idposition FROM position WHERE positionName = 'creator'), "+
					"(SELECT idaccessibility FROM accessibility WHERE accessibility = 'internal'),"+ 
					"(SELECT idvisibility FROM visibility WHERE visibility = 'private'))");

			dbA.executeSQL("INSERT INTO `agentList` (`agentName`) VALUES ('"+ eAgent +"')");
			dbA.executeSQL("INSERT INTO `agentPlayList` (`idagentList`, `idroleList`) VALUES ((SELECT idagentList FROM agentList " +
					"WHERE agentName = '"+ eAgent +"'),(SELECT idroleList FROM roleList WHERE (roleName = '"+ eRoleMember +"' AND " +
					"idunitList = (SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'))))");
			dbA.executeSQL("INSERT INTO `agentPlayList` (`idagentList`, `idroleList`) VALUES ((SELECT idagentList FROM agentList " +
					"WHERE agentName = '"+ eAgent +"'),(SELECT idroleList FROM roleList WHERE (roleName = '"+ eRoleCreator +"' AND " +
					"idunitList = (SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'))))");
			
			dbA.executeSQL("INSERT INTO `agentList` (`agentName`) VALUES ('"+ eAgent2 +"')");
			dbA.executeSQL("INSERT INTO `agentPlayList` (`idagentList`, `idroleList`) VALUES ((SELECT idagentList FROM agentList " +
					"WHERE agentName = '"+ eAgent2 +"'),(SELECT idroleList FROM roleList WHERE (roleName = '"+ eRoleMember +"' AND " +
					"idunitList = (SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'))))");
			dbA.executeSQL("INSERT INTO `agentPlayList` (`idagentList`, `idroleList`) VALUES ((SELECT idagentList FROM agentList " +
					"WHERE agentName = '"+ eAgent2 +"'),(SELECT idroleList FROM roleList WHERE (roleName = '"+ eRoleCreator +"' AND " +
					"idunitList = (SELECT idunitList FROM unitList WHERE unitName = '"+ unit +"'))))");
			//----------------------------------------------------------------------------------------------------------------//
			
			dbI = new DataBaseInterface();
			
			Object[] parameters = new Object[1];
			parameters[0] = unit;
		    
		    ArrayList<ArrayList<String>> result = (ArrayList<ArrayList<String>>) m.invoke(dbI, parameters);
			assertEquals("The message should be:", "[["+ eAgent +", "+ eRoleMember +"], ["+ eAgent2 +", "+ eRoleMember +"]]", result.toString());

		} catch(InvocationTargetException e) {
			
			fail(e.getTargetException().getMessage());
			
		} catch(Exception e) {
			
			fail(e.getMessage());
			
		}
	}
	
	@Test(timeout = 5 * 1000)
	public void testGetAgentsRolesInUnit2() {
		
		/**---------------------------------------------------------------------------------
		 * --			2.	
		 * --				- Any parameters are incorrect
		 * --				- Unit not exist
		 * --				- Not return information
		 * ---------------------------------------------------------------------------------
		 */
		
		try {	
		
			dbI = new DataBaseInterface();
			
			Object[] parameters = new Object[1];
			parameters[0] = "NotExists";
		    
		    ArrayList<ArrayList<String>> result = (ArrayList<ArrayList<String>>) m.invoke(dbI, parameters);
			assertEquals("The message should be:", "[]", result.toString());

		} catch(InvocationTargetException e) {
			
			fail(e.getTargetException().getMessage());
			
		} catch(Exception e) {
			
			fail(e.getMessage());
			
		}
	}
}
