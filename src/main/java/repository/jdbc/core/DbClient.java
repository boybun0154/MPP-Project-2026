package repository.jdbc.core;

import config.DBConnection;

import java.sql.*;
import java.util.*;
import java.util.Optional;

public final class DbClient {

    private DbClient() {
    }

    public static <T> List<T> query(String sql, RowMapper<T> mapper, Object... params) {
        List<T> results = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setParameters(stmt, params);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapper.map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error: " + e.getMessage(), e);
        }

        return results;
    }

    public static <T> Optional<T> fetchOne(String sql, RowMapper<T> mapper, Object... params) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setParameters(stmt, params);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapper.map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error: " + e.getMessage(), e);
        }

        return Optional.empty();
    }


    public static int execute(String sql, Object... params) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setParameters(stmt, params);
            return stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error: " + e.getMessage(), e);
        }
    }

    private static void setParameters(PreparedStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }
}