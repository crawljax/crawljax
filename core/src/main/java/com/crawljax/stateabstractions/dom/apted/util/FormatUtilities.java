/* MIT License
 *
 * Copyright (c) 2017 Nikolaus Augsten
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.crawljax.stateabstractions.dom.apted.util;

import java.util.Date;
import java.util.LinkedList;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

/**
 * Various formatting utilities.
 *
 * @author Nikolaus Augsten
 *
 */
public class FormatUtilities
{

    public FormatUtilities()
    {
    }

    public static String getField(int fieldNr, String line, char seperator)
    {
        if(line != null)
        {
            int pos = 0;
            for(int i = 0; i < fieldNr; i++)
            {
                pos = line.indexOf(seperator, pos);
                if(pos == -1)
                    return null;
                pos++;
            }

            int pos2 = line.indexOf(seperator, pos);
            String res;
            if(pos2 == -1)
                res = line.substring(pos);
            else
                res = line.substring(pos, pos2);
            return res.trim();
        } else
        {
            return null;
        }
    }

    public static String[] getFields(String line, char separator)
    {
        if(line != null && !line.equals(""))
        {
            StringBuffer field = new StringBuffer();
            LinkedList fieldArr = new LinkedList();
            for(int i = 0; i < line.length(); i++)
            {
                char ch = line.charAt(i);
                if(ch == separator)
                {
                    fieldArr.add(field.toString().trim());
                    field = new StringBuffer();
                } else
                {
                    field.append(ch);
                }
            }

            fieldArr.add(field.toString().trim());
            return (String[])fieldArr.toArray(new String[fieldArr.size()]);
        } else
        {
            return new String[0];
        }
    }

    public static String[] getFields(String line, char separator, char quote)
    {
        String parse[] = getFields(line, separator);
        for(int i = 0; i < parse.length; i++)
            parse[i] = stripQuotes(parse[i], quote);

        return parse;
    }

    public static String stripQuotes(String s, char quote)
    {
        if(s.length() >= 2 && s.charAt(0) == quote && s.charAt(s.length() - 1) == quote)
            return s.substring(1, s.length() - 1);
        else
            return s;
    }

    public static String resizeEnd(String s, int size)
    {
        return resizeEnd(s, size, ' ');
    }

    public static String getRandomString(int length)
    {
        Date d = new Date();
        Random r = new Random(d.getTime());
        String str = "";
        for(int i = 0; i < length; i++)
            str = (new StringBuilder(String.valueOf(str))).append((char)(65 + r.nextInt(26))).toString();

        return str;
    }

    public static String resizeEnd(String s, int size, char fillChar)
    {
        String res;
        try
        {
            res = s.substring(0, size);
        }
        catch(IndexOutOfBoundsException e)
        {
            res = s;
            for(int i = s.length(); i < size; i++)
                res = (new StringBuilder(String.valueOf(res))).append(fillChar).toString();

        }
        return res;
    }

    public static String resizeFront(String s, int size)
    {
        return resizeFront(s, size, ' ');
    }

    public static String resizeFront(String s, int size, char fillChar)
    {
        String res;
        try
        {
            res = s.substring(0, size);
        }
        catch(IndexOutOfBoundsException e)
        {
            res = s;
            for(int i = s.length(); i < size; i++)
                res = (new StringBuilder(String.valueOf(fillChar))).append(res).toString();

        }
        return res;
    }

    public static int matchingBracket(String s, int pos)
    {
        if(s == null || pos > s.length() - 1)
            return -1;
        char open = s.charAt(pos);
        char close;
        switch(open)
        {
        case 123: // '{'
            close = '}';
            break;

        case 40: // '('
            close = ')';
            break;

        case 91: // '['
            close = ']';
            break;

        case 60: // '<'
            close = '>';
            break;

        default:
            return -1;
        }
        pos++;
        int count;
        for(count = 1; count != 0 && pos < s.length(); pos++)
            if(s.charAt(pos) == open)
                count++;
            else
            if(s.charAt(pos) == close)
                count--;

        if(count != 0)
            return -1;
        else
            return pos - 1;
    }

