package org.pacita.csv;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * @author Toon Macharis
 */
public class CSVFieldIterator implements Iterator<String> {
    private final char separator;
    private final Reader reader;
    private final StringBuilder buffer = new StringBuilder();

    private ReadState readState;
    private boolean newLine;
    private boolean separatorBeforeEOF;
    private int c;

    private static enum ReadState {
        REGULAR, QUOTE, SEPARATOR, EOL, EOF
    }

    public CSVFieldIterator(char separator, Reader reader) {
        switch (separator) {
            case '\n':
            case '\r':
            case '\"':
                throw new IllegalArgumentException("Invalid value for separator, it cannot be '\\n', '\\r' or '\\\"'.");
        }
        this.separator = separator;
        this.reader = Objects.requireNonNull(reader);
        readState = startField();
    }

    public boolean skipLine() {
        do {
            readCharCloseSafe();
        } while ((c != '\n') && (c != -1));
        if (c == '\n') {
            newLine = false;
            separatorBeforeEOF = false;
            readState = startField();
            return true;
        }
        return false;
    }

    public boolean hasNextForLine() {
        return hasNext() && !newLine;
    }

    @Override
    public boolean hasNext() {
        return (readState != ReadState.EOF) || separatorBeforeEOF;
    }

    @Override
    public String next() {
        newLine = false;
        switch (readState) {
            case EOF:
                if (!separatorBeforeEOF) {
                    throw new NoSuchElementException();
                } //else fall through
            case EOL:
            case SEPARATOR:
                endField(); //there were no characters read before EOF, '\n' or the separator, therefore the field is the empty String
                break;
            case QUOTE:
                buffer.setLength(0); //remove start quote
                do {
                    continueField();
                    switch (c) {
                        case '\"':
                            buffer.setLength(buffer.length() - 1); //unescape "" to " or remove closing "
                            readChar();
                            break;
                        case -1:
                            break;
                        default:
                            c = 0; //prevent endField() to return true for '\n' or the separator, because they are within quotes
                    }
                } while (!endField());
                break;
            case REGULAR:
                do {
                    continueField();
                } while (!endField());
                break;
        }

        String value = buffer.toString();
        readState = startField();
        return value;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove");
    }

    private ReadState startField() {
        buffer.setLength(0); //reset the buffer for the next read
        readChar();
        return readCharState();
    }

    private void continueField() {
        do {
            readChar();
        } while (readCharState() == ReadState.REGULAR);
    }

    private boolean endField() {
        switch (c) {
            case '\n':
                newLine = true;
                buffer.setLength(buffer.length() - 1); //remove new line
                //fall through
            case -1:
                separatorBeforeEOF = false;
                return true;
            default:
                if (c == separator) {
                    buffer.setLength(buffer.length() - 1); //remove separator
                    separatorBeforeEOF = true;
                    return true;
                }
                return false;
        }
    }

    private ReadState readCharState() {
        switch (c) {
            case -1:
                return ReadState.EOF;
            case '\n':
                return ReadState.EOL;
            case '\"':
                return ReadState.QUOTE;
            default:
                return (c == separator) ? ReadState.SEPARATOR : ReadState.REGULAR;
        }
    }

    private void readChar() {
        readCharCloseSafe();
        if (c != -1) {
            buffer.append((char) c);
        }
    }

    private void readCharCloseSafe() {
        if (c != -1) { //not yet closed
            try {
                do {
                    c = reader.read();
                } while (c == '\r'); //ignore '\r'
                if (c == -1) {
                    reader.close();
                }
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }
}
