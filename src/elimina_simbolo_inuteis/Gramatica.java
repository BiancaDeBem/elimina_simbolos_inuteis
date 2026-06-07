package elimina_simbolo_inuteis;
import java.util.*;

public class Gramatica {
    Set<String> vn;
    Set<String> vt;
    List<Regra> regras;
    String simboloInicial;

    public Gramatica(Set<String> vn, Set<String> vt, List<Regra> regras, String simboloInicial) {
        this.vn = vn;
        this.vt = vt;
        this.regras = regras;
        this.simboloInicial = simboloInicial;
    }

    private boolean isAlternativaFertil(String alternativa, Set<String> ferteis) {
        String altLimpa = alternativa.replaceAll("\\s+", "");
        if (altLimpa.equals("ε")) return true;

        for (int i = 0; i < altLimpa.length(); i++) {
            String simbolo = String.valueOf(altLimpa.charAt(i));
            if (!vt.contains(simbolo) && !ferteis.contains(simbolo)) {
                return false;
            }
        }
        return true;
    }

    public String eliminaInferteis() {
        StringBuilder log = new StringBuilder("=== ETAPA 1: ELIMINAÇÃO DE SÍMBOLOS INFÉRTEIS ===\n");
        log.append("Buscando variáveis que conseguem gerar terminais (direta ou indiretamente)...\n");
        
        Set<String> ferteis = new HashSet<>();
        boolean novaDescoberta;
        int iteracao = 1;

        // 1. Encontrando os símbolos férteis com rastreio detalhado
        do {
            novaDescoberta = false;
            log.append("\n-> Iteração ").append(iteracao).append(":\n");
            
            for (Regra regra : regras) {
                String nt = regra.getNaoTerminal();
                if (!ferteis.contains(nt)) {
                    String[] alternativas = regra.getSubstituicao().split("\\|");
                    for (String alt : alternativas) {
                        if (isAlternativaFertil(alt, ferteis)) {
                            ferteis.add(nt);
                            novaDescoberta = true;
                            log.append("   [+] '").append(nt).append("' é FÉRTIL (motivo: derivação válida através de '").append(alt.trim()).append("')\n");
                            break;
                        }
                    }
                }
            }
            log.append("   Conjunto N").append(iteracao).append(" = ").append(ferteis).append("\n");
            iteracao++;
        } while (novaDescoberta);

        // Identificando quem ficou de fora (os inférteis de fato)
        Set<String> inferteis = new HashSet<>(this.vn);
        inferteis.removeAll(ferteis);
        if (inferteis.isEmpty()) {
            log.append("\n-> Nenhum símbolo infértil foi encontrado.\n");
        } else {
            log.append("\n-> Símbolos INFÉRTEIS descartados: ").append(inferteis).append("\n");
        }

        // 2. Reconstruindo as regras apenas com os blocos férteis
        List<Regra> regrasAtualizadas = new ArrayList<>();
        for (Regra regra : regras) {
            if (ferteis.contains(regra.getNaoTerminal())) {
                String[] alternativas = regra.getSubstituicao().split("\\|");
                List<String> alternativasFerteis = new ArrayList<>();
                for (String alt : alternativas) {
                    if (isAlternativaFertil(alt, ferteis)) {
                        alternativasFerteis.add(alt.trim());
                    } else {
                        log.append("   [-] Alternativa '").append(alt.trim()).append("' removida da regra de '").append(regra.getNaoTerminal()).append("' (depende de inférteis).\n");
                    }
                }
                if (!alternativasFerteis.isEmpty()) {
                    String novaSubstituicao = String.join(" | ", alternativasFerteis);
                    regrasAtualizadas.add(new Regra(regra.getNaoTerminal(), novaSubstituicao));
                }
            }
        }
        
        this.regras = regrasAtualizadas;
        this.vn.retainAll(ferteis);

        log.append("\n[Gramática Intermediária após Etapa 1]\n");
        if(regras.isEmpty()) log.append("(Vazia)\n");
        else log.append(getGramaticaFormatada()).append("\n");

        return log.toString();
    }

