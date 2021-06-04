package org.stripesframework.web.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.stripesframework.web.FilterEnabledTestBase;
import org.stripesframework.web.action.ActionBean;
import org.stripesframework.web.action.ActionBeanContext;
import org.stripesframework.web.action.Resolution;
import org.stripesframework.web.mock.MockRoundtrip;


/**
 * Tests some cases where generics have been known to mess up validation.
 *
 * @author Ben Gunter
 */
public class ValidationWithGenericsTest extends FilterEnabledTestBase {

   /**
    * Attempts to trigger validation errors on an ActionBean declared with a type parameter.
    * Validation was crippled by a bug in JDK6 and earlier.
    *
    * @see http://www.stripesframework.org/jira/browse/STS-664
    */
   @Test
   public void testActionBeanWithTypeParameter() throws Exception {
      runValidationTests(OverrideGetterAndSetterActionBean.class);
      runValidationTests(OverrideGetterActionBean.class);
      runValidationTests(OverrideSetterActionBean.class);
      runValidationTests(OverloadSetterActionBean.class);
      runValidationTests(ExtendOverloadSetterActionBean.class);
      runValidationTests(ExtendOverloadSetterAgainActionBean.class);
   }

   protected void runValidationTests( Class<? extends BaseActionBean<User>> type ) throws Exception {
      // Trigger the validation errors
      MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), type);
      trip.execute("login");
      ValidationErrors errors = trip.getValidationErrors();
      assertThat(errors).isNotNull();
      assertThat(errors).hasSize(2);

      // Now add the required parameters and make sure the validation errors don't happen
      trip.addParameter("model.username", "Scooby");
      trip.addParameter("model.password", "Shaggy");
      trip.execute("login");
      errors = trip.getValidationErrors();
      assertThat(errors).isEmpty();

      BaseActionBean<User> bean = trip.getActionBean(type);
      assertThat(bean).isNotNull();
      assertThat(bean.getModel()).isNotNull();
      assertThat(bean.getModel().getUsername()).isEqualTo("Scooby");
      assertThat(bean.getModel().getPassword()).isEqualTo("Shaggy");
   }

   public static class AdminUser extends User {}


   public static class BaseActionBean<T> implements ActionBean {

      private ActionBeanContext context;
      private T                 model;

      @Override
      public ActionBeanContext getContext() { return context; }

      public T getModel() { return model; }

      @Override
      public void setContext( ActionBeanContext context ) { this.context = context; }

      public void setModel( T model ) { this.model = model; }
   }


   public static class ExtendOverloadSetterActionBean extends OverloadSetterActionBean {}


   public static class ExtendOverloadSetterAgainActionBean extends ExtendOverloadSetterActionBean {

      @Override
      @ValidateNestedProperties({ @Validate(field = "username", required = true), @Validate(field = "password", required = true) })
      public void setModel( User user ) { super.setModel(user); }
   }


   @SuppressWarnings("unused")
   public static class OverloadSetterActionBean extends BaseActionBean<User> {

      @Override
      public User getModel() { return super.getModel(); }

      public Resolution login() { return null; }

      public void setModel( AdminUser user ) {}

      public void setModel( SuperUser user ) {}

      public void setModel( String string ) {}

      public void setModel( Integer integer ) {}

      @Override
      @ValidateNestedProperties({ @Validate(field = "username", required = true), @Validate(field = "password", required = true) })
      public void setModel( User user ) { super.setModel(user); }
   }


   @SuppressWarnings("unused")
   public static class OverrideGetterActionBean extends BaseActionBean<User> {

      @Override
      @ValidateNestedProperties({ @Validate(field = "username", required = true), @Validate(field = "password", required = true) })
      public User getModel() { return super.getModel(); }

      public Resolution login() { return null; }
   }


   @SuppressWarnings("unused")
   public static class OverrideGetterAndSetterActionBean extends BaseActionBean<User> {

      @Override
      @ValidateNestedProperties({ @Validate(field = "username", required = true), @Validate(field = "password", required = true) })
      public User getModel() { return super.getModel(); }

      public Resolution login() { return null; }

      @Override
      public void setModel( User user ) { super.setModel(user); }
   }


   @SuppressWarnings("unused")
   public static class OverrideSetterActionBean extends BaseActionBean<User> {

      public Resolution login() { return null; }

      @Override
      @ValidateNestedProperties({ @Validate(field = "username", required = true), @Validate(field = "password", required = true) })
      public void setModel( User user ) { super.setModel(user); }
   }


   public static class SuperUser extends AdminUser {}


   public static class User {

      private String username, password;

      public String getPassword() { return password; }

      public String getUsername() { return username; }

      public void setPassword( String password ) { this.password = password; }

      public void setUsername( String username ) { this.username = username; }
   }
}
