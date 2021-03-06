package com.slotmachine.ocr.mic;

import android.graphics.Rect;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText;
import java.util.ArrayList;
import java.util.List;

public class TextParser {

    //private String text;

    public TextParser() { }

    /*public static void main(String[] args) {

    }*/

    /*private static boolean charIsIn(char c, String string) {
        for (int i = 0; i < string.length(); i++) {
            if (c == string.charAt(i))
                return true;
        }
        return false;
    }*/

    /*public String getText() {
        return this.text;
    }*/

    // need to start at $
    // Basic algorithm to determine if text is relevant to current progressive value
    public static List<String> parse(List<FirebaseVisionDocumentText.Word> words) {

        boolean hasReachedFirstDollarSign = false;
        List<String> ret = new ArrayList<>();
        Rect dollarSignRect = new Rect();
        StringBuilder tmp = new StringBuilder();
        for (FirebaseVisionDocumentText.Word word : words) {
            if (word.getText().contains("$")) {
                hasReachedFirstDollarSign = true;
            }
            if (hasReachedFirstDollarSign) {
                if (word.getText().contains("$")) {
                    if (tmp.toString().trim().isEmpty()) {
                        tmp.append(word.getText());
                    } else { // tmp not "" so add to ret and then clear tmp
                        ret.add(tmp.toString().trim());
                        tmp = new StringBuilder(word.getText());
                    }
                    dollarSignRect = word.getBoundingBox();
                } else { // Not dollar sign
                    double dollarSignMidpoint = (dollarSignRect.bottom - dollarSignRect.top) / 2.0 + dollarSignRect.top;
                    int dollarSignHeight = dollarSignRect.bottom - dollarSignRect.top;
                    int currentHeight = word.getBoundingBox().bottom - word.getBoundingBox().top;
                    boolean lessThanHalfHeight = currentHeight < dollarSignHeight / 2.0;
                    boolean greaterThanDoubleHeight = currentHeight > dollarSignHeight * 2.0;
                    boolean higherThanMidpoint = word.getBoundingBox().top > dollarSignMidpoint;
                    boolean lowerThanMidpoint = word.getBoundingBox().bottom < dollarSignMidpoint;
                    if (lessThanHalfHeight || greaterThanDoubleHeight || higherThanMidpoint || lowerThanMidpoint) {
                        // ignore - maybe break?
                    } else {
                        tmp.append(word.getText());
                    }
                }
            }
        }

        // add the last remaining tmp
        if (!tmp.toString().trim().isEmpty()) {
            ret.add(tmp.toString().trim());
        }

        // Format dollar values
        for (int i = 0; i < ret.size(); i++) {
            String formatted = formatProgressives(ret.get(i)).trim();
            if (!formatted.isEmpty()) {
                ret.set(i, formatProgressives(ret.get(i)));
            }
        }

        return ret;
    }

    private static String formatProgressives(String progressive) {
        StringBuilder formattedProgressive = new StringBuilder();
        for (int i = 0; i < progressive.length(); i++) {
            if ("0123456789".indexOf(progressive.charAt(i)) >= 0) {
                formattedProgressive.append(progressive.charAt(i));
            }
        }
        // Add decimal point
        if (formattedProgressive.length() == 3) {
            formattedProgressive = formattedProgressive.append(".00");
        } else if (formattedProgressive.length() > 3) {
            formattedProgressive.insert(formattedProgressive.length() - 2, '.');
        }
        return formattedProgressive.toString();
    }


}
