package net.sourceforge.stripes.webtests.bugzooky

import net.sourceforge.stripes.webtests.WebtestCaseFixed

class BugzookyTest extends WebtestCaseFixed {

  def homeUrl = 'http://localhost:9999/webtests/examples/bugzooky'

  static int count = 10 //System.currentTimeMillis()

  void testAuthenticationIsRequired() {
    webtest('testAuthenticationIsRequired') {
      [
        'BugList.action',
        'SingleBug.action',
        'MultiBug.action',
        'AdministerComponents.action'
      ].each { p ->
        invoke "$homeUrl/$p"
        verifyTitle 'Bugzooky - Login'
        verifyText 'Login'
      }
    }
  }

  private String register() {
    count++
    def username = "foobar$count"
    ant.invoke "$homeUrl/Register.action"

    ant.setInputField name:'user.firstName', value:username
    ant.clickButton name:'gotoStep2'

    ant.verifyText 'Last Name is a required field'
    ant.verifyText 'Username is a required field'
    ant.setInputField name:'user.lastName', value:username
    ant.setInputField name:'user.username', value:'a'
    ant.clickButton name:'gotoStep2'

    ant.verifyText 'Username must be at least 5 characters long'
    ant.setInputField name:'user.username', value:username
    ant.clickButton name:'gotoStep2'

    ant.verifyText "Welcome $username, please pick a password"
    ant.setInputField name:'user.password', value:username
    ant.setInputField name:'confirmPassword', value:'bazbazbaz'
    ant.clickButton name:'register'

    ant.verifyText 'The Passwords entered did not match'
    ant.setInputField name:'user.password', value:username
    ant.setInputField name:'confirmPassword', value:username
    ant.clickButton name:'register'

    ant.verifyText "Thank you for registering, ${username}. Your account has been created with username '${username}'."      
    return username
  }
  
  void testBugListSingleEdit() {
    webtest('testBugListSingleEdit') {
      register()

      // list bugs
      invoke "$homeUrl/BugList.action"

      verifyTitle 'Bugzooky - Bug List'
      verifyXPath xpath:'/html/body/div/div[2]/form/table/tbody/tr[2]/td[4]',
              text:'First ever bug in the system.'
      clickLink xpath:'/html/body/div/div[2]/form/table/tbody/tr[2]/td[9]/a'

      // bug edition
      setSelectField name:'bug.component', value:'2'
      clickButton name:'saveAndAgain'

      // fill in new bug
      setSelectField name:'bug.component', value:'3'
      setSelectField name:'bug.owner', value:'4'
      setSelectField name:'bug.priority', value:'Blocker'
      setInputField name:'bug.longDescription',
              value:'This is a long decription for this new bug blah blah blah...'
      setInputField name:'bug.shortDescription', value:''
      clickButton name:'save'

      verifyXPath xpath:'/html/body/div/div[2]/ol/li',
              text:'Short Description is a required field'
      setInputField name:'bug.shortDescription', value:'brand new bug'
      clickButton name:'save'

      verifyXPath xpath:'/html/body/div/div[2]/form/table/tbody/tr[2]/td[4]',
              text:'brand new bug'
      verifyXPath xpath:'/html/body/div/div[2]/form/table/tbody/tr[2]/td[5]',
              text:'Component 3'
      verifyXPath xpath:'/html/body/div/div[2]/form/table/tbody/tr[2]/td[6]',
              text:'Blocker'
      verifyXPath xpath:'/html/body/div/div[2]/form/table/tbody/tr[2]/td[8]',
              text:'velma'
    }
  }

