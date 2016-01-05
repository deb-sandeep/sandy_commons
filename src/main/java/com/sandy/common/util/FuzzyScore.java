package com.sandy.common.util;

/**
 * A matching algorithm that is similar to the searching algorithms implemented in editors such
 * as Sublime Text, TextMate, Atom and others.
 *
 * <p>
 * One point is given for every matched character. Subsequent matches yield two bonus points. A higher score
 * indicates a higher similarity.
 * </p>
 *
 * <p>
 * This code has been adapted from Apache Commons Lang 3.3.
 * </p>
 */
public class FuzzyScore {


    /**
     * <p>
     * Find the Fuzzy Score which indicates the similarity score between two
     * Strings.
     * </p>
     *
     * <pre>
     * score.fuzzyScore(null, null, null)                                    = IllegalArgumentException
     * score.fuzzyScore("", "", Locale.ENGLISH)                              = 0
     * score.fuzzyScore("Workshop", "b", Locale.ENGLISH)                     = 0
     * score.fuzzyScore("Room", "o", Locale.ENGLISH)                         = 1
     * score.fuzzyScore("Workshop", "w", Locale.ENGLISH)                     = 1
     * score.fuzzyScore("Workshop", "ws", Locale.ENGLISH)                    = 2
     * score.fuzzyScore("Workshop", "wo", Locale.ENGLISH)                    = 4
     * score.fuzzyScore("Apache Software Foundation", "asf", Locale.ENGLISH) = 3
     * </pre>
     *
     * @param term a full term that should be matched against, must not be null
     * @param query the query that will be matched against a term, must not be
     *            null
     * @return result score
     * @throws IllegalArgumentException if either String input {@code null} or
     *             Locale input {@code null}
     */
    public Integer fuzzyScore(CharSequence term, CharSequence query) {
        if (term == null || query == null) {
            throw new IllegalArgumentException("Strings must not be null");
        }

        // fuzzy logic is case insensitive. We normalize the Strings to lower
        // case right from the start. Turning characters to lower case
        // via Character.toLowerCase(char) is unfortunately insufficient
        // as it does not accept a locale.
        final String termLowerCase = term.toString().toLowerCase();
        final String queryLowerCase = query.toString().toLowerCase();

        // the resulting score
        int score = 0;

        // the position in the term which will be scanned next for potential
        // query character matches
        int termIndex = 0;

        // index of the previously matched character in the term
        int previousMatchingCharacterIndex = Integer.MIN_VALUE;

        for (int queryIndex = 0; queryIndex < queryLowerCase.length(); queryIndex++) {
            final char queryChar = queryLowerCase.charAt(queryIndex);

            boolean termCharacterMatchFound = false;
            for (; termIndex < termLowerCase.length()
                    && !termCharacterMatchFound; termIndex++) {
                final char termChar = termLowerCase.charAt(termIndex);

                if (queryChar == termChar) {
                    
                    // Enhancement - a case sensitive match results in higher
                    // score than an insensitive match. Note - we are ignoring
                    // locale.
                    if( query.charAt( queryIndex ) == term.charAt( termIndex ) ) {
                        score += 2 ;
                    }
                    else {
                        // simple character matches result in one point
                        score += 1 ;
                    }
                    
                    // subsequent character matches further improve
                    // the score.
                    if (previousMatchingCharacterIndex + 1 == termIndex) {
                        score += 2;
                    }

                    previousMatchingCharacterIndex = termIndex;

                    // we can leave the nested loop. Every character in the
                    // query can match at most one character in the term.
                    termCharacterMatchFound = true;
                }
            }
        }

        return score;
    }
}