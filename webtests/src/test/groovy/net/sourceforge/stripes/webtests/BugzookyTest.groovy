package net.sourceforge.stripes.webtests

import com.pojosontheweb.selenium.Findr
import com.pojosontheweb.selenium.ManagedDriverJunit4TestBase
import com.pojosontheweb.selenium.formz.Select
import org.junit.Test
import org.openqa.selenium.By
import org.pojosontheweb.selenium.groovy.DollrCategory
import org.pojosontheweb.selenium.groovy.FindrCategory
import org.pojosontheweb.selenium.groovy.ListFindrCategory
import org.pojosontheweb.selenium.groovy.WebDriverCategory
import static com.pojosontheweb.selenium.Findrs.*

class BugzookyTest extends ManagedDriverJunit4TestBase {

    def homeUrl = "${BaseUrl.get()}/bugzooky"

    int count = System.currentTimeMillis()

    @Test
    void testAuthenticationIsRequired() {
        [
            'BugList.action',
            'SingleBug.action',
            'MultiBug.action',
            'AdministerComponents.action'
        ].each { p ->
            webDriver.get "$homeUrl/$p"
            use(WebDriverCategory, FindrCategory, ListFindrCategory, DollrCategory) {
                $('#pageContent') +
                    $$('.sectionTitle') +
                    at(0) +
                    textEquals('Login') >> eval()
            }
        }
    }

    private Findr input(String name) {
        findr().elemList(By.tagName('input')).where(attrEquals('name', name)).at(0)
    }

    private String username() {
        "u$count"
    }

    private String register() {
        use(WebDriverCategory, FindrCategory, ListFindrCategory, DollrCategory) {
            count++
            def username = username()
            webDriver.get "$homeUrl/Register.action"

            input('user.firstName') >> sendKeys(username)
            input('gotoStep2') >> click()

            $$('label.error') + attrEquals('for', 'user.lastName') >> eval()
            $$('label.error') + attrEquals('for', 'user.username') >> eval()

            input('user.lastName') >> sendKeys(username)
            input('user.username') >> sendKeys('a')
            input('gotoStep2') >> click()
            $$('span') +
                textEquals('Username must be at least 5 characters long') +
                whereElemCount(1) +
                at(0) >> eval()

            input('user.username') >> sendKeys(username)
            input('gotoStep2') >> click()

            $('#pageContent') +
                $$('.sectionTitle') +
                at(0) +
                textEquals('Register') >> eval()

            $$('form') +
                attrEquals('action', '/webtests/bugzooky/Register.action') +
                at(0) +
                $$('p') +
                at(0) +
                textEquals("Welcome $username, please pick a password:")

            input('user.password') >> sendKeys(username)

            input('confirmPassword') >> sendKeys('bazbazbaz')

            input('register') >> click()

            $$('form') +
                attrEquals('action', '/webtests/bugzooky/Register.action') +
                at(0) +
                $$('ol')[0] +
                $$('li')[0] + textEquals('The Passwords entered did not match')

            input('user.password') >> clear()
            input('user.password') >> sendKeys(username)
            input('confirmPassword') >> clear()
            input('confirmPassword') >> sendKeys(username)
            input('register') >> click()

            $$('ul.messages')[0] +
                $$('li') +
                textStartsWith("Thank you for registering, ${username}") +
                whereElemCount(1) +
                at(0) >> eval()
            return username
        }
    }

    private Select select(String name) {
        new Select(findr().elemList(By.tagName('select')).where(attrEquals('name', name)).at(0))
    }

    private Findr findTableRowMultiBug(int index) {
        findr().elemList(By.tagName('form'))
            .where(attrEndsWith('action', '/webtests/bugzooky/MultiBug.action'))
            .whereElemCount(1)
            .at(0)
            .elemList(By.cssSelector('table.display'))
            .at(0)
            .elemList(By.tagName('tr'))
            .at(index + 1)
    }

