package org.dulci.challenge.utils;

import java.util.Set;

public class UtilHelper {

    public static String findClosestByValue(final String text, final Set<String> wordList) {
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


    private static int calculateTotalCharacterValue(String word) {
        int totalValue = 0;
        for (char c : word.toCharArray()) {
            totalValue += Character.toLowerCase(c) - 'a' + 1;
        }

        return totalValue;
    }

    public static String findClosestByLexical(final String text, final Set<String> wordList) {
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

    private static int calculateLexicalCloseness(final String word1, final String word2) {
        return Math.abs(word1.compareToIgnoreCase(word2));
    }
}
