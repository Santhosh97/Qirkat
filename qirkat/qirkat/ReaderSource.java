package qirkat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Provides command input from a Reader.
 *
 * @author P. N. Hilfinger
 */
class ReaderSource implements CommandSource {

    /**
     * Input source.
     */
    private BufferedReader _input;
    /**
     * True if we request a prompt for each getLine.
     */
    private boolean _shouldPrompt;

    /**
     * A new source that reads from INPUT and prints prompts
     * if SHOULDPROMPT.
     */
    ReaderSource(Reader input, boolean shouldPrompt) {
        _input = new BufferedReader(input);
        _shouldPrompt = shouldPrompt;
    }

    @Override
    public String getLine(String prompt) {
        if (_input == null) {
            return null;
        }

        try {
            if (_shouldPrompt) {
                System.out.print(prompt);
                System.out.flush();
            }
            String result = _input.readLine();
            if (result == null) {
                _input.close();
            }
            return result;
        } catch (IOException excp) {
            return null;
        }
    }
}

