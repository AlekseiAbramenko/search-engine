import java.io.*;

public class WorkerThread implements Runnable {
    private int regionFrom;
    private int regionTo;
    private String fileNameNumber;
    private Long start;

    public WorkerThread(int getRegionCodeFrom, int regionCodeTo, String fileNameNumber, Long start) {
        this.regionFrom = getRegionCodeFrom;
        this.regionTo = regionCodeTo;
        this.fileNameNumber = fileNameNumber;
        this.start = start;
    }

    @Override
    public void run() {
        try {
            char[] letters = {'У', 'К', 'Е', 'Н', 'Х', 'В', 'А', 'Р', 'О', 'С', 'М', 'Т'};
            BufferedWriter writer = new  BufferedWriter(new FileWriter("res/numbers" + fileNameNumber + ".txt"));

            for (int regionCode = regionFrom; regionCode <= regionTo; regionCode++) {
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
