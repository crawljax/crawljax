package com.crawljax.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import javax.xml.xpath.XPathExpressionException;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Test class for the XPathHelper class.
 */
public class XPathHelperTest {

    /**
     * Check if XPath building works correctly.
     *
     * @throws IOException
     */
    @Test
    public void testGetXpathExpression() throws IOException {
        String html =
                "<body><div id='firstdiv'></div><div><span id='thespan'>" + "<a id='thea'>test</a></span></div></body>";

        Document dom = DomUtils.asDocument(html);
        assertNotNull(dom);

        // first div
        String expectedXpath = "//DIV[@id = 'firstdiv']";
        String xpathExpr = XPathHelper.getXPathExpression_other(dom.getElementById("firstdiv"));
        assertEquals(expectedXpath, xpathExpr);

        // span
        expectedXpath = "//SPAN[@id = 'thespan']";
        xpathExpr = XPathHelper.getXPathExpression_other(dom.getElementById("thespan"));
        assertEquals(expectedXpath, xpathExpr);

        // a
        expectedXpath = "//A[@id = 'thea']";
        xpathExpr = XPathHelper.getXPathExpression_other(dom.getElementById("thea"));
        assertEquals(expectedXpath, xpathExpr);

        // test anchoring to parent id
        html = "<body><div id='firstdiv'><span><div></div></span></div>"
                + "<div><span id='thespan'><div></div></span><span></span></div></body>";

        dom = DomUtils.asDocument(html);

        expectedXpath = "//DIV[@id = 'firstdiv']/SPAN[1]/DIV[1]";
        xpathExpr = XPathHelper.getXPathExpression_other(
                dom.getElementById("firstdiv").getFirstChild().getFirstChild());
        assertEquals(expectedXpath, xpathExpr);

        expectedXpath = "//SPAN[@id = 'thespan']/DIV[1]";
        xpathExpr = XPathHelper.getXPathExpression_other(
                dom.getElementById("thespan").getFirstChild());
        assertEquals(expectedXpath, xpathExpr);

        // un-anchored: xpath should go to root
        expectedXpath = "/HTML[1]/BODY[1]/DIV[2]/SPAN[2]";
        xpathExpr = XPathHelper.getXPathExpression_other(
                dom.getFirstChild().getLastChild().getLastChild().getLastChild());
        assertEquals(expectedXpath, xpathExpr);

        expectedXpath = "/HTML[1]/BODY[1]/DIV[2]/SPAN[2]";
        xpathExpr = XPathHelper.getSkeletonXpath(
                dom.getFirstChild().getLastChild().getLastChild().getLastChild());
        assertEquals(expectedXpath, xpathExpr);

        expectedXpath = "SPAN[2]";
        xpathExpr = XPathHelper.getXPathFromSpecificParent(
                dom.getFirstChild().getLastChild().getLastChild().getLastChild(),
                dom.getFirstChild().getLastChild().getLastChild());
        assertEquals(expectedXpath, xpathExpr);
    }

    @Test
    public void whenWildcardsUsedXpathShouldFindTheElements() throws Exception {
        String html = "<body>" + "<DIV><P>Bla</P><P>Bla2</P></DIV>" + "<DIV id='exclude'><P>Ex</P><P>Ex2</P></DIV>"
                + "</body>";
        String xpathAllP = "//DIV//P";
        String xpathOnlyExcludedP = "//DIV[@id='exclude']//P";
        NodeList nodes = XPathHelper.evaluateXpathExpression(html, xpathAllP);
        assertThat(nodes.getLength(), is(4));

        nodes = XPathHelper.evaluateXpathExpression(html, xpathOnlyExcludedP);
        assertThat(nodes.getLength(), is(2));
    }

    @Test
    public void testXPathLocation() {
        String html = "<HTML><LINK foo=\"bar\">woei</HTML>";
        String xpath = "/HTML[1]/LINK[1]";
        int start = XPathHelper.getXPathLocation(html, xpath);
        int end = XPathHelper.getCloseElementLocation(html, xpath);

        assertEquals(6, start);
        assertEquals(22, end);
    }

    @Test
    public void formatXPath() {
        assertThat(XPathHelper.formatXPath("//ul/a"), is("//UL/A"));
        assertThat(XPathHelper.formatXPath("/div//span"), is("/DIV//SPAN"));
        assertThat(XPathHelper.formatXPath("//ul[@CLASS=\"Test\"]"), is("//UL[@class=\"Test\"]"));
        assertThat(XPathHelper.formatXPath("//ul[@CLASS=\"Test\"]/a"), is("//UL[@class=\"Test\"]/A"));
    }

