package net.sourceforge.stripes.util.bean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.jupiter.api.Test;


public class PropertyExpressionTests {

   @Test
   public void testBackToBackQuotedStrings() {
      Throwable throwable = catchThrowable(() -> PropertyExpression.getExpression("foo['bar''splat']"));
      assertThat(throwable).isInstanceOf(ParseException.class);
   }

   @Test
   public void testBooleanIndex() {
      PropertyExpression expr = PropertyExpression.getExpression("foo[false]");
      Node root = expr.getRootNode();
      assertThat(root.getStringValue()).isEqualTo("foo");
      assertThat(root.getNext().getStringValue()).isEqualTo("false");
      assertThat(root.getNext().getTypedValue()).isEqualTo(Boolean.FALSE);

      expr = PropertyExpression.getExpression("foo[tRue]");
      root = expr.getRootNode();
      assertThat(root.getStringValue()).isEqualTo("foo");
      assertThat(root.getNext().getStringValue()).isEqualTo("tRue");
      assertThat(root.getNext().getTypedValue()).isEqualTo(Boolean.TRUE);
   }

   @Test
   public void testDotNotation() {
      PropertyExpression expr = PropertyExpression.getExpression("foo.bar.splat");
      Node root = expr.getRootNode();
      assertThat(root.getStringValue()).isEqualTo("foo");
      assertThat(root.getNext().getStringValue()).isEqualTo("bar");
      assertThat(root.getNext().getNext().getStringValue()).isEqualTo("splat");
   }

   @Test
   public void testDotNotationWithEscapes() {
      PropertyExpression expr = PropertyExpression.getExpression("fo\\\"o\\\".bar.splat");
      Node root = expr.getRootNode();
      assertThat(root.getStringValue()).isEqualTo("fo\"o\"");
      assertThat(root.getNext().getStringValue()).isEqualTo("bar");
      assertThat(root.getNext().getNext().getStringValue()).isEqualTo("splat");
   }

   @Test
   public void testDoubleIndex() {
      PropertyExpression expr = PropertyExpression.getExpression("foo[123.4]");
      Node root = expr.getRootNode();
      assertThat(root.getStringValue()).isEqualTo("foo");
      assertThat(root.getNext().getStringValue()).isEqualTo("123.4");
      assertThat(root.getNext().getTypedValue()).isEqualTo(123.4);
   }

   @Test
   public void testFloatIndex() {
      PropertyExpression expr = PropertyExpression.getExpression("foo[123F]");
      Node root = expr.getRootNode();
      assertThat(root.getStringValue()).isEqualTo("foo");
      assertThat(root.getNext().getStringValue()).isEqualTo("123F");
      assertThat(root.getNext().getTypedValue()).isEqualTo(123f);
   }

   @Test
   public void testIntIndex() {
      PropertyExpression expr = PropertyExpression.getExpression("foo[123]");
      Node root = expr.getRootNode();
      assertThat(root.getStringValue()).isEqualTo("foo");
      assertThat(root.getNext().getStringValue()).isEqualTo("123");
      assertThat(root.getNext().getTypedValue()).isEqualTo(123);
   }

   @Test
   public void testLongIndex() {
      PropertyExpression expr = PropertyExpression.getExpression("foo[123l]");
      Node root = expr.getRootNode();
      assertThat(root.getStringValue()).isEqualTo("foo");
      assertThat(root.getNext().getStringValue()).isEqualTo("123l");
      assertThat(root.getNext().getTypedValue()).isEqualTo(123L);
   }

   @Test
   public void testSquareBracketNotation() {
      PropertyExpression expr = PropertyExpression.getExpression("foo[index].bar");
      Node root = expr.getRootNode();
      assertThat(root.getStringValue()).isEqualTo("foo");
      assertThat(root.getNext().getStringValue()).isEqualTo("index");
      assertThat(root.getNext().getNext().getStringValue()).isEqualTo("bar");
   }

   @Test
   public void testSquareBracketNotation2() {
      PropertyExpression expr = PropertyExpression.getExpression("foo[index][bar]");
      Node root = expr.getRootNode();
      assertThat(root.getStringValue()).isEqualTo("foo");
      assertThat(root.getNext().getStringValue()).isEqualTo("index");
      assertThat(root.getNext().getNext().getStringValue()).isEqualTo("bar");
   }

   @Test
   public void testSquareBracketWithDoubleQuoteNotation() {
      PropertyExpression expr = PropertyExpression.getExpression("foo[\"index\"].bar");
      Node root = expr.getRootNode();
      assertThat(root.getStringValue()).isEqualTo("foo");
      assertThat(root.getNext().getStringValue()).isEqualTo("index");
      assertThat(root.getNext().getNext().getStringValue()).isEqualTo("bar");
   }

   @Test
   public void testSquareBracketWithSingleQuoteNotation() {
      PropertyExpression expr = PropertyExpression.getExpression("foo['index'].bar");
      Node root = expr.getRootNode();
      assertThat(root.getStringValue()).isEqualTo("foo");
      assertThat(root.getNext().getStringValue()).isEqualTo("index");
      assertThat(root.getNext().getNext().getStringValue()).isEqualTo("bar");
   }
}