    @Test
    void testBugListSingleEdit() {
        register()
        use(WebDriverCategory, FindrCategory, ListFindrCategory, DollrCategory) {
            webDriver.get "$homeUrl/BugList.action"
            findTableRowMultiBug(0) + $$('td')[3] + textEquals('First ever bug in the system.') >> eval()
            findTableRowMultiBug(0) + $$('a') + textEquals('Edit') + at(0) >> click()

            select('bug.component').selectByVisibleText('Component 2')

            input('saveAndAgain') >> click()

            select('bug.component').selectByVisibleText('Component 3')
            select('bug.owner').selectByVisibleText('velma')
            select('bug.priority').selectByVisibleText('Blocker')
            def textAreaLongDesc = textarea('bug.longDescription')
            textAreaLongDesc >> clear()
            textAreaLongDesc >> sendKeys('This is a long decription for this new bug blah blah blah...')
            input('bug.shortDescription') >> clear()
            input('save') >> click()

            $$('#pageContent')[0] +
                $$('ol')[0] +
                $$('li') +
                textEquals('Short Description is a required field') +
                whereElemCount(1) +
                at(0) >> eval()

            input('bug.shortDescription') >> sendKeys('brand new bug')
            input('save').click()

            findTableRowMultiBug(0) + $$('td')[3] + textEquals('brand new bug')
        }
    }

    private Findr textarea(String name) {
        findr().elemList(By.tagName('textarea')).where(attrEquals('name', name)).at(0)
    }

    private Findr findTd(int row, int col) {
        findTableRowMultiBug(row).elemList(By.tagName('td')).at(col)
    }


    @Test
    void testBulkAdd() {
        register()
        use(WebDriverCategory, FindrCategory, ListFindrCategory, DollrCategory) {
            $('#navLinks') + $$('a') + textEquals('Bulk Add') + at(0) >> click()
            select('bugs[0].component').selectByVisibleText('Component 1')
            select('bugs[1].component').selectByVisibleText('Component 2')
            select('bugs[0].owner').selectByVisibleText('shaggy')
            select('bugs[1].owner').selectByVisibleText('scrappy')
            select('bugs[0].priority').selectByVisibleText('Critical')
            select('bugs[1].priority').selectByVisibleText('Blocker')
            textarea('bugs[0].shortDescription').sendKeys('Short desc 1')
            textarea('bugs[0].longDescription').sendKeys('This is looooooooooooooooooooooooooooooooooooooooong !')
            textarea('bugs[1].longDescription').sendKeys('This is looooooooooooooooooooooooooooooooooooooooong too !')
            input('save') >> click()

            $$('form') +
                attrEndsWith('action', '/webtests/bugzooky/MultiBug.action') +
                at(0) +
                $$('ol')[0] +
                $('li') + textEquals('Short Description is a required field')

            textarea('bugs[1].shortDescription').sendKeys('Short desc 2')
            input('save') >> click()

            def assertTd = { int row, int col, String expectedText ->
                findTd(row, col) + textEquals(expectedText) >> eval()
            }

            assertTd 5, 3, 'Short desc 1'
            assertTd 5, 4, 'Component 1'
            assertTd 5, 5, 'Critical'
            assertTd 5, 7, 'shaggy'

            assertTd 6, 3, 'Short desc 2'
            assertTd 6, 4, 'Component 2'
            assertTd 6, 5, 'Blocker'
            assertTd 6, 7, 'scrappy'
        }

    }

    @Test
    void testBugListBulkEdit() {
        register()
        webDriver.get "$homeUrl/BugList.action"

        // check the two first bugs and bulk edit
        def clickCheckbox = { int row ->
            findTd(row, 0).elem(By.tagName('input')).where(attrEquals('type', 'checkbox')).click()
        }

        clickCheckbox(0)
        clickCheckbox(1)
        input('view').click()

        textarea('bugs[0].shortDescription').where(textEquals('First ever bug in the system.')).eval()
        textarea('bugs[1].shortDescription').where(textEquals('Another bug!  Oh no!.')).eval()
        // don't test more : it's already done in testBulkAdd
    }


    @Test
    void testAdminister() {
        register()
        webDriver.get "$homeUrl/AdministerComponents.action"
        use(WebDriverCategory, FindrCategory, ListFindrCategory, DollrCategory) {
            input('people[0].email') >> clear()
            input('people[0].email') >> sendKeys('foo@bar.com')
            input('Save') >> click()

            input('people[0].email') + attrEquals('value', 'foo@bar.com') >> eval()

            $$('input') + attrEquals('name', 'deleteIds') + at(0) >> click()
            input('Save') >> click()

            input('people[0].email') + attrEquals('value', 'shaggy@mystery.machine.tv') >> eval()
        }
    }

    @Test
    void testLogin() {
        register()
        use(WebDriverCategory, FindrCategory, ListFindrCategory, DollrCategory) {
            $$('a') + textEquals('Logout') + at(0) >> click()
            $$('a') + textEquals('Bug List') + at(0) >> click()
            input('username').sendKeys(username())
            input('password').sendKeys(username())
            input('login') >> click()
        }
    }

}
