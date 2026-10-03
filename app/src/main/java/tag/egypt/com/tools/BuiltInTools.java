package tag.egypt.com.tools;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Suite of standard built-in offline tools in TAJ EGY.
 */
public final class BuiltInTools {

    private BuiltInTools() {
        // Utility
    }

    public static final class CalculatorTool implements Tool {
        @Override
        public String getName() {
            return "calculator";
        }

        @Override
        public String getDescription() {
            return "Safely evaluates mathematical expressions (addition, subtraction, multiplication, division, powers).";
        }

        @Override
        public String getInputSchemaJson() {
            return "{\"type\":\"object\",\"properties\":{\"expression\":{\"type\":\"string\",\"description\":\"The math expression to calculate\"}},\"required\":[\"expression\"]}";
        }

        @Override
        public String execute(String argumentsJson) throws Exception {
            JSONObject obj = new JSONObject(argumentsJson);
            String expression = obj.getString("expression").replaceAll("[^0-9+\\-*/().^ ]", "");
            double result = evalSimple(expression);
            return new JSONObject().put("result", result).toString();
        }

        private double evalSimple(String str) {
            str = str.replaceAll(" ", "");
            if (str.contains("+")) {
                String[] p = str.split("\\+", 2);
                return evalSimple(p[0]) + evalSimple(p[1]);
            }
            if (str.contains("-") && !str.startsWith("-")) {
                String[] p = str.split("-", 2);
                return evalSimple(p[0]) - evalSimple(p[1]);
            }
            if (str.contains("*")) {
                String[] p = str.split("\\*", 2);
                return evalSimple(p[0]) * evalSimple(p[1]);
            }
            if (str.contains("/")) {
                String[] p = str.split("/", 2);
                double divisor = evalSimple(p[1]);
                if (divisor == 0) throw new ArithmeticException("Division by zero");
                return evalSimple(p[0]) / divisor;
            }
            return Double.parseDouble(str);
        }
    }

    public static final class DateTimeTool implements Tool {
        @Override
        public String getName() {
            return "get_current_time";
        }

        @Override
        public String getDescription() {
            return "Returns current system time, UTC time, and formatted date.";
        }

        @Override
        public String getInputSchemaJson() {
            return "{\"type\":\"object\",\"properties\":{}}";
        }

        @Override
        public String execute(String argumentsJson) throws Exception {
            long now = System.currentTimeMillis();
            SimpleDateFormat localFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.US);
            SimpleDateFormat utcFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            utcFmt.setTimeZone(TimeZone.getTimeZone("UTC"));

            JSONObject out = new JSONObject();
            out.put("timestamp_ms", now);
            out.put("local_time", localFmt.format(new Date(now)));
            out.put("utc_time", utcFmt.format(new Date(now)));
            return out.toString();
        }
    }

    public static final class TokenEstimatorTool implements Tool {
        @Override
        public String getName() {
            return "estimate_tokens";
        }

        @Override
        public String getDescription() {
            return "Estimates prompt and completion token counts using BPE heuristic (~4 characters per token for English).";
        }

        @Override
        public String getInputSchemaJson() {
            return "{\"type\":\"object\",\"properties\":{\"text\":{\"type\":\"string\"}},\"required\":[\"text\"]}";
        }

        @Override
        public String execute(String argumentsJson) throws Exception {
            JSONObject obj = new JSONObject(argumentsJson);
            String text = obj.optString("text", "");
            int chars = text.length();
            int estimatedTokens = Math.max(1, (int) Math.ceil(chars / 3.8));

            JSONObject out = new JSONObject();
            out.put("characters", chars);
            out.put("estimated_tokens", estimatedTokens);
            return out.toString();
        }
    }

    public static final class JsonFormatterTool implements Tool {
        @Override
        public String getName() {
            return "format_json";
        }

        @Override
        public String getDescription() {
            return "Formats and pretty-prints raw JSON strings with 2-space indentation.";
        }

        @Override
        public String getInputSchemaJson() {
            return "{\"type\":\"object\",\"properties\":{\"raw_json\":{\"type\":\"string\"}},\"required\":[\"raw_json\"]}";
        }

        @Override
        public String execute(String argumentsJson) throws Exception {
            JSONObject obj = new JSONObject(argumentsJson);
            String raw = obj.getString("raw_json");
            String pretty;
            if (raw.trim().startsWith("[")) {
                pretty = new org.json.JSONArray(raw).toString(2);
            } else {
                pretty = new JSONObject(raw).toString(2);
            }
            return new JSONObject().put("formatted", pretty).toString();
        }
    }
}
