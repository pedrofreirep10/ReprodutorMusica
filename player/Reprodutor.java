package player;

import model.Musica;
import model.NoMusica;
import model.Playlist;
import java.util.ArrayList;
import java.util.List;

/**
 * Controla a playlist, o gerenciador de audio e notifica observers da UI.
 *
 * @author com.reprodutor
 * @version 1.0
 */
public class Reprodutor {

    /**
     * Observer usado pela UI para receber mudancas do reprodutor sem JavaFX properties.
     *
     * @author com.reprodutor
     * @version 1.0
     */
    public interface ReprodutorObserver {

        /**
         * Chamado quando a musica atual muda.
         *
         * @param musica musica atual ou null quando nada esta selecionado.
         */
        void onMusicaAlterada(Musica musica);

        /**
         * Chamado quando o estado de reproducao muda.
         *
         * @param tocando true quando uma faixa esta tocando.
         * @param pausado true quando uma faixa esta pausada.
         */
        void onEstadoAlterado(boolean tocando, boolean pausado);

        /**
         * Chamado quando ocorre erro na camada de audio.
         *
         * @param mensagem mensagem amigavel do erro.
         */
        void onErro(String mensagem);
    }

    private final Playlist playlist;
    private final GerenciadorAudio gerenciadorAudio;
    private final List<ReprodutorObserver> observers;
    private boolean tocando;
    private boolean pausado;

    /**
     * Cria um reprodutor para a playlist informada.
     *
     * @param playlist playlist controlada pelo reprodutor.
     */
    public Reprodutor(Playlist playlist) {
        this.playlist = playlist;
        this.gerenciadorAudio = new GerenciadorAudio();
        this.observers = new ArrayList<>();
        this.tocando = false;
        this.pausado = false;

        this.gerenciadorAudio.setOnMusicaTerminada(() -> {
            if (this.playlist.isModoRepetir()) {
                playAtual();
            } else {
                proximaFaixa();
            }
        });
        this.gerenciadorAudio.setOnErro(this::notificarErro);
    }

    /**
     * Registra um observer do reprodutor.
     *
     * @param observer observer que recebera notificacoes.
     */
    public void adicionarObserver(ReprodutorObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Remove um observer registrado.
     *
     * @param observer observer que deixara de receber notificacoes.
     */
    public void removerObserver(ReprodutorObserver observer) {
        observers.remove(observer);
    }

    /**
     * Inicia ou retoma a reproducao.
     */
    public void play() {
        if (playlist.isEmpty()) {
            return;
        }

        if (pausado) {
            gerenciadorAudio.resumir();
            tocando = true;
            pausado = false;
            notificarEstado();
            return;
        }

        if (playlist.getAtual() == null) {
            playlist.setAtual(playlist.getCabeca());
        }

        playAtual();
    }

    /**
     * Pausa a reproducao atual.
     */
    public void pause() {
        if (!tocando) {
            return;
        }

        gerenciadorAudio.pause();
        tocando = false;
        pausado = true;
        notificarEstado();
    }

    /**
     * Para a reproducao e limpa o estado de pausa.
     */
    public void stop() {
        gerenciadorAudio.parar();
        tocando = false;
        pausado = false;
        notificarEstado();
    }

    /**
     * Avanca para a proxima faixa e inicia a reproducao.
     */
    public void proximaFaixa() {
        if (playlist.isEmpty()) {
            return;
        }

        playlist.tocarProxima();
        playAtual();
    }

    /**
     * Volta para a faixa anterior e inicia a reproducao.
     */
    public void faixaAnterior() {
        if (playlist.isEmpty()) {
            return;
        }

        playlist.tocarAnterior();
        playAtual();
    }

    /**
     * Inicia a reproducao de um no especifico da playlist.
     *
     * @param no no da musica que deve tocar.
     */
    public void playMusica(NoMusica no) {
        if (no == null) {
            return;
        }

        playlist.setAtual(no);
        playAtual();
    }

    /**
     * Define o volume solicitado pela UI.
     *
     * @param volume volume entre 0 e 100.
     */
    public void setVolume(int volume) {
        gerenciadorAudio.setVolume(volume);
    }

    /**
     * Retorna a musica atualmente selecionada.
     *
     * @return musica atual ou null quando nao ha selecao.
     */
    public Musica getMusicaAtual() {
        return playlist.getAtual() == null ? null : playlist.getAtual().getMusica();
    }

    /**
     * Informa se o reprodutor esta tocando.
     *
     * @return true quando ha uma faixa em reproducao.
     */
    public boolean isTocando() {
        return tocando;
    }

    /**
     * Informa se o reprodutor esta pausado.
     *
     * @return true quando a faixa atual esta pausada.
     */
    public boolean isPausado() {
        return pausado;
    }

    /**
     * Reproduz a musica apontada pelo no atual.
     */
    private void playAtual() {
        NoMusica noAtual = playlist.getAtual();
        if (noAtual == null) {
            return;
        }

        try {
            gerenciadorAudio.play(noAtual.getMusica().getCaminhoArquivo());
            tocando = true;
            pausado = false;
            notificarMusica(noAtual.getMusica());
            notificarEstado();
        } catch (IllegalArgumentException e) {
            notificarErro(e.getMessage());
        }
    }

    /**
     * Notifica observers sobre a musica atual.
     *
     * @param musica musica atual.
     */
    private void notificarMusica(Musica musica) {
        for (ReprodutorObserver observer : List.copyOf(observers)) {
            observer.onMusicaAlterada(musica);
        }
    }

    /**
     * Notifica observers sobre estado de reproducao.
     */
    private void notificarEstado() {
        for (ReprodutorObserver observer : List.copyOf(observers)) {
            observer.onEstadoAlterado(tocando, pausado);
        }
    }

    /**
     * Notifica observers sobre erro de audio.
     *
     * @param mensagem mensagem de erro.
     */
    private void notificarErro(String mensagem) {
        tocando = false;
        pausado = false;
        notificarEstado();
        for (ReprodutorObserver observer : List.copyOf(observers)) {
            observer.onErro(mensagem);
        }
    }
}
