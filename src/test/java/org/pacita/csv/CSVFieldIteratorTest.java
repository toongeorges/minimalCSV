package org.pacita.csv;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Toon Macharis
 */
public class CSVFieldIteratorTest {
    @Test
    public void empty() {
        List<List<String>> expected = Arrays.asList(
        );

        List<List<String>> actual = toValues("");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void basic() {
        List<List<String>> expected = Arrays.asList(
            Arrays.asList("a", "b", "c")
        );

        List<List<String>> actual = toValues("a,b,c");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void multipleLinesWithEndingLineBreak() {
        List<List<String>> expected = Arrays.asList(
            Arrays.asList("a", "b", "c"),
            Arrays.asList("d", "e", "f"),
            Arrays.asList("g", "h", "i")
        );

        List<List<String>> actual = toValues(
                "a,b,c\n"
              + "d,e,f\n"
              + "g,h,i\n"
        );

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void multipleLinesWithoutEndingLineBreak() {
        List<List<String>> expected = Arrays.asList(
            Arrays.asList("a", "b", "c"),
            Arrays.asList("d", "e", "f"),
            Arrays.asList("g", "h", "i")
        );

        List<List<String>> actual = toValues(
                "a,b,c\n"
              + "d,e,f\n"
              + "g,h,i"
        );

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void emptyValuesWithEndingLineBreak() {
        List<List<String>> expected = Arrays.asList(
            Arrays.asList("", "", ""),
            Arrays.asList("", "", ""),
            Arrays.asList("", "", "")
        );

        List<List<String>> actual = toValues(
                ",,\n"
              + ",,\n"
              + ",,\n"
        );

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void emptyValuesWithoutEndingLineBreak() {
        List<List<String>> expected = Arrays.asList(
            Arrays.asList("", "", ""),
            Arrays.asList("", "", ""),
            Arrays.asList("", "", "")
        );

        List<List<String>> actual = toValues(
                ",,\n"
              + ",,\n"
              + ",,"
        );

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void quotes() {
        List<List<String>> expected = Arrays.asList(
            Arrays.asList("a", "b", "c"),
            Arrays.asList("d", "e", "f"),
            Arrays.asList("g", "h", "i")
        );

        List<List<String>> actual = toValues(
                "a,\"b\",c\n" //followed by ','
              + "d,e,\"f\"\n" //followed by '\n'
              + "g,h,\"i\""   //followed by EOF
        );

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void quotesContent() {
        List<List<String>> expected = Arrays.asList(
            Arrays.asList("a", ",b,b,", "c"),
            Arrays.asList("d", "\ne\ne\n", "f"),
            Arrays.asList("g", "\"h\"h\"", "i")
        );

        List<List<String>> actual = toValues(
                "a,\",b,b,\",c\n"        //String containing ','
              + "d,\"\ne\ne\n\",f\n"     //String containing '\n'
              + "g,\"\"\"h\"\"h\"\"\",i" //String containing '\"
        );

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void noMoreValues() {
        CSVFieldIterator iterator = new CSVFieldIterator(',', new StringReader(""));

        try {
            iterator.next();
            Assert.fail("Expected a NoSuchElementException");
        } catch (NoSuchElementException e) {
            //expected
        }
    }

    @Test
    public void skipHeader() {
        List<List<String>> expected = Arrays.asList(
            Arrays.asList("d", "e", "f"),
            Arrays.asList("g", "h", "i")
        );

        CSVFieldIterator iterator = new CSVFieldIterator(',', new StringReader(
                "a,b,c\n"
              + "d,e,f\n"
              + "g,h,i"
        ));

        Assert.assertTrue(iterator.skipLine());

        List<List<String>> actual = new ArrayList<>();
        for (List<String> line = iterator.readLine(); line != null; line = iterator.readLine()) {
            actual.add(line);
        }

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void skipMultipleLines() {
        List<List<String>> expected = Arrays.asList(
            Arrays.asList("g", "h", "i")
        );

        CSVFieldIterator iterator = new CSVFieldIterator(',', new StringReader(
                "a,b,c\n"
              + "d,e,f\n"
              + "g,h,i"
        ));

        Assert.assertTrue(iterator.skipLine());
        Assert.assertTrue(iterator.skipLine());

        List<List<String>> actual = new ArrayList<>();
        for (List<String> line = iterator.readLine(); line != null; line = iterator.readLine()) {
            actual.add(line);
        }

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void skipRandom() {
        List<List<String>> expected = Arrays.asList(
            Arrays.asList("a", "b", "c"),
            Arrays.asList("d"),
            Arrays.asList("g", "h", "i")
        );

        CSVFieldIterator iterator = new CSVFieldIterator(',', new StringReader(
                "a,b,c\n"
              + "d,e,f\n"
              + "g,h,i"
        ));

        List<List<String>> actual = new ArrayList<>();
        actual.add(iterator.readLine());
        actual.add(Arrays.asList(iterator.next()));

        Assert.assertTrue(iterator.skipLine());
        Assert.assertFalse(iterator.hasNextForLine());

        actual.add(iterator.readLine());

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void skipEOF() {
        List<List<String>> expected = Arrays.asList(
            Arrays.asList("a", "b", "c"),
            Arrays.asList("d", "e", "f"),
            Arrays.asList("g")
        );

        CSVFieldIterator iterator = new CSVFieldIterator(',', new StringReader(
                "a,b,c\n"
              + "d,e,f\n"
              + "g,h,i"
        ));

        List<List<String>> actual = new ArrayList<>();
        actual.add(iterator.readLine());
        actual.add(iterator.readLine());
        actual.add(Arrays.asList(iterator.next()));

        Assert.assertTrue(iterator.hasNext());
        Assert.assertFalse(iterator.skipLine());
        Assert.assertFalse(iterator.hasNext());
        Assert.assertFalse(iterator.skipLine());

        Assert.assertEquals(expected, actual);
    }

    private List<List<String>> toValues(String input) {
        CSVFieldIterator iterator = new CSVFieldIterator(',', new StringReader(input));

        List<List<String>> lines = new ArrayList<>();
        for (List<String> line = iterator.readLine(); line != null; line = iterator.readLine()) {
            lines.add(line);
        }
        return lines;
    }
}
