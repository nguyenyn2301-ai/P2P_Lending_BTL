package util;

import java.math.BigDecimal;

public class NumberToVietnameseWords {
    private static final String[] UNITS = {"", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín"};
    private static final String[] TEENS = {"mười", "mười một", "mười hai", "mười ba", "mười bốn", "mười lăm", "mười sáu", "mười bảy", "mười tám", "mười chín"};
    private static final String[] TENS = {"", "", "hai mươi", "ba mươi", "bốn mươi", "năm mươi", "sáu mươi", "bảy mươi", "tám mươi", "chín mươi"};

    public static String convert(BigDecimal amount) {
        if (amount == null) return "Không";
        long n = amount.longValue();
        if (n == 0) return "Không đồng";
        return readTriple(n).trim() + " đồng";
    }

    private static String readTriple(long n) {
        if (n < 10) return UNITS[(int) n];
        if (n < 20) return TEENS[(int) (n - 10)];
        if (n < 100) {
            int t = (int) (n / 10);
            int u = (int) (n % 10);
            if (u == 0) return TENS[t];
            if (u == 1 && t > 1) return TENS[t] + " mốt";
            if (u == 5 && t > 0) return TENS[t] + " lăm";
            return TENS[t] + " " + UNITS[u];
        }
        if (n < 1000) {
            int h = (int) (n / 100);
            int rest = (int) (n % 100);
            String head = (h == 1 ? "một trăm" : UNITS[h] + " trăm");
            if (rest == 0) return head;
            if (rest < 10) return head + " lẻ " + UNITS[rest];
            return head + " " + readTriple(rest);
        }
        if (n < 1_000_000) {
            int th = (int) (n / 1000);
            int rest = (int) (n % 1000);
            String s = readTriple(th) + " nghìn";
            if (rest > 0) s += " " + readTriple(rest);
            return s;
        }
        if (n < 1_000_000_000L) {
            int m = (int) (n / 1_000_000);
            int rest = (int) (n % 1_000_000);
            String s = readTriple(m) + " triệu";
            if (rest > 0) s += " " + readTriple(rest);
            return s;
        }
        long b = n / 1_000_000_000L;
        int rest = (int) (n % 1_000_000_000L);
        String s = readTriple(b) + " tỷ";
        if (rest > 0) s += " " + readTriple(rest);
        return s;
    }
}
