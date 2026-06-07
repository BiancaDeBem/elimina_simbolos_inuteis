package elimina_simbolo_inuteis;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Tela extends JFrame {

    private JTextField txtSimboloInicial;
    private JTextArea txtProducoes;
    private JTextArea txtSaida;
    private JComboBox<String> comboGramaticas;
    private JButton btnAnalisar;
    private JButton btnLimpar;

    public Tela() {
        setTitle("Simplificador de Gramáticas - Símbolos Inúteis");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 650);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(1, 2, 10, 10));

        // PAINEL DE ENTRADA (ESQUERDA)
        JPanel painelEntrada = new JPanel(new BorderLayout(5, 5));
        painelEntrada.setBorder(BorderFactory.createTitledBorder("Entrada de Dados"));

        // Configuração do topo do painel de entrada (Seleção e Símbolo Inicial)
        JPanel painelTopo = new JPanel(new GridLayout(2, 1, 5, 5));
        
        JPanel linhaSelecao = new JPanel(new FlowLayout(FlowLayout.LEFT));
        linhaSelecao.add(new JLabel("Pré-carregadas: "));
        String[] opcoes = {
            "Customizada (Digitar manualmente)",
            "1. Exemplo do Trabalho (Múltiplas Remoções)",
            "2. Com Símbolo Infértil (Loop de C)",
            "3. Com Símbolo Inalcançável (Variável A isolada)"
        };
        comboGramaticas = new JComboBox<>(opcoes);
        linhaSelecao.add(comboGramaticas);
        
        JPanel linhaInicial = new JPanel(new FlowLayout(FlowLayout.LEFT));
        linhaInicial.add(new JLabel("Símbolo Inicial: "));
        txtSimboloInicial = new JTextField("S", 5);
        linhaInicial.add(txtSimboloInicial);
        
        painelTopo.add(linhaSelecao);
        painelTopo.add(linhaInicial);

        // Área de texto para as produções
        txtProducoes = new JTextArea();
        txtProducoes.setFont(new Font("Monospaced", Font.PLAIN, 16));
        JScrollPane scrollProducoes = new JScrollPane(txtProducoes);
        scrollProducoes.setBorder(BorderFactory.createTitledBorder("Produções (Ex: S -> A B | C)"));

        // Painel de botões de ação
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnLimpar = new JButton("Limpar");
        btnAnalisar = new JButton("Simplificar Gramática");
        painelBotoes.add(btnLimpar);
        painelBotoes.add(btnAnalisar);

        painelEntrada.add(painelTopo, BorderLayout.NORTH);
        painelEntrada.add(scrollProducoes, BorderLayout.CENTER);
        painelEntrada.add(painelBotoes, BorderLayout.SOUTH);

        // PAINEL DE SAÍDA (DIREITA)
        txtSaida = new JTextArea();
        txtSaida.setEditable(false);
        txtSaida.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollSaida = new JScrollPane(txtSaida);
        scrollSaida.setBorder(BorderFactory.createTitledBorder("Console de Processamento (Passo a Passo)"));

        add(painelEntrada);
        add(scrollSaida);

        // CONFIGURAÇÃO DOS EVENTOS / LISTENERS
        
        // Listener para carregar as gramáticas predefinidas
        comboGramaticas.addActionListener(e -> carregarGramaticaPredefinida());

        btnLimpar.addActionListener(e -> {
            comboGramaticas.setSelectedIndex(0);
            txtProducoes.setText("");
            txtSaida.setText("");
            txtSimboloInicial.setText("");
        });

        btnAnalisar.addActionListener(e -> processarGramatica());

        // Carrega o primeiro exemplo por padrão ao iniciar a tela
        comboGramaticas.setSelectedIndex(1);
    }

    private void carregarGramaticaPredefinida() {
        int index = comboGramaticas.getSelectedIndex();
        switch (index) {
            case 1: // Exemplo do Trabalho
                txtSimboloInicial.setText("S");
                txtProducoes.setText("S -> A B | C\nA -> a A | a\nB -> b\nC -> c D\nD -> d D\nE -> e");
                break;
            case 2: // Símbolo Infértil
                txtSimboloInicial.setText("S");
                txtProducoes.setText("S -> A | B\nA -> a\nB -> b C\nC -> c C");
                break;
            case 3: // Símbolo Inalcançável
                txtSimboloInicial.setText("S");
                txtProducoes.setText("S -> a b\nA -> b");
                break;
            default: // Customizada (Não altera o texto atual de forma drástica ou limpa)
                break;
        }
    }

    private void processarGramatica() {
        String inicial = txtSimboloInicial.getText().trim();
        if (inicial.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o Símbolo Inicial!");
            return;
        }

        Set<String> vn = new HashSet<>();
        Set<String> vt = new HashSet<>();
        List<Regra> regras = new ArrayList<>();
        vn.add(inicial);

        String[] linhas = txtProducoes.getText().split("\n");
        for (String linha : linhas) {
            if (!linha.contains("->")) continue;
            String[] partes = linha.split("->");
            String lhs = partes[0].trim();
            String rhs = partes[1].trim();

            regras.add(new Regra(lhs, rhs));
            vn.add(lhs);

            String rhsLimpo = rhs.replaceAll("\\s+", "");
            for (char c : rhsLimpo.toCharArray()) {
                if (c == '|') continue;
                if (Character.isUpperCase(c)) {
                    vn.add(String.valueOf(c));
                } else if (c != 'ε') {
                    vt.add(String.valueOf(c));
                }
            }
        }

        Gramatica glc = new Gramatica(vn, vt, regras, inicial);

        StringBuilder logFinal = new StringBuilder();
        logFinal.append("--- GRAMÁTICA ORIGINAL ---\n");
        logFinal.append(glc.getGramaticaFormatada()).append("\n");

        logFinal.append(glc.eliminaInferteis());
        logFinal.append(glc.eliminaInalcancaveis());

        logFinal.append("\n=== RESULTADO: GRAMÁTICA SIMPLIFICADA ===\n");
        if (glc.getGramaticaFormatada().isEmpty()) {
            logFinal.append("(Gramática Vazia ou Símbolo Inicial Inútil)\n");
        } else {
            logFinal.append(glc.getGramaticaFormatada());
        }

        txtSaida.setText(logFinal.toString());
        txtSaida.setCaretPosition(0); 
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Tela().setVisible(true);
        });
    }
}