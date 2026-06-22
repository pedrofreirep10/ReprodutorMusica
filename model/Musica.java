package model;

import java.util.Objects;

/**
 * Representa uma musica MP3 armazenada na playlist.
 *
 * @author Guilherme Pereira de Rivoredo
 * @author João Batistella da Costa
 * @author Pedro Henrique Freire Pereira
 * @version 1.0
 */
public class Musica {

    private final String titulo;
    private final String artista;
    private final int duracao;
    private final String caminhoArquivo;

    /**
     * Cria uma musica com todos os dados necessarios para exibicao e reproducao.
     *
     * @param titulo titulo da musica.
     * @param artista artista da musica.
     * @param duracao duracao em segundos; use zero quando nao for conhecida.
     * @param caminhoArquivo caminho absoluto do arquivo MP3 no disco.
     */
    public Musica(String titulo, String artista, int duracao, String caminhoArquivo) {
        this.titulo = titulo;
        this.artista = artista;
        this.duracao = duracao;
        this.caminhoArquivo = caminhoArquivo;
    }

    /**
     * Retorna o titulo da musica.
     *
     * @return titulo da musica.
     */
    public String getTitulo() {
        return titulo;
    }

    /**
     * Retorna o artista da musica.
     *
     * @return artista da musica.
     */
    public String getArtista() {
        return artista;
    }

    /**
     * Retorna a duracao da musica em segundos.
     *
     * @return duracao em segundos.
     */
    public int getDuracao() {
        return duracao;
    }

    /**
     * Retorna o caminho absoluto do arquivo MP3.
     *
     * @return caminho absoluto do arquivo.
     */
    public String getCaminhoArquivo() {
        return caminhoArquivo;
    }

    /**
     * Retorna a representacao textual usada na JList.
     *
     * @return texto no formato titulo - artista.
     */
    @Override
    public String toString() {
        return titulo + " - " + artista;
    }

    /**
     * Compara musicas pelo caminho do arquivo, que identifica a faixa no disco.
     *
     * @param objeto outro objeto para comparacao.
     * @return true quando o objeto representa o mesmo arquivo.
     */
    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }
        if (!(objeto instanceof Musica musica)) {
            return false;
        }
        return Objects.equals(caminhoArquivo, musica.caminhoArquivo);
    }

    /**
     * Calcula o hash pelo caminho do arquivo.
     *
     * @return codigo hash da musica.
     */
    @Override
    public int hashCode() {
        return Objects.hash(caminhoArquivo);
    }
}
