package com.project.ProjectS.util;

import com.project.ProjectS.entity.Chapter;
import com.project.ProjectS.entity.RuleEngine;
import com.project.ProjectS.entity.TableAttribute;
import com.project.ProjectS.exception.DuplicateRuleEngineException;
import com.project.ProjectS.repository.RuleEngineRepository;
import org.springframework.dao.DataIntegrityViolationException;

public final class RuleEngineUniqueness {
    private static final String CONSTRAINT = "rule_engines_chapter_attribute_key";
    private RuleEngineUniqueness() {}

    public static void requireAvailable(RuleEngineRepository repository, Chapter chapter,
                                        TableAttribute attribute, Long excludedRuleId) {
        if (repository.existsForChapterAndAttribute(chapter.getChapterId(), attribute.getAttributeId(), excludedRuleId)) {
            throw duplicate(chapter, attribute);
        }
    }

    public static void save(RuleEngineRepository repository, RuleEngine rule) {
        try {
            repository.saveAndFlush(rule);
        } catch (DataIntegrityViolationException e) {
            // The DB constraint also protects simultaneous requests that both passed the precheck.
            for (Throwable cause = e; cause != null; cause = cause.getCause()) {
                if (cause.getMessage() != null && cause.getMessage().contains(CONSTRAINT)) {
                    throw duplicate(rule.getChapter(), rule.getTableAttributeid());
                }
            }
            throw e;
        }
    }

    private static DuplicateRuleEngineException duplicate(Chapter chapter, TableAttribute attribute) {
        return new DuplicateRuleEngineException("A rule already exists for attribute '" + attribute.getName()
                + "' (ID " + attribute.getAttributeId() + ") in chapter '" + chapter.getName()
                + "' (ID " + chapter.getChapterId() + "). Only one rule per attribute per chapter is allowed.");
    }
}
