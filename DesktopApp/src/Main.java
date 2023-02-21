import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        JFrame collapseFrame = new JFrame();
        collapseFrame.setSize(400, 250);
        collapseFrame.setTitle("Введите ФИО");

        collapseFrame.add(new MainForm().getJpanel());

        collapseFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        collapseFrame.setLocationRelativeTo(null);
        collapseFrame.setVisible(true);

    }
}