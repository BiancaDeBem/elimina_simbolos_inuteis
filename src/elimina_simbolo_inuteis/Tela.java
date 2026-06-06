package elimina_simbolo_inuteis;

import java.awt.*;
import javax.swing.*;
import java.util.*;

public class Tela extends JFrame {
	private JTextArea campoA;
    private JTextArea campoB;
    private JButton analisarButton;
    private JButton limparButton;
    
	public static void imprime(Gramatica glc) {
		System.out.println("=== GRAMÁTICA ORIGINAL ===");
        glc.imprimirRegras();
        
        System.out.println("\n=== EXECUTANDO ELIMINAÇÃO ===");
        glc.eliminaInferteis();
        
        System.out.println("\n=== EXECUTANDO ELIMINAÇÃO DE INALCANÇÁVEIS ===");
        glc.eliminaInalcancaveis();
        
        System.out.println("\n=== GRAMÁTICA RESULTANTE ===");
        glc.imprimirRegras();
	}
	
	public void tela() {
		setTitle("Reconhecedor de Linguagem Regular");
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    setSize(760, 520);
	    setLocationRelativeTo(null);
	    setLayout(new BorderLayout(8, 8));
	
	    campoA = new JTextArea(8, 40);
	    campoA.setLineWrap(true);
	    campoA.setWrapStyleWord(true);
	    JScrollPane scrollA = new JScrollPane(campoA);
	    scrollA.setBorder(BorderFactory.createTitledBorder("Gramática 2 Valores Inférteis"));
	
	    campoB = new JTextArea(10, 40);
	    campoB.setEditable(false);
	    campoB.setLineWrap(true);
	    campoB.setWrapStyleWord(true);
	    JScrollPane scrollB = new JScrollPane(campoB);
	    scrollB.setBorder(BorderFactory.createTitledBorder("Gramática Variável Infértil e Inalcançável"));
	
	    JPanel botoesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
	    analisarButton = new JButton("Analisar");
	    limparButton = new JButton("Limpar");
	    botoesPanel.add(analisarButton);
	    botoesPanel.add(limparButton);
	
	    JPanel camposPanel = new JPanel();
	    camposPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
	    camposPanel.setLayout(new BoxLayout(camposPanel, BoxLayout.Y_AXIS));
	    camposPanel.add(scrollA);
	    camposPanel.add(Box.createVerticalStrut(8));
	    camposPanel.add(scrollB);
	
	    add(camposPanel, BorderLayout.CENTER);
	    add(botoesPanel, BorderLayout.SOUTH);
	
	    limparButton.addActionListener(e -> limparCampos());
	    analisarButton.addActionListener(e -> analisarEntrada());
	}
	
	private void limparCampos() {
        campoA.setText("");
        campoB.setText("");
        campoA.requestFocus();
    }

	public static void main(String[] args) {
		Set<String> vn = new HashSet<>(Arrays.asList("S", "A", "B"));
        Set<String> vt = new HashSet<>(Arrays.asList("a", "ε"));
        String simboloInicial = "S"; 
        
        List<Regra> regras = new ArrayList<>();
        
        regras.add(new Regra("S", "ASB | BSA | SS | aS | ε"));
        regras.add(new Regra("A", "AB | B"));
        regras.add(new Regra("B", "BA | A"));
        
        Gramatica glc = new Gramatica(vn, vt, regras, simboloInicial);
        
        //imprime(glc);
        
        Set<String> vn1 = new HashSet<>(Arrays.asList("S", "A", "B", "C", "D"));
        Set<String> vt1 = new HashSet<>(Arrays.asList("a", "c", "b", "d"));
        String simboloInicial1 = "S"; 
        
        List<Regra> regras1 = new ArrayList<>();
        
        regras1.add(new Regra("S", "aA"));
        regras1.add(new Regra("A", "a | bB"));
        regras1.add(new Regra("B", "b | dD"));
        regras1.add(new Regra("C", "cC | c"));
        regras1.add(new Regra("D", "dD"));
        
        Gramatica glc1 = new Gramatica(vn1, vt1, regras1, simboloInicial1);
        
        imprime(glc1);
    }

}
