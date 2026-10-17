package com.project.ProjectS.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Existing databases may retain the obsolete, required roll-number column. */
@Component
public class AttendanceSchemaCompatibility implements ApplicationRunner {
    private final JdbcTemplate jdbc;

    public AttendanceSchemaCompatibility(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        jdbc.execute("""
                DO $$ BEGIN
                    IF EXISTS (SELECT 1 FROM information_schema.columns
                               WHERE table_schema = current_schema() AND table_name = 'attendance'
                                 AND column_name = 'roll_no' AND is_nullable = 'NO') THEN
                        ALTER TABLE attendance ALTER COLUMN roll_no DROP NOT NULL;
                    END IF;
                END $$;
                """);
    }
}
