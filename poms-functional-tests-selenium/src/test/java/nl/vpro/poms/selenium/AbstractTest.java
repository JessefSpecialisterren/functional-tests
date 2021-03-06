package nl.vpro.poms.selenium;

import io.github.bonigarcia.wdm.DriverManagerType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import org.junit.*;
import org.junit.rules.TestName;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.LoggerFactory;

import nl.vpro.api.client.utils.Config;
import nl.vpro.poms.selenium.pages.*;
import nl.vpro.poms.selenium.poms.pages.Search;
import nl.vpro.poms.selenium.util.WebDriverFactory;
import nl.vpro.poms.selenium.util.WebDriverFactory.Browser;
import nl.vpro.poms.selenium.util.WebDriverUtil;
import nl.vpro.rules.DoAfterException;
import nl.vpro.rules.TestMDC;

/**
 *
 */
@RunWith(Parameterized.class)
public abstract class AbstractTest {
    static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AbstractTest.class);
    protected org.slf4j.Logger log = LoggerFactory.getLogger(getClass());


    public static final Config CONFIG =
            new Config("npo-functional-tests.properties", "npo-browser-tests.properties");

    public static final String MID = "WO_VPRO_025057";

    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.MINUTES);

    @Rule
    public TestMDC testMDC = new TestMDC();

    @Rule
    public TestName testMethod = new TestName();

    protected final Browser browser;

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected WebDriverUtil webDriverUtil;

    protected static Map<Browser, WebDriver> staticDrivers = new HashMap<>();

    protected static Map<Class<?>, Boolean> loggedAboutSetupEach = new HashMap<>();
    protected boolean setupEach = true;

    protected LogEntries browserLogs;

    @Rule
    public DoAfterException doAfterException = new DoAfterException((t) -> {
        if (! (t instanceof AssumptionViolatedException)) {
            AbstractTest.exceptions.put(getClass(), t);
        }
    });


    protected  static final Map<Class<?>, Throwable> exceptions = new ConcurrentHashMap<>();


    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        List<Object[]> result = new ArrayList<>();
        List<String> browsers = Arrays.asList(CONFIG.getProperty("browsers").split("\\s*,\\s*"));
        if (browsers.contains("chrome")) {
            result.add(new Object[]{new Browser(DriverManagerType.CHROME, "2.41")}); // 2.41 corresponds with the chrome on jenkins.
        }
        if (browsers.contains("firefox")) {
            result.add(new Object[]{new Browser(DriverManagerType.FIREFOX, null)});
        }
        return result;
    }

    protected AbstractTest(@Nonnull Browser browser) {
        this.browser = browser;
        this.setupEach = this.getClass().getAnnotation(FixMethodOrder.class) == null;
        if (!this.setupEach && !loggedAboutSetupEach.getOrDefault(getClass(), false)) {
            log.info("\nRunning" + getClass() + " with fixed method order, so keeping the driver between the tests");
            loggedAboutSetupEach.put(getClass(), true);
        }

    }

    @Before
    public void setUp() {
        if (setupEach) {
            driver = createDriver(browser);
        } else {
            driver = staticDrivers.computeIfAbsent(browser, AbstractTest::createDriver);
        }
        browserLogs = driver.manage().logs().get("browser");
        webDriverUtil  = new WebDriverUtil(driver, log);
        wait = webDriverUtil.w();
    }

    private static WebDriver createDriver(Browser browser) {
        try {
            WebDriver driver = browser.asWebDriver();
            // The dimension of the browser should be big enough, (headless browser seem to be small!), otherwise test will keep waiting forever
            Dimension d = new Dimension(1200, 1000);
            driver.manage().window().setSize(d);
            return driver;
        } catch (Exception e) {
            LOG.error("Could not create driver for " + browser + ":" + e.getMessage(), e);
            throw e;
        }
    }

    @After
    public void tearDown() {
        log.info("{}", browserLogs.getAll());

        if (setupEach) {
            if (driver != null) {
                /*
                 * In head mode we experience the issue with driver.close()
                 * org.openqa.selenium.NoSuchSessionException: Tried to run command without establishing a connection
                 */
                if (WebDriverFactory.headless) {
                    driver.close();
                }
                driver.quit();
            }
            webDriverUtil = null;
            wait = null;
        }
    }

    @AfterClass
    public static void tearDownClass() {
        if (exceptions.isEmpty() || WebDriverFactory.headless) {
            for (WebDriver wd : staticDrivers.values()) {
                wd.close();
                wd.quit();

            }
            staticDrivers.clear();
            loggedAboutSetupEach.clear();
        } else {
            LOG.warn("Not closing browser because of test failures {}", exceptions);
        }
    }

    protected KeycloakLogin keycloakLogin(String url) {
        return new KeycloakLogin(url, webDriverUtil);
    }

    protected CasLogin casLogin(String url) {
        return new CasLogin(url, webDriverUtil);
    }
    protected abstract AbstractLogin login();

    protected void logout() {
        if (driver != null) {
            Search search = new Search(webDriverUtil);
            search.logout();
        } else {
            log.error("Cannot logout because no driver");
        }
    }

}
