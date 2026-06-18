package ui;

import controller.PlaylistController;
import model.Musica;
import player.Reprodutor;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Painel Swing com controles de reproducao e dados da faixa atual.
 *
 * @author com.reprodutor
 * @version 1.0
 */
public class PlayerView extends JPanel implements Reprodutor.ReprodutorObserver {

    private final PlaylistController controller;
    private final JLabel labelTitulo;
    private final JLabel labelArtista;
    private final JProgressBar progresso;
    private final JButton botaoPlayPause;
    private final JToggleButton botaoRepetir;
    private final JToggleButton botaoEmbaralhar;
    private final Timer timerProgresso;
    private Musica musicaAtual;
    private int segundosDecorridos;

    /**
     * Cria o painel de controle do player.
     *
     * @param controller controller usado para comandos da UI.
     * @param reprodutor reprodutor observado pela tela.
     */
    public PlayerView(PlaylistController controller, Reprodutor reprodutor) {
        this.controller = controller;
        this.labelTitulo = new JLabel("Nenhuma musica", SwingConstants.CENTER);
        this.labelArtista = new JLabel("Adicione um MP3 para comecar", SwingConstants.CENTER);
        this.progresso = new JProgressBar();
        this.botaoPlayPause = criarBotaoControle("▶");
        this.botaoRepetir = criarToggle("↻");
        this.botaoEmbaralhar = criarToggle("⤨");
        this.timerProgresso = new Timer(1000, event -> atualizarProgresso());
        this.musicaAtual = null;
        this.segundosDecorridos = 0;

        configurarPainel();
        configurarAcoes();
        reprodutor.adicionarObserver(this);
    }

    /**
     * Recebe notificacao de alteracao da musica atual.
     *
     * @param musica musica atual ou null quando nada esta selecionado.
     */
    @Override
    public void onMusicaAlterada(Musica musica) {
        SwingUtilities.invokeLater(() -> {
            musicaAtual = musica;
            segundosDecorridos = 0;
            if (musica == null) {
                labelTitulo.setText("Nenhuma musica");
                labelArtista.setText("Adicione um MP3 para comecar");
                progresso.setIndeterminate(false);
                progresso.setValue(0);
                progresso.setString("00:00");
                return;
            }

            labelTitulo.setText(musica.getTitulo());
            labelArtista.setText(musica.getArtista());
            progresso.setIndeterminate(false);
            progresso.setMaximum(Math.max(1, musica.getDuracao()));
            progresso.setValue(0);
            progresso.setString(formatarTempo(0, musica.getDuracao()));
        });
    }

    /**
     * Recebe notificacao de alteracao do estado de reproducao.
     *
     * @param tocando true quando ha musica tocando.
     * @param pausado true quando a musica esta pausada.
     */
    @Override
    public void onEstadoAlterado(boolean tocando, boolean pausado) {
        SwingUtilities.invokeLater(() -> {
            botaoPlayPause.setText(tocando ? "⏸" : "▶");
            if (tocando) {
                if (musicaAtual != null && musicaAtual.getDuracao() <= 0) {
                    progresso.setString("Reproduzindo");
                    progresso.setIndeterminate(true);
                }
                timerProgresso.start();
            } else {
                timerProgresso.stop();
                progresso.setIndeterminate(false);
                if (pausado) {
                    progresso.setString("Pausado");
                }
            }
        });
    }

