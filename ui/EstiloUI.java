package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.border.Border;

/**
 * Centraliza constantes visuais usadas pela interface Swing.
 *
 * @author Guilherme Pereira de Rivoredo
 * @author João Batistella da Costa
 * @author Pedro Henrique Freire Pereira
 * @version 1.0
 */
public final class EstiloUI {

    public static final Font FONTE_TITULO = new Font("SansSerif", Font.BOLD, 22);
    public static final Font FONTE_SUBTITULO = new Font("SansSerif", Font.PLAIN, 15);
    public static final Font FONTE_PADRAO = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font FONTE_BOTAO = new Font("SansSerif", Font.BOLD, 18);
    public static final Color COR_FUNDO = new Color(245, 247, 250);
    public static final Color COR_PAINEL = new Color(255, 255, 255);
    public static final Color COR_TEXTO = new Color(30, 41, 59);
    public static final Color COR_TEXTO_SECUNDARIO = new Color(100, 116, 139);
    public static final Color COR_DESTAQUE = new Color(37, 99, 235);
    public static final Color COR_BORDA = new Color(203, 213, 225);
    public static final Color COR_ERRO = new Color(185, 28, 28);
    public static final Dimension DIMENSAO_BOTAO_CONTROLE = new Dimension(54, 42);
    public static final Dimension DIMENSAO_BOTAO_ACAO = new Dimension(170, 36);
    public static final Dimension DIMENSAO_JANELA = new Dimension(940, 560);
    public static final Dimension DIMENSAO_PLAYER = new Dimension(340, 520);
    public static final Border BORDA_PADRAO = BorderFactory.createLineBorder(COR_BORDA, 1);
    public static final Border BORDA_PAINEL = BorderFactory.createCompoundBorder(
            BORDA_PADRAO,
            BorderFactory.createEmptyBorder(18, 18, 18, 18));

    /**
     * Impede instanciacao da classe de constantes.
     */
    private EstiloUI() {
    }
}
