import configuration.GoogleChrome;
import junitparams.JUnitParamsRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.junit.Test;
import junitparams.Parameters;

import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

@RunWith(JUnitParamsRunner.class)
public class test {
    private static final Logger logger = LogManager.getLogger(test.class);

    /**
     * Provides parameters for tests.
     *
     * @return Object[]
     */
    public Object[] parametersForCreateNewClaim() {
        logger.info("Reading excel sheet for test data.");
        return ExcelUtil.getData("Data.xlsx", "Vehicle",0);
    }

    @Test
    @Parameters
    public void createNewClaim(JSONObject data) throws Exception {

        GoogleChrome gc = new GoogleChrome();
        WebDriver webDriver = gc.startChromeDriver();

        webDriver.navigate().to("https://" +data.getString("Website"));
        logger.info("Navigated to data.getString(\"Website\")");
        Thread.sleep(5*1000);

        gc.stopChromeAndDriver();
        webDriver.quit();

    }
}

