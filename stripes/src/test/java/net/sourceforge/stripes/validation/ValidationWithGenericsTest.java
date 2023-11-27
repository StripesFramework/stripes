package net.sourceforge.stripes.validation;

import net.sourceforge.stripes.FilterEnabledTestBase;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.mock.MockRoundtrip;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests some cases where generics have been known to mess up validation.
 *
 * @author Ben Gunter
 */
public class ValidationWithGenericsTest extends FilterEnabledTestBase {

  public static class User {
    private String username, password;

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }
  }

  public static class AdminUser extends User {}

  public static class SuperUser extends AdminUser {}

  public static class BaseActionBean<T> implements ActionBean {
    private ActionBeanContext context;
    private T model;

    public T getModel() {
      return model;
    }

    public void setModel(T model) {
      this.model = model;
    }

    public ActionBeanContext getContext() {
      return context;
    }

    public void setContext(ActionBeanContext context) {
      this.context = context;
    }
  }

  public static class OverrideGetterAndSetterActionBean extends BaseActionBean<User> {
    @Override
    @ValidateNestedProperties({
      @Validate(field = "username", required = true),
      @Validate(field = "password", required = true)
    })
    public User getModel() {
      return super.getModel();
    }

    @Override
    public void setModel(User user) {
      super.setModel(user);
    }

    public Resolution login() {
      return null;
    }
  }

  public static class OverrideGetterActionBean extends BaseActionBean<User> {
    @Override
    @ValidateNestedProperties({
      @Validate(field = "username", required = true),
      @Validate(field = "password", required = true)
    })
    public User getModel() {
      return super.getModel();
    }

    public Resolution login() {
      return null;
    }
  }

  public static class OverrideSetterActionBean extends BaseActionBean<User> {
    @Override
    @ValidateNestedProperties({
      @Validate(field = "username", required = true),
      @Validate(field = "password", required = true)
    })
    public void setModel(User user) {
      super.setModel(user);
    }

    public Resolution login() {
      return null;
    }
  }

  /**
   * Attempts to trigger validation errors on an ActionBean declared with a type parameter.
   * Validation was crippled by a bug in JDK6 and earlier.
   *
   * @see <a href="http://www.stripesframework.org/jira/browse/STS-664">...</a>
   */
  @Test
  public void testActionBeanWithTypeParameter() throws Exception {
    runValidationTests(OverrideGetterAndSetterActionBean.class);
    runValidationTests(OverrideGetterActionBean.class);
    runValidationTests(OverrideSetterActionBean.class);
  }

  protected void runValidationTests(Class<? extends BaseActionBean<User>> type) throws Exception {
    // Trigger the validation errors
    MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), type);
    trip.execute("login");
    ValidationErrors errors = trip.getValidationErrors();
    Assert.assertNotNull("Expected validation errors but got none", errors);
    Assert.assertFalse("Expected validation errors but got none", errors.isEmpty());
    Assert.assertEquals(
        "Expected two validation errors but got " + errors.size(), errors.size(), 2);

    // Now add the required parameters and make sure the validation errors don't happen
    trip.addParameter("model.username", "Scooby");
    trip.addParameter("model.password", "Shaggy");
    trip.execute("login");
    errors = trip.getValidationErrors();
    Assert.assertTrue("Got unexpected validation errors", errors == null || errors.isEmpty());

    BaseActionBean<User> bean = trip.getActionBean(type);
    Assert.assertNotNull(bean);
    Assert.assertNotNull(bean.getModel());
    Assert.assertEquals(bean.getModel().getUsername(), "Scooby");
    Assert.assertEquals(bean.getModel().getPassword(), "Shaggy");
  }

  public static void main(String[] args) throws Exception {
    new ValidationWithGenericsTest().testActionBeanWithTypeParameter();
  }
}