    public String eliminaInalcancaveis() {
        StringBuilder log = new StringBuilder("=== ETAPA 2: ELIMINAÇÃO DE SÍMBOLOS INALCANÇÁVEIS ===\n");
        
        if (!this.vn.contains(this.simboloInicial)) {
            log.append("O símbolo inicial '").append(this.simboloInicial).append("' foi removido na Etapa 1. A gramática resultante é vazia.\n");
            this.regras.clear();
            return log.toString();
        }

        log.append("Partindo do símbolo inicial '").append(this.simboloInicial).append("' para descobrir o que é alcançável...\n\n");
        
        Set<String> vtAlc = new HashSet<>();
        Set<String> vnAlc = new HashSet<>();
        vnAlc.add(this.simboloInicial);

        boolean novaDescoberta;
        int iteracao = 0;
        log.append("-> Iteração ").append(iteracao).append(":\n");
        log.append("   Conjunto V").append(iteracao).append(" (Alcançáveis) = ").append(vnAlc).append("\n");

        // Encontrando símbolos alcançáveis com rastreio detalhado
        do {
            novaDescoberta = false;
            iteracao++;
            log.append("\n-> Iteração ").append(iteracao).append(":\n");

            for (Regra regra : regras) {
                if (vnAlc.contains(regra.getNaoTerminal())) {
                    String[] alternativas = regra.getSubstituicao().split("\\|");
                    for (String alt : alternativas) {
                        String altLimpa = alt.replaceAll("\\s+", "");
                        if (altLimpa.equals("ε")) continue;

                        for (int i = 0; i < altLimpa.length(); i++) {
                            String simbolo = String.valueOf(altLimpa.charAt(i));
                            if (this.vn.contains(simbolo) && !vnAlc.contains(simbolo)) {
                                vnAlc.add(simbolo);
                                novaDescoberta = true;
                                log.append("   [+] Alcançou Variável '").append(simbolo).append("' através da regra '").append(regra.getNaoTerminal()).append(" -> ").append(alt.trim()).append("'\n");
                            } else if (this.vt.contains(simbolo) && !vtAlc.contains(simbolo)) {
                                vtAlc.add(simbolo);
                                novaDescoberta = true;
                                log.append("   [+] Alcançou Terminal '").append(simbolo).append("' através da regra '").append(regra.getNaoTerminal()).append(" -> ").append(alt.trim()).append("'\n");
                            }
                        }
                    }
                }
            }
            if(novaDescoberta) {
                Set<String> todosAlcancaveis = new HashSet<>(vnAlc);
                todosAlcancaveis.addAll(vtAlc);
                log.append("   Conjunto V").append(iteracao).append(" = ").append(todosAlcancaveis).append("\n");
            } else {
                log.append("   Nenhuma nova descoberta nesta iteração. Busca encerrada.\n");
            }
        } while (novaDescoberta);

        // Identificando quem ficou isolado
        Set<String> vnInalcancaveis = new HashSet<>(this.vn);
        vnInalcancaveis.removeAll(vnAlc);
        if (vnInalcancaveis.isEmpty()) {
            log.append("\n-> Nenhuma variável inalcançável foi encontrada.\n");
        } else {
            log.append("\n-> Variáveis INALCANÇÁVEIS descartadas: ").append(vnInalcancaveis).append("\n");
        }

        this.vn.retainAll(vnAlc);
        this.vt.retainAll(vtAlc);

        List<Regra> regrasAtualizadas = new ArrayList<>();
        for (Regra regra : regras) {
            if (this.vn.contains(regra.getNaoTerminal())) {
                regrasAtualizadas.add(regra);
            }
        }
        this.regras = regrasAtualizadas;

        return log.toString();
    }

    public String getGramaticaFormatada() {
        StringBuilder sb = new StringBuilder();
        for (Regra r : regras) {
            sb.append(r.toString()).append("\n");
        }
        return sb.toString();
    }
}