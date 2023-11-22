package net.sourceforge.stripes.util.bean;

import org.junit.Assert;
import org.junit.Test;

/** */
public class PropertyExpressionTests {
  @Test
  public void testDotNotation() {
    PropertyExpression expr = PropertyExpression.getExpression("foo.bar.splat");
    Node root = expr.getRootNode();
    Assert.assertEquals(root.getStringValue(), "foo");
    Assert.assertEquals(root.getNext().getStringValue(), "bar");
    Assert.assertEquals(root.getNext().getNext().getStringValue(), "splat");
  }

  @Test
  public void testDotNotationWithEscapes() {
    PropertyExpression expr = PropertyExpression.getExpression("fo\\\"o\\\".bar.splat");
    Node root = expr.getRootNode();
    Assert.assertEquals(root.getStringValue(), "fo\"o\"");
    Assert.assertEquals(root.getNext().getStringValue(), "bar");
    Assert.assertEquals(root.getNext().getNext().getStringValue(), "splat");
  }

  @Test
  public void testSquareBracketNotation() {
    PropertyExpression expr = PropertyExpression.getExpression("foo[index].bar");
    Node root = expr.getRootNode();
    Assert.assertEquals(root.getStringValue(), "foo");
    Assert.assertEquals(root.getNext().getStringValue(), "index");
    Assert.assertEquals(root.getNext().getNext().getStringValue(), "bar");
  }

  @Test
  public void testSquareBracketNotation2() {
    PropertyExpression expr = PropertyExpression.getExpression("foo[index][bar]");
    Node root = expr.getRootNode();
    Assert.assertEquals(root.getStringValue(), "foo");
    Assert.assertEquals(root.getNext().getStringValue(), "index");
    Assert.assertEquals(root.getNext().getNext().getStringValue(), "bar");
  }

  @Test
  public void testSquareBracketWithSingleQuoteNotation() {
    PropertyExpression expr = PropertyExpression.getExpression("foo['index'].bar");
    Node root = expr.getRootNode();
    Assert.assertEquals(root.getStringValue(), "foo");
    Assert.assertEquals(root.getNext().getStringValue(), "index");
    Assert.assertEquals(root.getNext().getNext().getStringValue(), "bar");
  }

  @Test
  public void testSquareBracketWithDoubleQuoteNotation() {
    PropertyExpression expr = PropertyExpression.getExpression("foo[\"index\"].bar");
    Node root = expr.getRootNode();
    Assert.assertEquals(root.getStringValue(), "foo");
    Assert.assertEquals(root.getNext().getStringValue(), "index");
    Assert.assertEquals(root.getNext().getNext().getStringValue(), "bar");
  }

  @Test(expected = ParseException.class)
  public void testBackToBackQuotedStrings() {
    @SuppressWarnings("unused")
    PropertyExpression expr = PropertyExpression.getExpression("foo['bar''splat']");
  }

  @Test
  public void testIntIndex() {
    PropertyExpression expr = PropertyExpression.getExpression("foo[123]");
    Node root = expr.getRootNode();
    Assert.assertEquals(root.getStringValue(), "foo");
    Assert.assertEquals(root.getNext().getStringValue(), "123");
    Assert.assertEquals(root.getNext().getTypedValue(), 123);
  }

  @Test
  public void testDoubleIndex() {
    PropertyExpression expr = PropertyExpression.getExpression("foo[123.4]");
    Node root = expr.getRootNode();
    Assert.assertEquals(root.getStringValue(), "foo");
    Assert.assertEquals(root.getNext().getStringValue(), "123.4");
    Assert.assertEquals(root.getNext().getTypedValue(), 123.4D);
  }

  @Test
  public void testLongIndex() {
    PropertyExpression expr = PropertyExpression.getExpression("foo[123l]");
    Node root = expr.getRootNode();
    Assert.assertEquals(root.getStringValue(), "foo");
    Assert.assertEquals(root.getNext().getStringValue(), "123l");
    Assert.assertEquals(root.getNext().getTypedValue(), 123L);
  }

  @Test
  public void testFloatIndex() {
    PropertyExpression expr = PropertyExpression.getExpression("foo[123F]");
    Node root = expr.getRootNode();
    Assert.assertEquals(root.getStringValue(), "foo");
    Assert.assertEquals(root.getNext().getStringValue(), "123F");
    Assert.assertEquals(root.getNext().getTypedValue(), 123F);
  }

  @Test
  public void testBooleanIndex() {
    PropertyExpression expr = PropertyExpression.getExpression("foo[false]");
    Node root = expr.getRootNode();
    Assert.assertEquals(root.getStringValue(), "foo");
    Assert.assertEquals(root.getNext().getStringValue(), "false");
    Assert.assertEquals(root.getNext().getTypedValue(), Boolean.FALSE);

    expr = PropertyExpression.getExpression("foo[tRue]");
    root = expr.getRootNode();
    Assert.assertEquals(root.getStringValue(), "foo");
    Assert.assertEquals(root.getNext().getStringValue(), "tRue");
    Assert.assertEquals(root.getNext().getTypedValue(), Boolean.TRUE);
  }
}
