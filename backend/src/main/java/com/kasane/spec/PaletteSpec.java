package com.kasane.spec;

import com.kasane.model.Palette;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class PaletteSpec {

    private PaletteSpec() {}

    public static Specification<Palette> withFilters(String hue, String era, Integer colorCount, String mood, String q) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (hue != null && !hue.isBlank()) {
                predicates.add(cb.equal(root.get("dominantHue"), hue));
            }
            if (era != null && !era.isBlank()) {
                predicates.add(cb.equal(root.get("era"), era));
            }
            if (colorCount != null) {
                predicates.add(cb.equal(root.get("colorCount"), colorCount));
            }
            if (mood != null && !mood.isBlank()) {
                predicates.add(cb.like(root.get("moods"), "%" + mood + "%"));
            }
            if (q != null && !q.isBlank()) {
                String pattern = "%" + q.toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("summary")), pattern),
                    cb.like(cb.lower(root.get("colorName1")), pattern),
                    cb.like(cb.lower(root.get("colorName2")), pattern),
                    cb.like(cb.lower(root.get("colorName3")), pattern),
                    cb.like(cb.lower(root.get("colorName4")), pattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