  void testBulkAdd() {
    webtest('testBulkAdd') {
      register()

      clickLink xpath:'/html/body/div/div/div/a[3]'

      verifyXPath xpath:'/html/body/div/div[2]/div',
              text:'Bulk Add/Edit Bugs'
      setSelectField name:'bugs[0].component', value:'1'
      setSelectField name:'bugs[1].component', value:'2'
      setSelectField name:'bugs[0].owner', value:'1'
      setSelectField name:'bugs[1].owner', value:'2'
      setSelectField name:'bugs[0].priority', value:'Critical'
      setSelectField name:'bugs[1].priority', value:'Blocker'
      setInputField name:'bugs[0].shortDescription', value:'Short desc 1'
      setInputField name:'bugs[0].longDescription',
              value:'This is looooooooooooooooooooooooooooooooooooooooong !'
      setInputField name:'bugs[1].longDescription',
              value:'This is looooooooooooooooooooooooooooooooooooooooong too !'
      clickButton name:'save'

      verifyText 'Short Description is a required field'
      setInputField name:'bugs[1].shortDescription',
              value:'Short desc 2'
      clickButton name:'save'

      verifyXPath xpath:'/html/body/div/div[2]/form/table/tbody/tr[7]/td[4]',
              text:'Short desc 1'
      verifyXPath xpath:'/html/body/div/div[2]/form/table/tbody/tr[7]/td[5]',
              text:'Component 1'
      verifyXPath xpath:'/html/body/div/div[2]/form/table/tbody/tr[7]/td[6]',
              text:'Critical'
      verifyXPath xpath:'/html/body/div/div[2]/form/table/tbody/tr[7]/td[8]',
              text:'shaggy'
      verifyXPath xpath:'/html/body/div/div[2]/form/table/tbody/tr[8]/td[4]',
              text:'Short desc 2'
      verifyXPath xpath:'/html/body/div/div[2]/form/table/tbody/tr[8]/td[5]',
              text:'Component 2'
      verifyXPath xpath:'/html/body/div/div[2]/form/table/tbody/tr[8]/td[6]',
              text:'Blocker'
      verifyXPath xpath:'/html/body/div/div[2]/form/table/tbody/tr[8]/td[8]',
              text:'scrappy'
    }
  }

  void testBugListBulkEdit() {
    webtest('testBugListBulkEdit') {
      register()

      // list bugs
      invoke "$homeUrl/BugList.action"

      // check the two first bugs and bulk edit
      setCheckbox xpath:'/html/body/div/div[2]/form/table/tbody/tr[2]/td/input'
      setCheckbox xpath:'/html/body/div/div[2]/form/table/tbody/tr[3]/td/input'
      clickButton xpath:'/html/body/div/div[2]/form/div/input'

      verifyXPath xpath:'/html/body/div/div[2]/form/table/tbody/tr[2]/td[5]/textarea',
              text:'brand new bug',
              regex:true
      verifyXPath xpath: '/html/body/div/div[2]/form/table/tbody/tr[3]/td[5]/textarea',
              text:'Another bug!  Oh no!.',
              regex:true
      // don't test more : it's already done in testBulkAdd
    }
  }

  void testAdminister() {
    webtest('testAdminister') {
      register()

      invoke "$homeUrl/AdministerComponents.action"

      // change email for first user
      setInputField xpath:'/html/body/div/div[2]/form/table/tbody/tr[2]/td[6]/input',
              value:'foo@bar.com'
      clickButton xpath:'/html/body/div/div[2]/form/div/input'

      // check email change, remove first user
      verifyXPath xpath:"/html/body/div/div[2]/form/table/tbody/tr[2]/td[6]/input[@value='foo@bar.com']"
      setCheckbox xpath:'/html/body/div/div[2]/form/table/tbody/tr[2]/td/input'
      clickButton xpath:'/html/body/div/div[2]/form/div/input'

      // check email is not foo@bar.com for the first user
      not {
        verifyXPath xpath:"/html/body/div/div[2]/form/table/tbody/tr[2]/td[6]/input[@value='foo@bar.com']"
      }

      // change the value of component 3
      setInputField xpath:'/html/body/div/div[2]/form[2]/table/tbody/tr[5]/td[3]/input',
              value:'Component X'
      clickButton xpath:'/html/body/div/div[2]/form[2]/div/input'

      // verify component name change, and create new component
      verifyXPath xpath:"/html/body/div/div[2]/form[2]/table/tbody/tr[5]/td[3]/input[@value='Component X']"
      setInputField xpath:'/html/body/div/div[2]/form[2]/table/tbody/tr[7]/td[3]/input',
              value:'My Component'
      clickButton xpath:'/html/body/div/div[2]/form[2]/div/input'

      // assert component creation, and remove component
      verifyXPath xpath:"/html/body/div/div[2]/form[2]/table/tbody/tr[7]/td[3]/input[@value='My Component']"
      setCheckbox xpath:'/html/body/div/div[2]/form[2]/table/tbody/tr[7]/td/input'
      clickButton xpath:'/html/body/div/div[2]/form[2]/div/input'

      // assert component removal
      not {
        verifyXPath xpath:"/html/body/div/div[2]/form[2]/table/tbody/tr[7]/td[3]/input[@value='My Component']"
      }
    }
  }

  

  

}
