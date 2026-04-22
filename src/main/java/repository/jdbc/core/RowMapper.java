package repository.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A Functional Interface that enables the use of Lambda expressions
 * to map SQL ResultSet rows into Domain Objects.
 * This approach decouples data extraction from the repository logic,
 * promoting cleaner and more reusable code.
 *
 * P.s: Like Professor taught us
 */
@FunctionalInterface
public interface RowMapper<T> {
    T map(ResultSet rs) throws SQLException;
}
