package com.project.ProjectS.util;

import com.project.ProjectS.entity.TableAttribute;
import com.project.ProjectS.entity.TableHeader;
import com.project.ProjectS.repository.TableAttributeRepository;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

public final class RuleEngineAttributeLookup {
    private RuleEngineAttributeLookup() {}

    public static TableAttribute resolve(TableAttributeRepository repository, String name,
                                         TableHeader fieldType, String field) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(field + " is required for Field Type '" + fieldType.getName() + "'");
        }
        String attributeName = name.trim();
        String context = field + " '" + attributeName + "' with Field Type '" + fieldType.getName() + "'";
        try {
            return repository.findByNameAndTableHeader(attributeName, fieldType)
                    .orElseThrow(() -> new IllegalArgumentException(context + " not found"));
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new IllegalArgumentException(context + " matched multiple database records", e);
        }
    }
}
