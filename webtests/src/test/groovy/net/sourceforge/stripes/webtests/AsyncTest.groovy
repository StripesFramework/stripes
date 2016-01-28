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

            // open page
            webDriver.get("${BaseUrl.get()}/async")

            // click link to trigger async resolution
            $$('a') + textEquals("I'm an async event") + at(0) >> click()

            // check async data fetched etc
            def res = $('#result') + $$('p') +
                    textEquals('Displaying 30 commits received asynchronously from GitHub !') +
                    at(0)
            res >> eval()

            def input = $$('input') + attrEquals('name', 'someProp') + whereElemCount(1) + at(0)

            // check that form repop worked
            input + attrEquals('value', 'foobar') >> eval()

            // check that validation works as well in async mode...
            input.clear()
            def btn = $$('input') + attrEquals('name', 'asyncEvent') + at(0)
            btn >> click()
            $$('li') + textEquals('Some Prop is a required field') + whereElemCount(1) >> eval()

            input.sendKeys('skunk')
            btn >> click()
            res >> eval()

        }
    }

}
