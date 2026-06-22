package controller;

import model.Musica;
import model.NoMusica;
import model.Playlist;
import player.Reprodutor;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Ponte unica entre a interface grafica e o modelo da playlist.
 *
 * @author Guilherme Pereira de Rivoredo
 * @author João Batistella da Costa
 * @author Pedro Henrique Freire Pereira
 * @version 1.0
 */
public class PlaylistController {

    private final Playlist playlist;
    private final Reprodutor reprodutor;
    private final List<Consumer<List<Musica>>> playlistListeners;

    /**
     * Cria o controller com as dependencias principais da aplicacao.
     *
     * @param playlist playlist manipulada pelo controller.
     * @param reprodutor reprodutor que executa comandos de audio.
     */
    public PlaylistController(Playlist playlist, Reprodutor reprodutor) {
        this.playlist = playlist;
        this.reprodutor = reprodutor;
        this.playlistListeners = new ArrayList<>();
    }

    /**
     * Adiciona uma musica ao modelo.
     *
     * @param m musica que sera adicionada.
     */
    public void adicionarMusica(Musica m) {
        playlist.adicionarMusica(m);
        notificarPlaylist();
    }

    /**
     * Cria uma musica a partir de um arquivo MP3 e adiciona na playlist.
     *
     * @param arquivo arquivo MP3 escolhido pelo usuario.
     * @throws IOException quando o arquivo e invalido, inexistente ou nao e MP3.
     */
    public void adicionarArquivo(File arquivo) throws IOException {
        if (arquivo == null) {
            throw new IOException("Nenhum arquivo foi informado.");
        }
        if (!arquivo.exists() || !arquivo.isFile()) {
            throw new IOException("Arquivo inexistente ou invalido: " + arquivo.getAbsolutePath());
        }
        if (!arquivo.getName().toLowerCase(Locale.ROOT).endsWith(".mp3")) {
            throw new IOException("Selecione apenas arquivos MP3.");
        }

        String titulo = removerExtensao(arquivo.getName());
        Musica musica = new Musica(titulo, "Artista desconhecido", 0, arquivo.getAbsolutePath());
        adicionarMusica(musica);
    }

    /**
     * Remove uma musica da playlist pelo titulo.
     *
     * @param titulo titulo da musica que sera removida.
     */
    public void removerMusica(String titulo) {
        Musica atual = reprodutor.getMusicaAtual();
        if (atual != null && atual.getTitulo().equalsIgnoreCase(titulo)) {
            reprodutor.stop();
        }
        playlist.removerMusica(titulo);
        notificarPlaylist();
    }

    /**
     * Remove uma musica especifica da playlist.
     *
     * @param musica musica que sera removida.
     */
    public void removerMusica(Musica musica) {
        if (musica == null) {
            return;
        }

        Musica atual = reprodutor.getMusicaAtual();
        if (musica.equals(atual)) {
            reprodutor.stop();
        }
        playlist.removerMusica(musica);
        notificarPlaylist();
    }

    /**
     * Obtem todas as musicas na ordem atual da playlist.
     *
     * @return lista de musicas.
     */
    public List<Musica> obterTodas() {
        return playlist.obterTodas();
    }

    /**
     * Inicia ou retoma a reproducao.
     */
    public void play() {
        reprodutor.play();
    }

    /**
     * Pausa a reproducao.
     */
    public void pause() {
        reprodutor.pause();
    }

    /**
     * Para a reproducao.
     */
    public void stop() {
        reprodutor.stop();
    }

    /**
     * Avanca para a proxima faixa.
     */
    public void proximaFaixa() {
        reprodutor.proximaFaixa();
    }

    /**
     * Volta para a faixa anterior.
     */
    public void faixaAnterior() {
        reprodutor.faixaAnterior();
    }

    /**
     * Embaralha a playlist com Fisher-Yates.
     */
    public void embaralhar() {
        playlist.embaralhar();
        notificarPlaylist();
    }

    /**
     * Alterna o modo repetir.
     *
     * @return estado atualizado do modo repetir.
     */
    public boolean alternarRepetir() {
        playlist.setModoRepetir(!playlist.isModoRepetir());
        return playlist.isModoRepetir();
    }

    /**
     * Alterna o modo embaralhar executando o embaralhamento.
     *
     * @return estado atualizado do modo embaralhar.
     */
    public boolean alternarEmbaralhar() {
        if (playlist.isModoEmbaralhar()) {
            playlist.setModoEmbaralhar(false);
        } else {
            embaralhar();
        }
        return playlist.isModoEmbaralhar();
    }

    /**
     * Toca diretamente uma musica selecionada na interface.
     *
     * @param musica musica selecionada.
     */
    public void tocarSelecionada(Musica musica) {
        NoMusica no = playlist.localizarNo(musica);
        if (no != null) {
            reprodutor.playMusica(no);
        }
    }

    /**
     * Define o volume solicitado pela interface.
     *
     * @param volume volume entre 0 e 100.
     */
    public void definirVolume(int volume) {
        reprodutor.setVolume(volume);
    }

    /**
     * Adiciona um listener para atualizacoes da playlist.
     *
     * @param listener consumidor que recebe uma copia da lista de musicas.
     */
    public void adicionarPlaylistListener(Consumer<List<Musica>> listener) {
        if (listener != null && !playlistListeners.contains(listener)) {
            playlistListeners.add(listener);
        }
    }

    /**
     * Remove um listener de atualizacoes da playlist.
     *
     * @param listener listener que sera removido.
     */
    public void removerPlaylistListener(Consumer<List<Musica>> listener) {
        playlistListeners.remove(listener);
    }

    /**
     * Informa se a playlist esta vazia.
     *
     * @return true quando nao ha musicas na playlist.
     */
    public boolean isPlaylistVazia() {
        return playlist.isEmpty();
    }

    /**
     * Remove a extensao do nome do arquivo.
     *
     * @param nomeArquivo nome do arquivo.
     * @return nome sem a extensao.
     */
    private String removerExtensao(String nomeArquivo) {
        int ponto = nomeArquivo.lastIndexOf('.');
        return ponto > 0 ? nomeArquivo.substring(0, ponto) : nomeArquivo;
    }

    /**
     * Notifica listeners com uma copia defensiva da playlist atual.
     */
    private void notificarPlaylist() {
        List<Musica> copia = List.copyOf(playlist.obterTodas());
        for (Consumer<List<Musica>> listener : List.copyOf(playlistListeners)) {
            listener.accept(copia);
        }
    }
}
