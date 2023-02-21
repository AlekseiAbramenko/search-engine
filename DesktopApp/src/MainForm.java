import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainForm {
    private JPanel mainPanel;
    private JPanel southPanel;
    private JButton collapseButton;
    private JTextField patronymic;
    private JTextField name;
    private JTextField surName;
    private JPanel centerPanel;
    private JButton expendButton;
    private JTextField SNP;

    private final String rusInputRegex = "[а-яёА-ЯЁ]+";
    private final Pattern pattern = Pattern.compile(rusInputRegex);

    public MainForm() {
        expendButton.setVisible(false);
        SNP.setVisible(false);

        collapseButton.addActionListener(new Action() {
            @Override
            public Object getValue(String key) {
                return null;
            }

            @Override
            public void putValue(String key, Object value) {

            }

            @Override
            public void setEnabled(boolean b) {

            }

            @Override
            public boolean isEnabled() {
                return false;
            }

            @Override
            public void addPropertyChangeListener(PropertyChangeListener listener) {

            }

            @Override
            public void removePropertyChangeListener(PropertyChangeListener listener) {

            }

            @Override
            public void actionPerformed(ActionEvent e) {
                Matcher surNameM = pattern.matcher(getSurName());
                Matcher nameM = pattern.matcher(getName());
                Matcher patronymicM = pattern.matcher(getPatronymic());
                if(surNameM.find()
                        && nameM.find()
                        && patronymicM.find()
                ) {
                    expendButton.setVisible(true);
                    SNP.setVisible(true);
                    String snp = getSurName() + " " + getName() + " " + getPatronymic();
                    SNP.setText(snp);
                    collapseButton.setVisible(false);
                    surName.setVisible(false);
                    name.setVisible(false);
                    patronymic.setVisible(false);
                } else {
                    JOptionPane.showMessageDialog(
                            mainPanel,
                            "Введите корректные данные! \nПравильный формат: Иванов Иван Иванович",
                            "Ошибка",
                            JOptionPane.PLAIN_MESSAGE
                    );
                }
            }
        });

        expendButton.addActionListener(new Action() {
            @Override
            public Object getValue(String key) {
                return null;
            }

            @Override
            public void putValue(String key, Object value) {

            }

            @Override
            public void setEnabled(boolean b) {

            }

            @Override
            public boolean isEnabled() {
                return false;
            }

            @Override
            public void addPropertyChangeListener(PropertyChangeListener listener) {

            }

            @Override
            public void removePropertyChangeListener(PropertyChangeListener listener) {

            }

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String snp = getSNP();
                    String[] tokens = snp.split("\\s+");
                    int indexSurname = 0;
                    int indexName = 1;
                    int indexPatronymic = 2;
                    Matcher surNameM = pattern.matcher(tokens[indexSurname]);
                    Matcher nameM = pattern.matcher(tokens[indexName]);
                    Matcher patronymicM = pattern.matcher(tokens[indexPatronymic]);
                    if (tokens.length == 3
                            && surNameM.find()
                            && nameM.find()
                            && patronymicM.find()
                    ) {
                        surName.setVisible(true);
                        name.setVisible(true);
                        patronymic.setVisible(true);
                        collapseButton.setVisible(true);
                        SNP.setVisible(false);
                        expendButton.setVisible(false);
                        surName.setText(tokens[indexSurname]);
                        name.setText(tokens[indexName]);
                        patronymic.setText(tokens[indexPatronymic]);
                    } else {
                        JOptionPane.showMessageDialog(
                                mainPanel,
                                "Введите корректные данные! \nПравильный формат: Иванов Иван Иванович",
                                "Ошибка",
                                JOptionPane.PLAIN_MESSAGE
                        );
                    }
                } catch (ArrayIndexOutOfBoundsException ex) {
                    JOptionPane.showMessageDialog(
                            mainPanel,
                            "Введите корректные данные! \nПравильный формат: Иванов Иван Иванович",
                            "Ошибка",
                            JOptionPane.PLAIN_MESSAGE
                    );
                }
            }
        });
    }

    public String getSurName() {
        return surName.getText();
    }

    public String getName() {
        return name.getText();
    }

    public String getPatronymic() {
        return patronymic.getText();
    }

    public String getSNP() {
        return SNP.getText();
    }
    public JPanel getJpanel() {
        return mainPanel;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