    public static int getTreeID(String s)
    {
        if(s != null && s.length() > 0)
        {
            int end = s.indexOf(':', 1);
            if(end == -1)
                return -1;
            else
                return Integer.parseInt(s.substring(0, end));
        } else
        {
            return -1;
        }
    }

    public static String getRoot(String s)
    {
        if(s != null && s.length() > 0 && s.startsWith("{") && s.endsWith("}"))
        {
            int end = s.indexOf('{', 1);
            if(end == -1)
                end = s.indexOf('}', 1);
            return s.substring(1, end);
        } else
        {
            return null;
        }
    }

    public static List<String> getChildren(String s)
    {
        if(s != null && s.length() > 0 && s.startsWith("{") && s.endsWith("}"))
        {
            List<String> children = new ArrayList<>();
            int end = s.indexOf('{', 1);
            if(end == -1)
                return children;
            String rest = s.substring(end, s.length() - 1);
            for(int match = 0; rest.length() > 0 && (match = matchingBracket(rest, 0)) != -1;)
            {
                children.add(rest.substring(0, match + 1));
                if(match + 1 < rest.length())
                    rest = rest.substring(match + 1);
                else
                    rest = "";
            }

            return children;
        } else
        {
            return null;
        }
    }

    public static String parseTree(String s, List<String> children)
    {
        children.clear();
        if(s != null && s.length() > 0 && s.startsWith("{") && s.endsWith("}"))
        {
            int end = s.indexOf('{', 1);
            if(end == -1)
            {
                end = s.indexOf('}', 1);
                return s.substring(1, end);
            }
            String root = s.substring(1, end);
            String rest = s.substring(end, s.length() - 1);
            for(int match = 0; rest.length() > 0 && (match = matchingBracket(rest, 0)) != -1;)
            {
                children.add(rest.substring(0, match + 1));
                if(match + 1 < rest.length())
                    rest = rest.substring(match + 1);
                else
                    rest = "";
            }

            return root;
        } else
        {
            return null;
        }
    }

    public static String commaSeparatedList(String list[])
    {
        StringBuffer s = new StringBuffer();
        for(int i = 0; i < list.length; i++)
        {
            s.append(list[i]);
            if(i != list.length - 1)
                s.append(",");
        }

        return s.toString();
    }

    public static String commaSeparatedList(String list[], char quote)
    {
        StringBuffer s = new StringBuffer();
        for(int i = 0; i < list.length; i++)
        {
            s.append((new StringBuilder(String.valueOf(quote))).append(list[i]).append(quote).toString());
            if(i != list.length - 1)
                s.append(",");
        }

        return s.toString();
    }

    public static String spellOutNumber(String num)
    {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < num.length(); i++)
        {
            char ch = num.charAt(i);
            switch(ch)
            {
            case 48: // '0'
                sb.append("zero");
                break;

            case 49: // '1'
                sb.append("one");
                break;

            case 50: // '2'
                sb.append("two");
                break;

            case 51: // '3'
                sb.append("three");
                break;

            case 52: // '4'
                sb.append("four");
                break;

            case 53: // '5'
                sb.append("five");
                break;

            case 54: // '6'
                sb.append("six");
                break;

            case 55: // '7'
                sb.append("seven");
                break;

            case 56: // '8'
                sb.append("eight");
                break;

            case 57: // '9'
                sb.append("nine");
                break;

            default:
                sb.append(ch);
                break;
            }
        }

        return sb.toString();
    }

    public static String substituteBlanks(String s, String subst)
    {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < s.length(); i++)
            if(s.charAt(i) != ' ')
                sb.append(s.charAt(i));
            else
                sb.append(subst);

        return sb.toString();
    }

    public static String escapeLatex(String s)
    {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < s.length(); i++)
        {
            String c = (new StringBuilder(String.valueOf(s.charAt(i)))).toString();
            if(c.equals("#"))
                c = "\\#";
            if(c.equals("&"))
                c = "\\&";
            if(c.equals("$"))
                c = "\\$";
            if(c.equals("_"))
                c = "\\_";
            sb.append(c);
        }

        return sb.toString();
    }
}
