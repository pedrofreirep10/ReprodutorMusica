package ui;

import controller.PlaylistController;
import model.Musica;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Painel Swing responsavel por exibir e gerenciar a playlist.
 *
 * @author com.reprodutor
 * @version 1.0
 */
public class PlaylistView extends JPanel {

    private final PlaylistController controller;
    private final DefaultListModel<Musica> listModel;
    private final JList<Musica> listaMusicas;

    /**
     * Cria a visualizacao da playlist.
     *
     * @param controller controller usado para acionar operacoes do modelo.
     */
    public PlaylistView(PlaylistController controller) {
        this.controller = controller;
        this.listModel = new DefaultListModel<>();
        this.listaMusicas = new JList<>(listModel);

        configurarPainel();
        configurarLista();
        configurarListeners();
        atualizarLista(controller.obterTodas());
    }

    /**
     * Atualiza a JList com a lista recebida.
     *
     * @param musicas musicas que devem aparecer na interface.
     */
    public void atualizarLista(List<Musica> musicas) {
        Runnable atualizar = () -> {
            listModel.clear();
            for (Musica musica : musicas) {
                listModel.addElement(musica);
            }
        };

        if (SwingUtilities.isEventDispatchThread()) {
            atualizar.run();
        } else {
            SwingUtilities.invokeLater(atualizar);
        }
    }

    /**
     * Configura o layout e os botoes principais do painel.
     */
    private void configurarPainel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(EstiloUI.COR_FUNDO);
        setBorder(EstiloUI.BORDA_PAINEL);

        JLabel titulo = new JLabel("Playlist");
        titulo.setFont(EstiloUI.FONTE_TITULO);
        titulo.setForeground(EstiloUI.COR_TEXTO);
        add(titulo, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(listaMusicas);
        scrollPane.setBorder(EstiloUI.BORDA_PADRAO);
        add(scrollPane, BorderLayout.CENTER);

        JButton botaoAdicionar = criarBotaoAcao("Adicionar MP3");
        botaoAdicionar.addActionListener(event -> adicionarArquivos());

        JButton botaoRemover = criarBotaoAcao("Remover faixa");
        botaoRemover.addActionListener(event -> removerSelecionada());

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        painelBotoes.setBackground(EstiloUI.COR_FUNDO);
        painelBotoes.add(botaoAdicionar);
        painelBotoes.add(botaoRemover);
        add(painelBotoes, BorderLayout.SOUTH);
    }

    /**
     * Aplica estilos e comportamento da lista de musicas.
     */
    private void configurarLista() {
        listaMusicas.setFont(EstiloUI.FONTE_PADRAO);
        listaMusicas.setForeground(EstiloUI.COR_TEXTO);
        listaMusicas.setBackground(EstiloUI.COR_PAINEL);
        listaMusicas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaMusicas.setFixedCellHeight(42);
        listaMusicas.setCellRenderer(new DefaultListCellRenderer() {
            /**
             * Renderiza cada musica com cores padronizadas.
             *
             * @param list lista que contem o item.
             * @param value valor da celula.
             * @param index indice da celula.
             * @param isSelected true quando a celula esta selecionada.
             * @param cellHasFocus true quando a celula possui foco.
             * @return componente usado para renderizar a celula.
             */
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                label.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 12, 0, 12));
                label.setFont(EstiloUI.FONTE_PADRAO);
                if (isSelected) {
                    label.setBackground(EstiloUI.COR_DESTAQUE);
                    label.setForeground(EstiloUI.COR_PAINEL);
                }
                return label;
            }
        });
    }

    /**
     * Registra listeners de mouse e de atualizacao da playlist.
     */
    private void configurarListeners() {
        listaMusicas.addMouseListener(new MouseAdapter() {
            /**
             * Inicia reproducao no duplo clique sobre uma faixa.
             *
             * @param event evento de mouse recebido pela lista.
             */
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    Musica selecionada = listaMusicas.getSelectedValue();
                    if (selecionada != null) {
                        controller.tocarSelecionada(selecionada);
                    }
                }
            }
        });
        controller.adicionarPlaylistListener(this::atualizarLista);
    }

    /**
     * Abre o JFileChooser e adiciona os arquivos MP3 selecionados.
     */
    private void adicionarArquivos() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Adicionar arquivos MP3");
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivos MP3", "mp3"));

        int resultado = fileChooser.showOpenDialog(this);
        if (resultado != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File[] arquivos = fileChooser.getSelectedFiles();
        for (File arquivo : arquivos) {
            try {
                controller.adicionarArquivo(arquivo);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(
                        this,
                        e.getMessage(),
                        "Erro ao adicionar arquivo",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Remove a faixa atualmente selecionada.
     */
    private void removerSelecionada() {
        Musica selecionada = listaMusicas.getSelectedValue();
        if (selecionada == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Selecione uma faixa para remover.",
                    "Nenhuma faixa selecionada",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        controller.removerMusica(selecionada);
    }

    /**
     * Cria um botao de acao padronizado.
     *
     * @param texto texto exibido no botao.
     * @return botao configurado.
     */
    private JButton criarBotaoAcao(String texto) {
        JButton botao = new JButton(texto);
        botao.setFont(EstiloUI.FONTE_PADRAO);
        botao.setPreferredSize(EstiloUI.DIMENSAO_BOTAO_ACAO);
        botao.setFocusPainted(false);
        return botao;
    }
}
