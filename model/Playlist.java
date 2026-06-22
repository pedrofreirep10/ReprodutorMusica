package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Playlist baseada em lista duplamente encadeada circular.
 *
 * @author Guilherme Pereira de Rivoredo
 * @author João Batistella da Costa
 * @author Pedro Henrique Freire Pereira
 * @version 1.0
 */
public class Playlist {

    private NoMusica cabeca;
    private NoMusica atual;
    private int tamanho;
    private boolean modoRepetir;
    private boolean modoEmbaralhar;

    /**
     * Cria uma playlist vazia.
     */
    public Playlist() {
        this.cabeca = null;
        this.atual = null;
        this.tamanho = 0;
        this.modoRepetir = false;
        this.modoEmbaralhar = false;
    }

    /**
     * Adiciona uma musica ao final da lista circular.
     *
     * @param m musica que sera adicionada.
     */
    public void adicionarMusica(Musica m) {
        NoMusica novoNo = new NoMusica(m);

        if (cabeca == null) {
            // Em lista vazia, o novo no vira a cabeca.
            cabeca = novoNo;
            // Circularidade de um unico elemento: proximo aponta para ele mesmo.
            novoNo.setProximo(novoNo);
            // Circularidade de um unico elemento: anterior tambem aponta para ele mesmo.
            novoNo.setAnterior(novoNo);
            // O primeiro item adicionado tambem passa a ser a faixa atual.
            atual = novoNo;
        } else {
            // Em uma lista circular, o ultimo no e sempre o anterior da cabeca.
            NoMusica ultimo = cabeca.getAnterior();
            // O antigo ultimo aponta para o novo no, inserindo-o ao final da sequencia.
            ultimo.setProximo(novoNo);
            // O novo no aponta para tras para manter o encadeamento duplo.
            novoNo.setAnterior(ultimo);
            // O novo no aponta para a cabeca, fechando o circulo no sentido proximo.
            novoNo.setProximo(cabeca);
            // A cabeca aponta para o novo no como seu anterior, fechando o circulo no sentido anterior.
            cabeca.setAnterior(novoNo);
        }

        tamanho++;
    }

    /**
     * Remove a primeira musica encontrada pelo titulo.
     *
     * @param titulo titulo da musica que sera removida.
     */
    public void removerMusica(String titulo) {
        if (cabeca == null || titulo == null) {
            return;
        }

        NoMusica cursor = cabeca;
        do {
            if (titulo.equalsIgnoreCase(cursor.getMusica().getTitulo())) {
                removerNo(cursor);
                return;
            }
            // Avanca um no; como a lista e circular, o laco termina quando volta para a cabeca.
            cursor = cursor.getProximo();
        } while (cursor != cabeca);
    }

    /**
     * Remove a musica selecionada pela interface.
     *
     * @param musica musica que sera removida.
     */
    public void removerMusica(Musica musica) {
        NoMusica no = localizarNo(musica);
        if (no != null) {
            removerNo(no);
        }
    }

    /**
     * Avanca a faixa atual para o proximo no.
     *
     * @return no da proxima musica ou null quando a playlist esta vazia.
     */
    public NoMusica tocarProxima() {
        if (atual == null) {
            atual = cabeca;
        } else {
            atual = atual.getProximo();
        }
        return atual;
    }

    /**
     * Move a faixa atual para o no anterior.
     *
     * @return no da musica anterior ou null quando a playlist esta vazia.
     */
    public NoMusica tocarAnterior() {
        if (atual == null) {
            atual = cabeca;
        } else {
            atual = atual.getAnterior();
        }
        return atual;
    }

    /**
     * Embaralha os nos da playlist usando o algoritmo Fisher-Yates.
     */
    public void embaralhar() {
        if (tamanho < 2) {
            modoEmbaralhar = !modoEmbaralhar;
            return;
        }

        List<NoMusica> nos = obterTodosOsNos();
        Random random = new Random();

        // Comeca no ultimo indice, pois cada iteracao fixa uma posicao final da lista.
        for (int i = nos.size() - 1; i > 0; i--) {
            // Sorteia um indice entre 0 e i, inclusive, como exige Fisher-Yates.
            int j = random.nextInt(i + 1);
            // Guarda temporariamente o no da posicao i.
            NoMusica temporario = nos.get(i);
            // Move para i o no sorteado em j.
            nos.set(i, nos.get(j));
            // Coloca na posicao j o no que estava em i, completando a troca.
            nos.set(j, temporario);
        }

        // Reconecta os ponteiros proximo/anterior conforme a nova ordem embaralhada.
        reconstruirCircularidade(nos);
        modoEmbaralhar = true;
    }

    /**
     * Retorna todas as musicas na ordem atual da playlist.
     *
     * @return lista de musicas.
     */
    public List<Musica> obterTodas() {
        List<Musica> musicas = new ArrayList<>();
        if (cabeca == null) {
            return musicas;
        }

        NoMusica cursor = cabeca;
        do {
            musicas.add(cursor.getMusica());
            cursor = cursor.getProximo();
        } while (cursor != cabeca);

        return musicas;
    }

