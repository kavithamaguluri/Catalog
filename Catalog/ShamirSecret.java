import org.json.JSONObject;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

public class ShamirSecret {
    public static void main(String[] args) {
        try {
            // Process the first test case (testcase1.json)
            System.out.println("Processing testcase1.json...");
            processTestCase("testcase1.json");

            // Process the second test case (testcase2.json)
            System.out.println("Processing testcase2.json...");
            processTestCase("testcase2.json");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Function to process each test case
    private static void processTestCase(String jsonFilePath) throws IOException {
        // Parse JSON file
        JSONObject jsonData = new JSONObject(readFile(jsonFilePath));
        
        // Extract keys
        JSONObject keys = jsonData.getJSONObject("keys");
        int n = keys.getInt("n");
        int k = keys.getInt("k");
        
        // Decode roots (x, y pairs)
        List<Point> points = new ArrayList<>();
        for (String key : jsonData.keySet()) {
            if (!key.equals("keys")) {
                JSONObject point = jsonData.getJSONObject(key);
                int x = Integer.parseInt(key); // x-coordinate
                int base = point.getInt("base");
                String value = point.getString("value");
                BigInteger y = decodeValue(value, base); // Decode y-coordinate
                points.add(new Point(x, y));
            }
        }

        // Ensure we have enough roots
        if (points.size() < k) {
            throw new IllegalArgumentException("Insufficient number of roots.");
        }

        // Solve polynomial using Lagrange interpolation
        BigInteger secret = solveForConstant(points.subList(0, k));
        System.out.println("The constant term (secret) for " + jsonFilePath + " is: " + secret);
    }

    // Function to decode the value from the given base
    private static BigInteger decodeValue(String value, int base) {
        return new BigInteger(value, base); // Using BigInteger to decode the base value
    }

    // Function to solve for constant term (c) using Lagrange Interpolation
    private static BigInteger solveForConstant(List<Point> points) {
        BigInteger result = BigInteger.ZERO;

        for (int i = 0; i < points.size(); i++) {
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int j = 0; j < points.size(); j++) {
                if (i != j) {
                    BigInteger x_i = BigInteger.valueOf(points.get(i).x);
                    BigInteger x_j = BigInteger.valueOf(points.get(j).x);
                    numerator = numerator.multiply(x_j.negate()); // Multiply (-x_j)
                    denominator = denominator.multiply(x_i.subtract(x_j)); // (x_i - x_j)
                }
            }

            // Lagrange term: y_i * (numerator/denominator)
            BigInteger term = points.get(i).y.multiply(numerator).divide(denominator);
            result = result.add(term);
        }

        return result.mod(BigInteger.TWO.pow(256)); // Ensure the result fits within a 256-bit number
    }

    // Helper to read file content
    private static String readFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (FileReader reader = new FileReader(filePath)) {
            int ch;
            while ((ch = reader.read()) != -1) {
                content.append((char) ch);
            }
        }
        return content.toString();
    }

    // Point class to store x, y pairs
    static class Point {
        int x;
        BigInteger y;

        Point(int x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
    }
}
