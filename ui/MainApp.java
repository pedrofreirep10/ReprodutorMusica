package ui;

import controller.PlaylistController;
import model.Playlist;
import player.Reprodutor;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Classe principal que cria a janela Swing do reprodutor de musicas.
 *
 * @author Guilherme Pereira de Rivoredo
 * @author João Batistella da Costa
 * @author Pedro Henrique Freire Pereira
 * @version 1.0
 */
public class MainApp {

    /**
     * Ponto de entrada da aplicacao.
     *
     * @param args argumentos de linha de comando.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(MainApp::criarInterface);
    }

    /**
     * Cria e exibe a interface principal na EDT do Swing.
     */
    public static void criarInterface() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                 | UnsupportedLookAndFeelException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Nao foi possivel aplicar o tema do sistema: " + e.getMessage(),
                    "Aviso de interface",
                    JOptionPane.WARNING_MESSAGE);
        }

        Playlist playlist = new Playlist();
        Reprodutor reprodutor = new Reprodutor(playlist);
        PlaylistController controller = new PlaylistController(playlist, reprodutor);

        JFrame frame = new JFrame("Reprodutor de Musicas");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(EstiloUI.DIMENSAO_JANELA);
        frame.setLayout(new BorderLayout());

        PlayerView playerView = new PlayerView(controller, reprodutor);
        PlaylistView playlistView = new PlaylistView(controller);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, playerView, playlistView);
        splitPane.setResizeWeight(0.34);
        splitPane.setDividerSize(8);
        splitPane.setBorder(null);

        frame.add(splitPane, BorderLayout.CENTER);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            /**
             * Para o audio antes do fechamento da janela.
             *
             * @param event evento de fechamento da janela.
             */
            @Override
            public void windowClosing(java.awt.event.WindowEvent event) {
                controller.stop();
            }
        });

        frame.setSize(EstiloUI.DIMENSAO_JANELA);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
