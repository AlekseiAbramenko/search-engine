import java.io.PrintWriter;

public class OneThread {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        try {
            char[] letters = {'У', 'К', 'Е', 'Н', 'Х', 'В', 'А', 'Р', 'О', 'С', 'М', 'Т'};
            PrintWriter writer = new PrintWriter("res/numbers.txt");
            for (int regionCode = 1; regionCode <= 100; regionCode++) {
                StringBuilder builder = new StringBuilder();
                String region = padNumber(regionCode, 2);
                for (int number = 1; number < 1000; number++) {
                    String numbers = padNumber(number, 3);
                    for (char firstLetter : letters) {
                        for (char secondLetter : letters) {
                            for (char thirdLetter : letters) {
                                builder.append(firstLetter);
                                builder.append(numbers);
                                builder.append(secondLetter);
                                builder.append(thirdLetter);
                                builder.append(region);
                                builder.append("\n");
                            }
                        }
                    }
                }
                writer.write(builder.toString());
            }
            writer.flush();
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println((System.currentTimeMillis() - start) + " ms");
    }

    private static String padNumber(int number, int numberLength) {
        String numberStr = Integer.toString(number);

        switch (numberLength - numberStr.length()) {
            case 1 -> {
                return "0" + numberStr;
            }
            case 2 -> {
                return "00" + numberStr;
            }
        } return numberStr;
    }
}
