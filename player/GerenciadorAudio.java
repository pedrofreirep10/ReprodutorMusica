package player;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

/**
 * Gerencia a reproducao de MP3 com JLayer em uma thread separada da EDT.
 *
 * @author com.reprodutor
 * @version 1.0
 */
public class GerenciadorAudio {

    private final Object controleLock;
    private final AtomicBoolean pausado;
    private final AtomicBoolean parado;
    private final AtomicBoolean tocando;
    private AdvancedPlayer player;
    private Thread audioThread;
    private Runnable onMusicaTerminada;
    private Consumer<String> onErro;
    private int volume;
    private String caminhoAtual;
    private int frameAtual;
    private long versaoReproducao;
    private boolean pausaSolicitada;
    private boolean paradaSolicitada;

    /**
     * Cria um gerenciador de audio pronto para receber comandos.
     */
    public GerenciadorAudio() {
        this.controleLock = new Object();
        this.pausado = new AtomicBoolean(false);
        this.parado = new AtomicBoolean(true);
        this.tocando = new AtomicBoolean(false);
        this.volume = 80;
        this.caminhoAtual = null;
        this.frameAtual = 0;
        this.versaoReproducao = 0L;
        this.pausaSolicitada = false;
        this.paradaSolicitada = true;
    }

    /**
     * Define o callback disparado quando a faixa termina naturalmente.
     *
     * @param onMusicaTerminada acao executada ao final da musica.
     */
    public void setOnMusicaTerminada(Runnable onMusicaTerminada) {
        this.onMusicaTerminada = onMusicaTerminada;
    }

    /**
     * Define o callback usado para relatar erros de audio ou IO.
     *
     * @param onErro consumidor da mensagem de erro.
     */
    public void setOnErro(Consumer<String> onErro) {
        this.onErro = onErro;
    }

    /**
     * Inicia a reproducao de um arquivo MP3 em thread separada.
     *
     * @param caminho caminho absoluto do arquivo MP3.
     * @throws IllegalArgumentException quando o caminho e nulo ou vazio.
     */
    public void play(String caminho) {
        if (caminho == null || caminho.isBlank()) {
            throw new IllegalArgumentException("Caminho do arquivo MP3 nao informado.");
        }

        parar();

        long versao;
        synchronized (controleLock) {
            caminhoAtual = caminho;
            frameAtual = 0;
            versaoReproducao++;
            versao = versaoReproducao;
            paradaSolicitada = false;
            pausaSolicitada = false;
            parado.set(false);
            pausado.set(false);
            tocando.set(true);
        }

        // A decodificacao MP3 e bloqueante; por isso ela nunca roda na EDT do Swing.
        audioThread = new Thread(() -> executarAudio(caminho, 0, versao), "jlayer-audio-thread");
        // Daemon evita que a aplicacao fique presa caso a janela seja fechada durante a reproducao.
        audioThread.setDaemon(true);
        audioThread.start();
    }

    /**
     * Pausa a reproducao mantendo a thread viva.
     */
    public void pause() {
        if (tocando.get() && !parado.get()) {
            synchronized (controleLock) {
                // Pausa em JLayer e feita fechando o player atual; o listener guarda o frame.
                pausaSolicitada = true;
                paradaSolicitada = false;
                pausado.set(true);
                tocando.set(false);
                if (player != null) {
                    player.close();
                }
            }
        }
    }

    /**
     * Retoma uma reproducao pausada.
     */
    public void resumir() {
        String caminho;
        int frameInicial;
        long versao;

        synchronized (controleLock) {
            if (!pausado.get() || caminhoAtual == null) {
                return;
            }

            caminho = caminhoAtual;
            frameInicial = Math.max(0, frameAtual);
            versaoReproducao++;
            versao = versaoReproducao;
            pausaSolicitada = false;
            paradaSolicitada = false;
            parado.set(false);
            pausado.set(false);
            tocando.set(!parado.get());
        }

        // A retomada cria um novo AdvancedPlayer no frame salvo durante a pausa.
        audioThread = new Thread(() -> executarAudio(caminho, frameInicial, versao), "jlayer-audio-thread");
        audioThread.setDaemon(true);
        audioThread.start();
    }

    /**
     * Para a reproducao atual e libera os recursos do player.
     */
    public void parar() {
        synchronized (controleLock) {
            paradaSolicitada = true;
            pausaSolicitada = false;
            frameAtual = 0;
            parado.set(true);
            pausado.set(false);
            tocando.set(false);
            if (player != null) {
                // Fechar o player interrompe leituras bloqueantes feitas pelo JLayer.
                player.close();
            }
        }
    }