    /**
     * Verifica se a playlist nao possui musicas.
     *
     * @return true quando a playlist esta vazia; false caso contrario.
     */
    public boolean isEmpty() {
        return tamanho == 0;
    }

    /**
     * Retorna o no cabeca da lista.
     *
     * @return cabeca da lista ou null quando vazia.
     */
    public NoMusica getCabeca() {
        return cabeca;
    }

    /**
     * Retorna o no atual da reproducao.
     *
     * @return no atual ou null quando nenhuma musica foi selecionada.
     */
    public NoMusica getAtual() {
        return atual;
    }

    /**
     * Define o no atual da playlist.
     *
     * @param atual no que passara a ser a faixa atual.
     */
    public void setAtual(NoMusica atual) {
        this.atual = atual;
    }

    /**
     * Retorna a quantidade de musicas cadastradas.
     *
     * @return tamanho da playlist.
     */
    public int getTamanho() {
        return tamanho;
    }

    /**
     * Indica se a faixa atual deve repetir ao terminar.
     *
     * @return true quando o modo repetir esta ativo.
     */
    public boolean isModoRepetir() {
        return modoRepetir;
    }

    /**
     * Ativa ou desativa o modo repetir.
     *
     * @param modoRepetir true para repetir a faixa atual.
     */
    public void setModoRepetir(boolean modoRepetir) {
        this.modoRepetir = modoRepetir;
    }

    /**
     * Indica se o modo embaralhar esta ativo.
     *
     * @return true quando a playlist foi embaralhada.
     */
    public boolean isModoEmbaralhar() {
        return modoEmbaralhar;
    }

    /**
     * Ativa ou desativa o estado visual/logico de embaralhamento.
     *
     * @param modoEmbaralhar true quando a playlist esta em modo embaralhar.
     */
    public void setModoEmbaralhar(boolean modoEmbaralhar) {
        this.modoEmbaralhar = modoEmbaralhar;
    }

    /**
     * Localiza o no que contem a musica informada.
     *
     * @param musica musica procurada.
     * @return no encontrado ou null quando a musica nao esta na playlist.
     */
    public NoMusica localizarNo(Musica musica) {
        if (cabeca == null || musica == null) {
            return null;
        }

        NoMusica cursor = cabeca;
        do {
            if (cursor.getMusica() == musica || cursor.getMusica().equals(musica)) {
                return cursor;
            }
            cursor = cursor.getProximo();
        } while (cursor != cabeca);

        return null;
    }

    /**
     * Remove um no especifico preservando a circularidade.
     *
     * @param no no que sera removido.
     */
    private void removerNo(NoMusica no) {
        if (tamanho == 1) {
            // Ao remover o unico elemento, nao resta circularidade a preservar.
            cabeca = null;
            atual = null;
        } else {
            // Captura vizinhos antes de religar os ponteiros.
            NoMusica anterior = no.getAnterior();
            NoMusica proximo = no.getProximo();
            // O anterior passa a apontar para o proximo, pulando o no removido.
            anterior.setProximo(proximo);
            // O proximo passa a apontar para o anterior, mantendo a ligacao dupla.
            proximo.setAnterior(anterior);
            // Se a cabeca foi removida, a nova cabeca deve ser o proximo no valido.
            if (no == cabeca) {
                cabeca = proximo;
            }
            // Se a faixa atual foi removida, a reproducao avanca para uma faixa ainda existente.
            if (no == atual) {
                atual = proximo;
            }
        }

        tamanho--;
        // Desconecta o no removido para evitar referencias acidentais.
        no.setProximo(null);
        no.setAnterior(null);
    }

    /**
     * Coleta todos os nos da lista circular em uma lista auxiliar.
     *
     * @return lista de nos na ordem atual.
     */
    private List<NoMusica> obterTodosOsNos() {
        List<NoMusica> nos = new ArrayList<>();
        if (cabeca == null) {
            return nos;
        }

        NoMusica cursor = cabeca;
        do {
            nos.add(cursor);
            cursor = cursor.getProximo();
        } while (cursor != cabeca);

        return nos;
    }

    /**
     * Reconstrucao dos ponteiros circulares a partir de uma lista linear de nos.
     *
     * @param nos nos que serao religados na ordem recebida.
     */
    private void reconstruirCircularidade(List<NoMusica> nos) {
        cabeca = nos.get(0);

        for (int i = 0; i < nos.size(); i++) {
            // O indice seguinte usa modulo para fazer o ultimo apontar de volta para o primeiro.
            NoMusica proximo = nos.get((i + 1) % nos.size());
            // O indice anterior usa modulo deslocado para fazer o primeiro apontar de volta para o ultimo.
            NoMusica anterior = nos.get((i - 1 + nos.size()) % nos.size());
            // Religa o ponteiro para frente conforme a ordem calculada.
            nos.get(i).setProximo(proximo);
            // Religa o ponteiro para tras conforme a ordem calculada.
            nos.get(i).setAnterior(anterior);
        }
    }
}
