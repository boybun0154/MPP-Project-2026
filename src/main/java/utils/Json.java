package utils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class Json {
    private Json() {
    }

    public static String of(Object obj) {
        if (obj == null) return "null";
        // Se o objeto principal já for uma lista, redireciona para ofList
        if (obj instanceof Collection<?>) return ofList(new ArrayList<>((Collection<?>) obj));

        Field[] fields = obj.getClass().getDeclaredFields();
        String body = Arrays.stream(fields)
                .map(f -> {
                    f.setAccessible(true);
                    try {
                        Object val = f.get(obj);
                        if (val == null) return "\"" + f.getName() + "\":null";

                        String res;
                        if (val instanceof Number || val instanceof Boolean) {
                            res = val.toString();
                        } else if (val instanceof Collection<?>) {
                            res = ofList(new ArrayList<>((Collection<?>) val));
                        } else {
                            res = "\"" + val.toString().replace("\"", "\\\"") + "\"";
                        }

                        return "\"" + f.getName() + "\":" + res;
                    } catch (Exception e) {
                        return "";
                    }
                })
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(","));

        return "{" + body + "}";
    }

    public static String ofList(List<?> list) {
        if (list == null) return "[]";
        return "[" + list.stream().map(Json::of).collect(Collectors.joining(",")) + "]";
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            T obj = clazz.getDeclaredConstructor().newInstance();
            Map<String, String> data = parse(json);
            for (Field f : clazz.getDeclaredFields()) {
                f.setAccessible(true);
                String val = data.get(f.getName());
                if (val != null && !"null".equals(val)) {
                    Class<?> t = f.getType();
                    if (t == String.class) f.set(obj, val);
                    else if (t == Integer.class || t == int.class) f.set(obj, Integer.parseInt(val));
                    else if (t == Double.class || t == double.class) f.set(obj, Double.parseDouble(val));
                    else if (t == Long.class || t == long.class) f.set(obj, Long.parseLong(val));
                    else if (t == BigDecimal.class) f.set(obj, new BigDecimal(val));
                    else if (t == LocalDate.class) f.set(obj, LocalDate.parse(val));
                }
            }
            return obj;
        } catch (Exception e) {
            throw new RuntimeException("Json Parse Error", e);
        }
    }

    public static Map<String, String> parse(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null || json.isBlank()) return map;

        Matcher m = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(\"([^\"]*)\"|[^,}]+)").matcher(json);
        while (m.find()) {
            String key = m.group(1);
            String val = m.group(3) != null ? m.group(3) : m.group(2).trim();
            map.put(key, val);
        }
        return map;
    }
}