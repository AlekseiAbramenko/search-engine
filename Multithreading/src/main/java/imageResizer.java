import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class imageResizer extends Thread {

    private List<File> files;
    private int targetWidth;
    private String dstFolder;
    private long start;

    public imageResizer(List<File> files, int targetWidth, String dstFolder, long start) {
        this.files = files;
        this.targetWidth = targetWidth;
        this.dstFolder = dstFolder;
        this.start = start;
    }

    public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        return Scalr.resize(originalImage, Scalr.Method.AUTOMATIC, Scalr.Mode.AUTOMATIC,
                targetWidth, targetHeight, Scalr.OP_ANTIALIAS);
    }

    @Override
    public void run() {
        try {
            for (File file : files) {
                BufferedImage originalImage = ImageIO.read(file);
                if (originalImage == null) {
                    continue;
                }

                int targetHeight = (int) Math.round(originalImage.getHeight() / (originalImage.getWidth() / (double) targetWidth));

                BufferedImage outputImage = resizeImage(originalImage, targetWidth, targetHeight);

                File newFile = new File(dstFolder + "/" + file.getName());

                ImageIO.write(outputImage, "jpg", newFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Finished after start: " + (System.currentTimeMillis() - start) + " ms");
    }
}