    /**
     * Define o volume solicitado pela UI.
     *
     * @param volume volume entre 0 e 100.
     */
    public void setVolume(int volume) {
        this.volume = Math.max(0, Math.min(100, volume));
    }

    /**
     * Retorna o volume solicitado pela UI.
     *
     * @return volume entre 0 e 100.
     */
    public int getVolume() {
        return volume;
    }

    /**
     * Informa se ha audio tocando.
     *
     * @return true quando a reproducao esta ativa.
     */
    public boolean isTocando() {
        return tocando.get();
    }

    /**
     * Informa se a reproducao esta pausada.
     *
     * @return true quando a reproducao esta pausada.
     */
    public boolean isPausado() {
        return pausado.get();
    }

    /**
     * Executa a leitura e decodificacao do MP3.
     *
     * @param caminho caminho absoluto do arquivo MP3.
     */
    private void executarAudio(String caminho, int frameInicial, long versao) {
        boolean terminouNaturalmente = false;
        AdvancedPlayer playerLocal = null;

        try (FileInputStream arquivo = new FileInputStream(caminho)) {
            playerLocal = new AdvancedPlayer(arquivo);
            AdvancedPlayer playerDaThread = playerLocal;
            playerDaThread.setPlayBackListener(new PlaybackListener() {
                /**
                 * Guarda o ultimo frame conhecido quando o player finaliza ou e fechado.
                 *
                 * @param evento evento de finalizacao do JLayer.
                 */
                @Override
                public void playbackFinished(PlaybackEvent evento) {
                    synchronized (controleLock) {
                        if (versao == versaoReproducao && evento != null) {
                            frameAtual = Math.max(0, evento.getFrame());
                        }
                    }
                }
            });

            synchronized (controleLock) {
                if (versao != versaoReproducao || parado.get()) {
                    playerDaThread.close();
                    return;
                }
                player = playerDaThread;
            }

            // play(inicio, fim) deixa o JLayer decodificar continuamente, emitindo audio de forma estavel.
            playerDaThread.play(Math.max(0, frameInicial), Integer.MAX_VALUE);
            terminouNaturalmente = terminouSemInterrupcaoManual(versao);
        } catch (IOException | JavaLayerException e) {
            if (!foiInterrompidoManualmente(versao)) {
                notificarErro("Erro ao reproduzir o arquivo MP3: " + e.getMessage());
            }
        } finally {
            finalizarThreadAudio(playerLocal, versao, terminouNaturalmente);
        }

        if (terminouNaturalmente && onMusicaTerminada != null) {
            onMusicaTerminada.run();
        }
    }

    /**
     * Verifica se a execucao terminou sem pausa ou parada manual.
     *
     * @param versao versao da reproducao da thread atual.
     * @return true quando o fim foi natural.
     */
    private boolean terminouSemInterrupcaoManual(long versao) {
        synchronized (controleLock) {
            return versao == versaoReproducao && !pausaSolicitada && !paradaSolicitada;
        }
    }

    /**
     * Verifica se a thread foi encerrada por pausa, stop ou troca de musica.
     *
     * @param versao versao da reproducao da thread atual.
     * @return true quando a interrupcao foi solicitada pela aplicacao.
     */
    private boolean foiInterrompidoManualmente(long versao) {
        synchronized (controleLock) {
            return versao != versaoReproducao || pausaSolicitada || paradaSolicitada || parado.get();
        }
    }

    /**
     * Limpa referencias da thread de audio sem fechar um player novo por engano.
     *
     * @param playerLocal player criado pela thread atual.
     * @param versao versao da reproducao da thread atual.
     * @param terminouNaturalmente true quando a musica acabou.
     */
    private void finalizarThreadAudio(AdvancedPlayer playerLocal, long versao, boolean terminouNaturalmente) {
        synchronized (controleLock) {
            if (player == playerLocal) {
                player = null;
            }

            if (versao == versaoReproducao) {
                tocando.set(false);
                if (terminouNaturalmente) {
                    parado.set(true);
                    pausado.set(false);
                    frameAtual = 0;
                }
            }
        }
    }

    /**
     * Notifica erros ao consumidor configurado.
     *
     * @param mensagem mensagem amigavel para a camada de UI.
     */
    private void notificarErro(String mensagem) {
        Consumer<String> consumidor = onErro;
        if (Objects.nonNull(consumidor)) {
            consumidor.accept(mensagem);
        }
    }
}
