package e.ptextarea;

import java.util.*;
import java.util.regex.*;

/**
 * Recognizes keywords within NORMAL text segments and styles them KEYWORD.
 */
public class KeywordStyleApplicator extends RegularExpressionStyleApplicator {
    private HashSet<String> keywords;
    
    public KeywordStyleApplicator(PTextArea textArea, HashSet<String> keywords, String keywordRegularExpression) {
        super(textArea, keywordRegularExpression, PStyle.KEYWORD);
        this.keywords = keywords;
    }
    
    @Override
    public boolean isAcceptableMatch(CharSequence line, Matcher matcher) {
        return keywords.contains(matcher.group());
    }
}
