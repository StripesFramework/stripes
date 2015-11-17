package net.sourceforge.stripes.webtests

import com.google.common.base.Function
import com.pojosontheweb.selenium.Findr
import org.openqa.selenium.WebElement

import static com.pojosontheweb.selenium.Findrs.attrEquals
import static com.pojosontheweb.selenium.Findrs.textEquals
import static org.openqa.selenium.By.*

class CalcHelper {

    private final Findr findr
    private final boolean ajax
    private final String homeUrl

    CalcHelper(Findr findr, boolean ajax) {
        this.findr = findr
        this.ajax = ajax
        this.homeUrl = ajax ? 'http://localhost:9999/webtests/ajax/index.jsp' : 'http://localhost:9999/webtests/quickstart/index.jsp'
    }

    void openPage() {
        findr.driver.get homeUrl
    }

    private Findr inputByName(String type, String name) {
        findr.elemList(tagName('input'))
            .where(attrEquals('type', type))
            .where(attrEquals('name', name))
            .whereElemCount(1)
            .at(0)
    }

    Findr numberOne() {
        inputByName('text', 'numberOne')
    }

    Findr numberTwo() {
        inputByName('text', 'numberTwo')
    }

    Findr buttonAddition() {
        inputByName('submit', ajax ? 'add' : 'addition')
    }

    Findr buttonDivision() {
        inputByName('submit', ajax ? 'divide' : 'division')
    }

    void assertResult(String expected) {
        findr.elemList(id('result'))
            .whereElemCount(1)
            .at(0)
            .where(textEquals(expected))
            .eval()
    }

    void assertValidationErrors(String... expectedErrors) {

        def func = new Function<List<WebElement>, Boolean>() {
            @Override
            Boolean apply(List<WebElement> divs) {
                for (int i = 0; i < expectedErrors.length; i++) {
                    String expected = expectedErrors[i]
                    String actual = divs[i].text
                    if (expected != actual) {
                        return false
                    }
                }
                return true
            }
        }

        if (ajax) {
            findr.elem(id('result'))
                .elemList(tagName('div'))
                .whereElemCount(expectedErrors.length)
                .eval(func)
        } else {
            findr.elem(cssSelector('div.errors'))
                .elem(tagName('ol'))
                .elemList(tagName('li'))
                .whereElemCount(expectedErrors.length)
                .eval(func)
        }
    }
}
