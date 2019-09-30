package configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
@EnableAutoConfiguration(exclude={MongoAutoConfiguration.class})
public class GoogleChrome {
    private static final Logger logger = LogManager.getLogger(GoogleChrome.class);
    private String rootPath = System.getProperty("user.dir").replace("\\","/");

    private int port = -1;
    private int chromeDriverProcessID = -1;
    private int chromeProcessID = -1;



    public WebDriver startChromeDriver() {
        logger.info("Chrome driver path : " + rootPath + "/Tools/Drivers/chromedriver.exe");
        System.setProperty("webdriver.chrome.driver", rootPath + "/Tools/Drivers/chromedriver.exe");

        Map<String, Object> prefs = new HashMap<String, Object>();
        logger.info("Disabling Chrome's credentials service");
        prefs.put("credentials_enable_service", false);
        logger.info("Disabling Chrome's password manager");
        prefs.put("password_manager_enabled", false);


        final String regex = "^\\D*$";
        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(System.getProperty("user.name"));
        boolean isHuman = matcher.matches();



        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", prefs);
        logger.info("Disabling Chrome's info bars");
        options.addArguments("disable-infobars");
        options.addArguments("--incognito");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--allow-insecure-localhost");

        if (isHuman){
            logger.info("Chrome starting maximized - isHuman: " +isHuman + " process run by " +System.getProperty("user.name"));
            options.addArguments("--start-maximized");
        } else {
            logger.info("Chrome starting headless - isHuman: " +isHuman + " process run by " +System.getProperty("user.name")) ;
            options.addArguments("--headless");
            options.addArguments("--window-size=1980,1080");
        }
        options.setAcceptInsecureCerts(true);


        logger.info("Starting Chrome browser...");

        ChromeDriverService chromeDriverService = ChromeDriverService.createDefaultService();
        WebDriver driver = new ChromeDriver(chromeDriverService,options);

        logger.info("Window size: "+ driver.manage().window().getSize());
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);



        try {
            port = chromeDriverService.getUrl().getPort();
            chromeDriverProcessID = GetChromeDriverProcessID(port);
            chromeProcessID = GetChromeProcesID(chromeDriverProcessID);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("starting chromedriver on port " + port);
        logger.info("detected chromedriver process id " + chromeDriverProcessID);
        logger.info("detected chrome process id " + chromeProcessID);

        return driver;
    }

    private int GetChromeDriverProcessID(int aPort) throws IOException, InterruptedException
    {
        logger.info("Getting process ID for Chrome...");

        String[] commandArray = new String[3];

        commandArray[0] = "cmd";
        commandArray[1] = "/c";
        commandArray[2] = "netstat -aon | findstr LISTENING | findstr " + aPort;


        logger.info("running command " + commandArray[2]);

        Process p = Runtime.getRuntime().exec(commandArray);
        p.waitFor();

        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

        StringBuilder sb = new StringBuilder();
        String line = "";
        while ((line = reader.readLine()) != null)
        {
            sb.append(line + "\n");
        }

        String result = sb.toString().trim();

        logger.info("parse command response line:");
        logger.info(result);

        return ParseChromeDriverWindows(result);
    }

    private static int GetChromeProcesID(int chromeDriverProcessID) throws IOException, InterruptedException
    {
        String[] commandArray = new String[3];


        commandArray[0] = "cmd";
        commandArray[1] = "/c";
        commandArray[2] = "wmic process get processid,parentprocessid,executablepath | find \"chrome.exe\" |find \"" + chromeDriverProcessID + "\"";


        logger.info("running command " + commandArray[2]);

        Process p = Runtime.getRuntime().exec(commandArray);
        p.waitFor();

        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

        StringBuilder sb = new StringBuilder();
        String line = "";
        while ((line = reader.readLine()) != null)
        {
            sb.append(line + "\n");
        }

        String result = sb.toString().trim();

        logger.info("parse command response line:");
        logger.info(result);

        return ParseChromeWindows(result);
    }

    private static int ParseChromeWindows(String result)
    {
        String[] pieces = result.split("\\s+");
        // C:\Program Files (x86)\Google\Chrome\Application\chrome.exe 14304 19960
        return Integer.parseInt(pieces[pieces.length - 1]);
    }


    private static int ParseChromeDriverWindows(String netstatResult)
    {
        String[] pieces = netstatResult.split("\\s+");
        // TCP 127.0.0.1:26599 0.0.0.0:0 LISTENING 22828
        return Integer.parseInt(pieces[pieces.length - 1]);
    }

    public void stopChromeAndDriver(){
        try {
            logger.info("Stopping chromedriver.exe with PID " + chromeDriverProcessID);
            String killChromeDriver = "taskkill /F /PID " + chromeDriverProcessID;
            Runtime.getRuntime().exec(killChromeDriver);
            logger.info("chromedriver.exe with PID "+chromeDriverProcessID+" stopped successfully");

            logger.info("Stopping chrome.exe with PID " + chromeProcessID);
            String killChrome = "taskkill /F /PID " + chromeProcessID;
            Runtime.getRuntime().exec(killChrome);
            logger.info("chrome.exe with PID "+chromeProcessID+" stopped successfully");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
