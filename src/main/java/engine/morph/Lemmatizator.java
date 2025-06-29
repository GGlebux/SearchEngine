package engine.morph;

import java.io.Console;
import java.util.*;

import static java.lang.System.out;

public class Lemmatizator {
    public static void main(String[] args) throws Exception {
        Console console = System.console();
        char[] password = console.readPassword("Enter password: ");
        out.println(new String(password));

//        LuceneMorphology russian = new RussianLuceneMorphology();
//        russian.getMorphInfo("замок").forEach(out::println);
//        boolean isRealWord = russian.checkString("ываыва");
//        out.println(isRealWord);


    }
}
