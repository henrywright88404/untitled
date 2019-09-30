package configuration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Set;

@Configuration
@EnableAutoConfiguration(exclude = {org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration.class, MongoAutoConfiguration.class})
public class WebDriverConfig {

    //This class with generate webdriver bean and will initialize at component scanning by springboot.


    @Bean
    WebDriver webDriver(){
        return new WebDriver() {
            public void get(String s) {
            }

            public String getCurrentUrl() {
                return null;
            }

            public String getTitle() {
                return null;
            }

            public List<WebElement> findElements(By by) {
                return null;
            }

            public WebElement findElement(By by) {
                return null;
            }

            public String getPageSource() {
                return null;
            }

            public void close() {

            }

            public void quit() {

            }

            public Set<String> getWindowHandles() {
                return null;
            }

            public String getWindowHandle() {
                return null;
            }

            public TargetLocator switchTo() {
                return null;
            }

            public Navigation navigate() {
                return null;
            }

            public Options manage() {
                return null;
            }
        };
    }
}
