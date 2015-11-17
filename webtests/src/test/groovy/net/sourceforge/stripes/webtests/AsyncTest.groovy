package net.sourceforge.stripes.webtests

import com.pojosontheweb.selenium.ManagedDriverJunit4TestBase
import org.junit.Test
import org.pojosontheweb.selenium.groovy.DollrCategory
import org.pojosontheweb.selenium.groovy.FindrCategory
import org.pojosontheweb.selenium.groovy.ListFindrCategory
import org.pojosontheweb.selenium.groovy.WebDriverCategory
import static com.pojosontheweb.selenium.Findrs.*

class AsyncTest extends ManagedDriverJunit4TestBase {

    @Test
    void async() {
        use(WebDriverCategory, FindrCategory, ListFindrCategory, DollrCategory) {
            webDriver.get('http://localhost:9999/webtests/async')
            $$('a') + textEquals("I'm an async event") + at(0) >> click()
            $('#result') +
                $$('p') +
                textEquals('Displaying 30 commits received asynchronously from GitHub !') +
                at(0) >> eval()
        }
    }

}