    @Test
    public void formatXpathWithDoubleSlashes() {
        String xpath = "//div[@id='dontClick']//a";
        assertThat(XPathHelper.formatXPath(xpath), is("//DIV[@id='dontClick']//A"));
    }

    @Test
    public void formatXPathAxes() {
        String xPath = "//ancestor-or-self::div[@CLASS,'foo']";
        assertEquals("//ancestor-or-self::DIV[@class,'foo']", XPathHelper.formatXPath(xPath));
    }

    @Test
    public void getLastElementOfXPath() {
        String xPath = "/HTML/BODY/DIV/UL/LI[@class=\"Test\"]";
        assertEquals("LI", XPathHelper.getLastElementXPath(xPath));
    }

    @Test
    public void stripXPathToElement() {
        String xPath = "/HTML/BODY/DIV/UL/LI[@class=\"Test\"]";
        assertEquals("/HTML/BODY/DIV/UL/LI", XPathHelper.stripXPathToElement(xPath));
    }

    @Test
    public void useTextSelector() {
        String xPath = "//A[text()='add new']";
        String html =
                "<HTML xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\"><HEAD><META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n"
                        + "	<META content=\"text/html; charset=UTF-8\" http-equiv=\"Content-Type\">\n"
                        + "	<META content=\"PHP-Addressbook\" name=\"Description\">\n"
                        + "	<META content=\"\" name=\"Keywords\">\n"
                        + "\n"
                        + "	<STYLE type=\"text/css\">\n"
                        + "		\n"
                        + "    body {background-image:url('./skins/header-blue.jpg');background-repeat:repeat-x;background-position:top left;}\n"
                        + "    table#maintable th {text-align:center;border:1px solid #ccc;font-size:12px;background:#739fce;color:#fff;}\n"
                        + "    table#birthdays th {color:#fff;background:#739fce;margin:25px;border:1px solid #ccc;}\n"
                        + "		table#maintable th a {color:#fff;}\n"
                        + "body,#footer,ul {margin:0;padding:0;}\n"
                        + "body,p,td,h1,h2,a,a:hover {font-family:Arial,Helvetica,sans-serif;font-size:12px}\n"
                        + "h1 {font-size:18px}\n"
                        + "h2 {font-size:14px}\n"
                        + "a {color:#036}\n"
                        + "a:hover {color:#06C;text-decoration:none}\n"
                        + "img {border:0;}\n"
                        + "textarea {font-family:Arial,Helvetica,sans-serif;font-size:10pt}\n"
                        + "\n"
                        + "#container {margin:0 auto;width:780px;border:0}\n"
                        + "#top{color:#fff;margin:5px 0 0;height:20px;text-align:right;}\n"
                        + "#header {height:80px;}\n"
                        + "#header h1 {display:none;}\n"
                        + "\n"
                        + "#nav {margin:0 0 20px;height:25px;width:770px;float:left;border:0;display:inline;}\n"
                        + "#nav ul li a {color:#fff;padding:0 4px;}\n"
                        + "#nav ul li img {display:none;}\n"
                        + "\n"
                        + "#content {margin:20px 0 0;width:780px;}\n"
                        + "#footer {margin:45px 0 0;padding:20px 0;clear:both;}\n"
                        + "\n"
                        + "ul {list-style:none;}\n"
                        + "ul li {display:inline;}\n"
                        + "#footer ul li {display:block;}\n"
                        + "\n"
                        + "label {margin-right:0.5em;width:10em;float:left;text-align:left;display:block;}\n"
                        + "\n"
                        + "#search-az {text-align:center;padding:2px;}\n"
                        + "#a-z a {font-size:75%;} \n"
                        + "\n"
                        + ".odd {background:#e5e5e5;}\n"
                        + ".even {background:#f3f3f3;}\n"
                        + "\n"
                        + "#right,.right {float:right;}\n"
                        + "#left,.left {float:left;}\n"
                        + ".clear {clear:both;}\n"
                        + "\n"
                        + ".msgbox {padding:16px;border:1px solid #ccc;background:#fff4b4;width:60%;font-weight:700;}\n"
                        + ".msgbox i {font-weight:400;}\n"
                        + "\n"
                        + "table {width:100%;border:1px solid #ccc;border-collapse:collapse;}\n"
                        + "table tr td {border:1px solid #ccc;padding:2px 1px}\n"
                        + "table img,.center {text-align:center;}\n"
                        + "table th {text-align:left;font-size:14px;padding:8px 4px;}\n"
                        + "\n"
                        + "table#birthdays {border:0;}\n"
                        + ".tablespace td {border:0;}\n"
                        + "\n"
                        + "/* View.php */\n"
                        + "table#view,table#view td {border:1px solid #000;border-collapse:collapse;}\n"
                        + "table#view td {padding:5px;}\n"
                        + "\n"
                        + "/* Edit.php */\n"
                        + "#content input[type=text],#content textarea {width:220px; margin-bottom:3px;}\n"
                        + "#content input[type=text] {height:1,1em}\n"
                        + "// #content textarea {height:8em}\n"
                        + "input.byear{width:4em !important;}\n"
                        + "\n"
                        + "/* Source Forge */\n"
                        + "#download {margin:0;width:180px;background:#63A624;color:#fff;border:1px solid #000;text-align:center;}\n"
                        + "#download a,#top a {color:#fff;}\n"
                        + "\n"
                        + "/* Login */\n"
                        + "#content input[name=user],input[name=pass] {width:150px; margin-bottom:3px;}	</STYLE>\n"
                        + "	<!--[if !IE]>-->\n"
                        + "	<LINK href=\"iphone.css\" media=\"only screen and (max-device-width: 480px)\" rel=\"stylesheet\" type=\"text/css\">\n"
                        + "	<!--<![endif]-->\n"
                        + "	<META content=\"width=320; initial-scale=1.0; maximum-scale=1.0; user-scalable=0;\" name=\"viewport\">\n"
                        + "\n"
                        + "\n"
                        + "	<LINK href=\"icons/font.png\" rel=\"icon\" type=\"image/png\">\n"
                        + "	<TITLE>Address book</TITLE>  	</HEAD><BODY>\n"
                        + "  		<DIV id=\"container\">\n"
                        + "  			<DIV id=\"top\"></DIV>\n"
                        + "        <DIV id=\"header\"><A href=\".\"><IMG alt=\"address book\" src=\"title.png\" title=\"address book\"></A></DIV>\n"
                        + "  			<DIV id=\"nav\"></DIV>\n"
                        + "  			<DIV id=\"content\">\n"
                        + "  	      <FORM accept-charset=\"utf-8\" method=\"post\" name=\"LoginForm\">\n"
                        + "  	         <LABEL>User:</LABEL><INPUT name=\"user\" tabindex=\"0\"><BR>\n"
                        + "  	         <LABEL>Password:</LABEL><INPUT name=\"pass\" type=\"password\">\n"
                        + "  	         <BR>\n"
                        + "  	         <INPUT type=\"submit\" value=\"Login\">\n"
                        + "  	         <BR>\n"
                        + "  	         <BR>\n"
                        + "          </FORM>\n"
                        + "<!--  	         \n"
                        + "  	         <a href=\"../register\">Create account</a>\n"
                        + "  	         | <a href=\"../register\">Forgot password</a>  	         \n"
                        + "  	         <br><br>\n"
                        + "  	         <br>\n"
                        + "  	         <hr>\n"
                        + "  	         <br><br>\n"
                        + "\n"
                        + "  	      <form accept-charset=\"utf-8\" id=\"hLoginForm\" name=\"hLoginForm\" method=\"post\">\n"
                        + "  	         <input name=\"user\" type=\"hidden\"/>\n"
                        + "  	         <input name=\"pass\" type=\"hidden\"/>\n"
                        + "          </form>\n"
                        + "  	         \n"
                        + "  	         <a href=\"javascript:hLoginForm.user.value='Facebook';hLoginForm.submit();\"><img src=\"icons/facebook.png\"></a>\n"
                        + "  	         <a href=\"javascript:hLoginForm.user.value='Google';hLoginForm.submit();\"><img src=\"icons/google.png\"></a>\n"
                        + "  	         <a href=\"javascript:hLoginForm.user.value='Yahoo';hLoginForm.submit();\"><img src=\"icons/yahoo.png\"></a>\n"
                        + "  	         <a href=\"javascript:hLoginForm.user.value='Live';hLoginForm.submit();\"><img src=\"icons/microsoft.png\"></a>\n"
                        + "  	         <br><br><br>\n"
                        + "-->\n"
                        + "  \n"
                        + "        </DIV></DIV></BODY></HTML>";
        try {
            NodeList matches = XPathHelper.evaluateXpathExpression(html, xPath);
            System.out.println(matches.getLength());
        } catch (XPathExpressionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testXpathForClickables() throws XPathExpressionException, IOException {
        String html = "<html>" + "<body><div evlist=true></div><a></a></body>" + "</html>";

        NodeList nodes = XPathHelper.evaluateXpathExpression(html, "//*[@evlist]");
        System.out.println(nodes.getLength());
    }
}
