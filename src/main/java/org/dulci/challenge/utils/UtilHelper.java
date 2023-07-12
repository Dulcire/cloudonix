package org.dulci.challenge.utils;

import java.util.Set;
import java.util.logging.Logger;

public class UtilHelper {

    private static final Logger LOGGER = Logger.getLogger(UtilHelper.class.getName());

    /**
     * Method to find a word in wordlist, closest to the word provided in the
     * request in terms of total character value.
     * @param text word provided in the request.
     * @param wordList list of words.
     * @return word closest to the word provided.
     */
    public static String findClosestByValue(final String text, final Set<String> wordList) {
        LOGGER.info(String.format("Finding closest word to %s in list", text));
        if (wordList.isEmpty()) {
            return null;
        }
        String closestWord = "";
        int closestValue = Integer.MAX_VALUE;
        int largestValue = 0;

        for (String word : wordList) {
            final int wordValue = calculateTotalCharacterValue(word);
            final int textValue = calculateTotalCharacterValue(text);

            int difference = Math.abs(wordValue - textValue);

            if (difference < closestValue) {
                closestWord = word;
                closestValue = difference;
                largestValue = wordValue;
            } else if (difference == closestValue && wordValue > largestValue) {
                closestWord = word;
                largestValue = wordValue;
            } else if (difference == closestValue && wordValue == largestValue && word.compareTo(closestWord) > 0) {
                closestWord = word;
            }

        }
        return closestWord;
    }


    /**
     * Method to calculate the value of a word where character values are listed as a=1,
     * b=2 and so on.
     * @param word word to be calculated.
     * @return total value of the word
     */
    private static int calculateTotalCharacterValue(String word) {
        int totalValue = 0;
        for (char c : word.toCharArray()) {
            totalValue += Character.toLowerCase(c) - 'a' + 1;
        }

        return totalValue;
    }

    /**
     * Method to find a word in wordlist which contain the word closest to the word provided in the
     * request in terms of lexical closeness.
     * @param text word provided.
     * @param wordList list of words.
     * @return word closest to the word provided.
     */
    public static String findClosestByLexical(final String text, final Set<String> wordList) {
        LOGGER.info(String.format("Finding closest lexical word to %s in list", text));
        if (wordList.isEmpty()) {
            return null;
        }
        String closestWord = "";
        int closestCloseness = Integer.MAX_VALUE;

        for (String word : wordList) {
            int closeness = calculateLexicalCloseness(word, text);

            if (closeness < closestCloseness) {
                closestWord = word;
                closestCloseness = closeness;
            } else if (closeness == closestCloseness && word.compareTo(closestWord) < 0) {
                closestWord = word;
            }
        }

        return closestWord;
    }

    /**
     * Method to return that word that sorts lexically closest to
     * the provided request.
     * @param word1 word provided.
     * @param word2 word to comapre.
     * @return int value with te compared result .
     */
    private static int calculateLexicalCloseness(final String word1, final String word2) {
        return Math.abs(word1.compareToIgnoreCase(word2));
    }
}
