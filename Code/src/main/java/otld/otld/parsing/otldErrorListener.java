package otld.otld.parsing;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.util.ArrayList;
import java.util.List;

public class otldErrorListener extends BaseErrorListener {
    private List<String> errors;

    public otldErrorListener() {
        this.errors = new ArrayList<>();
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {

        errors.add(
                new Error(line,
                          charPositionInLine,
                          ErrorMsg.SYNTAXERROR.getMessage(),
                          offendingSymbol.toString().charAt(0)
                ).getError()
        );
    }

    public List<String> getErrors() {
        return this.errors;
    }
}