    /**
     * Recebe notificacao de erro de audio.
     *
     * @param mensagem mensagem amigavel do erro.
     */
    @Override
    public void onErro(String mensagem) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                this,
                mensagem,
                "Erro de reproducao",
                JOptionPane.ERROR_MESSAGE));
    }

    /**
     * Configura a estrutura visual do painel.
     */
    private void configurarPainel() {
        setLayout(new BorderLayout(12, 18));
        setPreferredSize(EstiloUI.DIMENSAO_PLAYER);
        setBackground(EstiloUI.COR_PAINEL);
        setBorder(EstiloUI.BORDA_PAINEL);

        JPanel painelInfo = new JPanel(new GridLayout(2, 1, 0, 8));
        painelInfo.setBackground(EstiloUI.COR_PAINEL);
        labelTitulo.setFont(EstiloUI.FONTE_TITULO);
        labelTitulo.setForeground(EstiloUI.COR_TEXTO);
        labelArtista.setFont(EstiloUI.FONTE_SUBTITULO);
        labelArtista.setForeground(EstiloUI.COR_TEXTO_SECUNDARIO);
        painelInfo.add(labelTitulo);
        painelInfo.add(labelArtista);

        progresso.setStringPainted(true);
        progresso.setString("00:00");
        progresso.setForeground(EstiloUI.COR_DESTAQUE);
        progresso.setBackground(EstiloUI.COR_FUNDO);

        JPanel centro = new JPanel(new BorderLayout(8, 18));
        centro.setBackground(EstiloUI.COR_PAINEL);
        centro.add(painelInfo, BorderLayout.NORTH);
        centro.add(progresso, BorderLayout.CENTER);
        centro.add(criarPainelVolume(), BorderLayout.SOUTH);

        add(centro, BorderLayout.CENTER);
        add(criarPainelControles(), BorderLayout.SOUTH);
    }

    /**
     * Configura as acoes dos botoes de controle.
     */
    private void configurarAcoes() {
        botaoPlayPause.addActionListener(event -> {
            if ("⏸".equals(botaoPlayPause.getText())) {
                controller.pause();
            } else {
                controller.play();
            }
        });
        botaoRepetir.addActionListener(event -> {
            boolean ativo = controller.alternarRepetir();
            aplicarEstadoToggle(botaoRepetir, ativo);
        });
        botaoEmbaralhar.addActionListener(event -> {
            boolean ativo = controller.alternarEmbaralhar();
            aplicarEstadoToggle(botaoEmbaralhar, ativo);
        });
    }

    /**
     * Cria o painel dos controles principais.
     *
     * @return painel com botoes de anterior, play, proxima, repetir e embaralhar.
     */
    private JPanel criarPainelControles() {
        JButton botaoAnterior = criarBotaoControle("⏮");
        botaoAnterior.addActionListener(event -> controller.faixaAnterior());

        JButton botaoProxima = criarBotaoControle("⏭");
        botaoProxima.addActionListener(event -> controller.proximaFaixa());

        JPanel painel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        painel.setBackground(EstiloUI.COR_PAINEL);
        painel.add(botaoEmbaralhar);
        painel.add(botaoAnterior);
        painel.add(botaoPlayPause);
        painel.add(botaoProxima);
        painel.add(botaoRepetir);
        return painel;
    }

    /**
     * Cria o painel do controle de volume.
     *
     * @return painel com label e slider de volume.
     */
    private JPanel criarPainelVolume() {
        JLabel labelVolume = new JLabel("Volume");
        labelVolume.setFont(EstiloUI.FONTE_PADRAO);
        labelVolume.setForeground(EstiloUI.COR_TEXTO_SECUNDARIO);

        JSlider sliderVolume = new JSlider(0, 100, 80);
        sliderVolume.setBackground(EstiloUI.COR_PAINEL);
        sliderVolume.setMajorTickSpacing(50);
        sliderVolume.setPaintTicks(true);
        sliderVolume.addChangeListener(event -> controller.definirVolume(sliderVolume.getValue()));

        JPanel painel = new JPanel(new BorderLayout(10, 0));
        painel.setBackground(EstiloUI.COR_PAINEL);
        painel.add(labelVolume, BorderLayout.WEST);
        painel.add(sliderVolume, BorderLayout.CENTER);
        return painel;
    }

    /**
     * Cria um botao de controle com simbolo Unicode.
     *
     * @param texto simbolo exibido no botao.
     * @return botao configurado.
     */
    private JButton criarBotaoControle(String texto) {
        JButton botao = new JButton(texto);
        botao.setFont(EstiloUI.FONTE_BOTAO);
        botao.setPreferredSize(EstiloUI.DIMENSAO_BOTAO_CONTROLE);
        botao.setFocusPainted(false);
        botao.setBorder(BorderFactory.createLineBorder(EstiloUI.COR_BORDA));
        return botao;
    }

    /**
     * Cria um toggle de controle com simbolo Unicode.
     *
     * @param texto simbolo exibido no toggle.
     * @return toggle configurado.
     */
    private JToggleButton criarToggle(String texto) {
        JToggleButton botao = new JToggleButton(texto);
        botao.setFont(EstiloUI.FONTE_BOTAO);
        botao.setPreferredSize(EstiloUI.DIMENSAO_BOTAO_CONTROLE);
        botao.setFocusPainted(false);
        botao.setBorder(BorderFactory.createLineBorder(EstiloUI.COR_BORDA));
        return botao;
    }

    /**
     * Aplica destaque visual ao toggle quando ativo.
     *
     * @param botao toggle atualizado.
     * @param ativo true quando o modo esta ativo.
     */
    private void aplicarEstadoToggle(JToggleButton botao, boolean ativo) {
        botao.setSelected(ativo);
        botao.setForeground(ativo ? EstiloUI.COR_DESTAQUE : EstiloUI.COR_TEXTO);
    }

    /**
     * Atualiza o progresso visual uma vez por segundo.
     */
    private void atualizarProgresso() {
        if (musicaAtual == null || musicaAtual.getDuracao() <= 0) {
            return;
        }

        segundosDecorridos = Math.min(segundosDecorridos + 1, musicaAtual.getDuracao());
        progresso.setValue(segundosDecorridos);
        progresso.setString(formatarTempo(segundosDecorridos, musicaAtual.getDuracao()));
    }

    /**
     * Formata tempos em segundos para exibicao.
     *
     * @param atual segundos decorridos.
     * @param total segundos totais.
     * @return texto formatado.
     */
    private String formatarTempo(int atual, int total) {
        return formatarMinutos(atual) + " / " + (total > 0 ? formatarMinutos(total) : "--:--");
    }

    /**
     * Converte segundos para mm:ss.
     *
     * @param segundos total de segundos.
     * @return texto no formato mm:ss.
     */
    private String formatarMinutos(int segundos) {
        int minutos = segundos / 60;
        int resto = segundos % 60;
        return String.format("%02d:%02d", minutos, resto);
    }
}
