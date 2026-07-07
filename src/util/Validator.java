package util;

/** Input validation utilities — throws IllegalArgumentException for bad input. */
public class Validator {

    public static String requireNonEmpty(String v, String name) {
        if (v == null || v.trim().isEmpty())
            throw new IllegalArgumentException(name + " is required.");
        return v.trim();
    }

    public static double requirePositiveDouble(String v, String name) {
        try {
            double d = Double.parseDouble(v.trim());
            if (d <= 0) throw new IllegalArgumentException(name + " must be > 0.");
            return d;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " must be a valid number.");
        }
    }

    public static int requirePositiveInt(String v, String name) {
        try {
            int i = Integer.parseInt(v.trim());
            if (i <= 0) throw new IllegalArgumentException(name + " must be > 0.");
            return i;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " must be a whole number.");
        }
    }

    public static int requireNonNegativeInt(String v, String name) {
        try {
            int i = Integer.parseInt(v.trim());
            if (i < 0) throw new IllegalArgumentException(name + " must be >= 0.");
            return i;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " must be a whole number.");
        }
    }

    public static String requireDate(String v, String name) {
        if (v == null || !v.trim().matches("\\d{4}-\\d{2}-\\d{2}"))
            throw new IllegalArgumentException(name + " must be YYYY-MM-DD.");
        return v.trim();
    }

    public static String requirePhone(String v, String name) {
        if (v == null || !v.trim().matches("[0-9+\\-() ]{7,20}"))
            throw new IllegalArgumentException(name + " must be a valid phone number.");
        return v.trim();
    }
}
