package nl.vpro.poms.selenium.poms.npogebruiker;

import static nl.vpro.poms.selenium.util.Config.CONFIG;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import nl.vpro.poms.selenium.pages.AccountSettingsOverlayPage;
import nl.vpro.poms.selenium.pages.Login;
import nl.vpro.poms.selenium.pages.Search;
import nl.vpro.poms.selenium.poms.AbstractTest;

public class NPOGebruikerTest extends AbstractTest {
	
	@Test
	public void testNPOGebruiker() {
		loginSpeciaalNPOGebruiker();
		
		Search search = new Search(driver);
    	search.goToAccountInstellingen();
    	
    	AccountSettingsOverlayPage overlayPage = new AccountSettingsOverlayPage(driver);
    	List<WebElement> roles = overlayPage.getRoles();
    	boolean hasRole = overlayPage.hasRole("MEDIA_MIS");
    	Assert.assertTrue(hasRole);
    	overlayPage.close();
		logout();
	}
	
	@Test
	public void testEigenaar() {
loginSpeciaalNPOGebruiker();
		
		Search search = new Search(driver);
    	search.goToAccountInstellingen();
    	
    	AccountSettingsOverlayPage overlayPage = new AccountSettingsOverlayPage(driver);
    	overlayPage.checkOwner("BROADCASTER");
    	overlayPage.clickOpslaan();
    	
    	search.goToAccountInstellingen();
    	overlayPage.checkOwner("");
    	overlayPage.clickOpslaan();
    	
//    	overlayPage.close();
//		logout();
	}
	
	private void loginSpeciaalNPOGebruiker() {
		Login login = new Login(driver);
		login.gotoPage();
		String user = CONFIG.getProperties().get("MISGebruiker.LOGIN");
		String password = CONFIG.getProperties().get("MISGebruiker.PASSWORD");
		login.login(user, password);
	}

	private void logout() {
		Search search = new Search(driver);
		search.logout();
	}
}
