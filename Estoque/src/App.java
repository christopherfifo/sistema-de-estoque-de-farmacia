import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import telas.TelaLogin;

public class App {
    public static void main(String[] args) {
        
        UIManager.put("OptionPane.yesButtonText", "Sim");
        UIManager.put("OptionPane.noButtonText", "Nao");

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new TelaLogin().setVisible(true);
            }
        });
    }
}