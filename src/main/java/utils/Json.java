package utils;

import model.Client;
import model.Department;
import model.Employee;
import model.Project;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Minimal JSON helpers. Avoids pulling in a third-party library.
 * Only covers the shapes we currently return from the controllers.
 */
public final class Json {
    private Json() {}

    public static String escape(String s) {
        if (s == null) return "null";
        StringBuilder sb = new StringBuilder(s.length() + 2);
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"':  sb.append("\\\""); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        sb.append('"');
        return sb.toString();
    }

    public static String str(String s) { return s == null ? "null" : escape(s); }
    public static String num(Number n) { return n == null ? "null" : n.toString(); }
    public static String date(LocalDate d) { return d == null ? "null" : escape(d.toString()); }
    public static String money(BigDecimal b) { return b == null ? "null" : b.toPlainString(); }

    public static String array(Collection<String> items) {
        StringBuilder sb = new StringBuilder("[");
        Iterator<String> it = items.iterator();
        while (it.hasNext()) {
            sb.append(it.next());
            if (it.hasNext()) sb.append(',');
        }
        sb.append(']');
        return sb.toString();
    }

    public static String obj(Map<String, String> fields) {
        StringBuilder sb = new StringBuilder("{");
        Iterator<Map.Entry<String, String>> it = fields.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> e = it.next();
            sb.append(escape(e.getKey())).append(':').append(e.getValue());
            if (it.hasNext()) sb.append(',');
        }
        sb.append('}');
        return sb.toString();
    }

    public static String of(Client c) {
        return "{"
                + "\"id\":" + num(c.getId())
                + ",\"name\":" + str(c.getName())
                + ",\"industry\":" + str(c.getIndustry())
                + ",\"primaryContactName\":" + str(c.getPrimaryContactName())
                + ",\"primaryContactPhone\":" + str(c.getPrimaryContactPhone())
                + ",\"primaryContactEmail\":" + str(c.getPrimaryContactEmail())
                + "}";
    }

    public static String of(Department d) {
        return "{"
                + "\"id\":" + num(d.getId())
                + ",\"name\":" + str(d.getName())
                + ",\"location\":" + str(d.getLocation())
                + ",\"annualBudget\":" + money(d.getAnnualBudget())
                + "}";
    }

    public static String of(Employee e) {
        return "{"
                + "\"id\":" + num(e.getId())
                + ",\"fullName\":" + str(e.getFullName())
                + ",\"title\":" + str(e.getTitle())
                + ",\"hireDate\":" + date(e.getHireDate())
                + ",\"salary\":" + money(e.getSalary())
                + ",\"departmentId\":" + (e.getDepartment() == null ? "null" : num(e.getDepartment().getId()))
                + "}";
    }

    private static final Pattern FIELD = Pattern.compile(
            "\"([^\"]+)\"\\s*:\\s*(\"((?:\\\\.|[^\"\\\\])*)\"|-?\\d+(?:\\.\\d+)?|true|false|null)");

    /** Very small flat-JSON parser: returns string values (unquoted where applicable). */
    public static Map<String, String> parse(String body) {
        Map<String, String> map = new HashMap<>();
        if (body == null) return map;
        Matcher m = FIELD.matcher(body);
        while (m.find()) {
            String key = m.group(1);
            String quoted = m.group(3);
            String raw = m.group(2);
            if (quoted != null) {
                map.put(key, unescape(quoted));
            } else if ("null".equals(raw)) {
                map.put(key, null);
            } else {
                map.put(key, raw);
            }
        }
        return map;
    }

    private static String unescape(String s) {
        return s.replace("\\\"", "\"").replace("\\\\", "\\")
                .replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t");
    }

    public static String of(Project p) {
        return "{"
                + "\"id\":" + num(p.getId())
                + ",\"name\":" + str(p.getName())
                + ",\"description\":" + str(p.getDescription())
                + ",\"startDate\":" + date(p.getStartDate())
                + ",\"endDate\":" + date(p.getEndDate())
                + ",\"budget\":" + money(p.getBudget())
                + ",\"status\":" + str(p.getStatus())
                + "}";
    }
}
