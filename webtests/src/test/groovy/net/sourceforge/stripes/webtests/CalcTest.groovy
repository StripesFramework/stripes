package net.sourceforge.stripes.webtests

import org.junit.Test
import com.pojosontheweb.selenium.ManagedDriverJunit4TestBase

class CalcTest extends ManagedDriverJunit4TestBase {


    def helpers = {
        [
            new CalcHelper(findr(), false),
            new CalcHelper(findr(), true)
        ]
    }

    @Test
    void add() {
        helpers().each { CalcHelper h ->
            h.openPage()
            h.numberOne().sendKeys('2')
            h.numberTwo().sendKeys('2')
            h.buttonAddition().click()
            h.assertResult('4.0')
        }
    }

    @Test
    void divide() {
        helpers().each { CalcHelper h ->
            h.openPage()
            h.numberOne().sendKeys('2')
            h.numberTwo().sendKeys('2')
            h.buttonDivision().click()
            h.assertResult('1.0')
        }
    }

    @Test
    void validationErrors() {
        helpers().each { CalcHelper h ->
            h.openPage()
            h.buttonDivision().click()
            h.assertValidationErrors(
                'Number One is a required field',
                'Number Two is a required field'
            )

            h.numberOne().sendKeys('abc')
            h.buttonDivision().click()
            h.assertValidationErrors(
                'The value (abc) entered in field Number One must be a valid number',
                'Number Two is a required field'
            )

            h.numberOne().clear()
            h.numberOne().sendKeys('2')
            h.buttonDivision().click()
            h.assertValidationErrors('Number Two is a required field')

            h.numberOne().clear()
            h.numberOne().sendKeys('2')
            h.numberTwo().sendKeys('0')
            h.buttonDivision().click()
            h.assertValidationErrors('Dividing by zero is not allowed.')
        }
    }

}
