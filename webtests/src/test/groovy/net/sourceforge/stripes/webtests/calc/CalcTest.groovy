package net.sourceforge.stripes.webtests.calc

import net.sourceforge.stripes.webtests.WebtestCaseFixed

class CalcTest extends WebtestCaseFixed {

  def homeUrl = 'http://localhost:9999/webtests/quickstart/index.jsp'

  void testCalcHome() {
    webtest('testCalcHome') {
      invoke homeUrl
      verifyTitle 'My First Stripe'
      verifyText 'Stripes Calculator'
    }
  }

  void testAdd() {
    webtest('testAdd') {
      invoke homeUrl
      setInputField(name:'numberOne', value: '2')
      setInputField(name:'numberTwo', value: '2')
      clickButton(name:'addition')
      verifyText 'Result:'
      verifyText '4.0'      
    }
  }

  void testDivide() {
    webtest('testDivide') {
      invoke homeUrl
      setInputField(name:'numberOne', value: '2')
      setInputField(name:'numberTwo', value: '2')
      clickButton(name:'division')
      verifyText 'Result:'
      verifyText '1.0'      
    }
  }

  void testValidationErrors() {
    webtest('testValidationErrors') {
      invoke homeUrl
      clickButton(name:'division')
      verifyText 'Number One is a required field'
      verifyText 'Number Two is a required field'

      invoke homeUrl
      setInputField(name:'numberOne', value: 'abc')
      clickButton(name:'division')
      verifyText 'The value (abc) entered in field Number One must be a valid number'
      verifyText 'Number Two is a required field'

      invoke homeUrl
      setInputField(name:'numberOne', value: '2')
      clickButton(name:'division')
      verifyText 'Number Two is a required field'

      invoke homeUrl
      setInputField(name:'numberOne', value: '2')
      setInputField(name:'numberTwo', value: '0')
      clickButton(name:'division')
      verifyText 'Dividing by zero is not allowed.'
    }
  }


}
