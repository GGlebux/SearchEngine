package engine.morph;

import engine.parsing.TestWriter;
import org.apache.lucene.morphology.LuceneMorphology;

import java.io.IOException;
import java.util.*;

import static java.lang.String.join;
import static java.util.Arrays.stream;
import static java.util.Locale.ROOT;
import static java.util.Set.of;
import static java.util.regex.Pattern.compile;

public class Lemmatizator {
    private final LuceneMorphology morph;
    private final static String NON_RUS_REX = "([^а-яё\\s])";
    private final static String WHITESPACE = "\\s+";
    private final static String RUS_WORD_INFO = "[а-яё]+\\|";
    private final static String PARTICLES_REX = "[А-ЯЁ]+";
    private final static Set<String> particles = of("МЕЖД", "ПРЕДЛ", "СОЮЗ");

    public Lemmatizator(LuceneMorphology morph) {
        this.morph = morph;
    }

    public Map<String, Integer> collectLemmas(String text) {
        Map<String, Integer> lemmas = new HashMap<>();
        String[] words = splitRussianWords(text);

        List<String> form = new ArrayList<>();


        for (String word : words) {
            if (word.isBlank()) continue;

            List<String> wordBaseForms = morph.getMorphInfo(word);
            if (isContainsParticle(wordBaseForms)) continue;

            List<String> normalForms = morph.getNormalForms(word);
            if (normalForms.isEmpty()) continue;

            if (normalForms
                    .stream()
                    .anyMatch(base -> base.equals("ть"))) {
                System.out.println("Гадское слово: " + word + "___" + normalForms+ "___" + wordBaseForms);
            }

            String norm = normalForms.getFirst();

            form.add(norm);

            if (lemmas.containsKey(norm)) {
                lemmas.put(norm, lemmas.get(norm) + 1);
            } else {
                lemmas.put(norm, 1);
            }
        }

        TestWriter body_writer;
        TestWriter split_writer;
        TestWriter format_writer;
        try {
            body_writer = new TestWriter("body.txt");
            body_writer.write(join("\n", text.split(WHITESPACE)));

            split_writer = new TestWriter("split.txt");
            split_writer.write(join("\n", words));

            format_writer = new TestWriter("format.txt");
            format_writer.write(join("\n", form));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lemmas;

    }

    /**
     * @param text Текст с разными языками и знаками препинания
     * @return {@code String[]} Массив русских слов в нижнем регистре
     */
    private static String[] splitRussianWords(String text) {
        return text.toLowerCase(ROOT)
                .replaceAll(NON_RUS_REX, " ")
                .trim()
                .split(WHITESPACE);
    }

    /**
     * @param wordsMorphInfo список морфологической информации о слове
     * @return {@code boolean} является ли слово частицей
     */
    private static boolean isContainsParticle(List<String> wordsMorphInfo) {
        return wordsMorphInfo
                .stream()
                .flatMap(info -> stream(info
                        .replaceAll(RUS_WORD_INFO, " ")
                        .split(WHITESPACE)))
                .filter(info -> compile(PARTICLES_REX).matcher(info).matches())
                .anyMatch(particles::contains);
    }
}
