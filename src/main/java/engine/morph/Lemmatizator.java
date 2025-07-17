package engine.morph;

import org.apache.lucene.morphology.LuceneMorphology;

import java.util.*;

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

    public Map<String, Long> collectLemmas(String text) {
        Map<String, Long> lemmas = new HashMap<>();
        String[] words = splitRussianWords(text);

        for (String word : words) {
            if (word.isBlank()) continue;

            List<String> wordBaseForms = morph.getMorphInfo(word);
            if (isContainsParticle(wordBaseForms)) continue;

            List<String> normalForms = morph.getNormalForms(word);
            if (normalForms.isEmpty()) continue;

            String norm = normalForms.getFirst();
            if (lemmas.containsKey(norm)) {
                lemmas.put(norm, lemmas.get(norm) + 1);
            } else {
                lemmas.put(norm, 1L);
            }
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
