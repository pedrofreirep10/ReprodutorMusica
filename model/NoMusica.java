package model;

/**
 * No da lista duplamente encadeada circular de musicas.
 *
 * @author Guilherme Pereira de Rivoredo
 * @author João Batistella da Costa
 * @author Pedro Henrique Freire Pereira
 * @version 1.0
 */
public class NoMusica {

    private final Musica musica;
    private NoMusica proximo;
    private NoMusica anterior;

    /**
     * Cria um no contendo uma musica.
     *
     * @param musica musica armazenada no no.
     */
    public NoMusica(Musica musica) {
        this.musica = musica;
    }

    /**
     * Retorna a musica do no.
     *
     * @return musica armazenada.
     */
    public Musica getMusica() {
        return musica;
    }

    /**
     * Retorna o proximo no da lista.
     *
     * @return proximo no.
     */
    public NoMusica getProximo() {
        return proximo;
    }

    /**
     * Define o proximo no da lista.
     *
     * @param proximo no que vira depois deste.
     */
    public void setProximo(NoMusica proximo) {
        this.proximo = proximo;
    }

    /**
     * Retorna o no anterior da lista.
     *
     * @return no anterior.
     */
    public NoMusica getAnterior() {
        return anterior;
    }

    /**
     * Define o no anterior da lista.
     *
     * @param anterior no que vira antes deste.
     */
    public void setAnterior(NoMusica anterior) {
        this.anterior = anterior;
    }
}
