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
        // Remove espaços em branco para avaliar apenas os símbolos
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
        StringBuilder log = new StringBuilder("=== ETAPA 1: ELIMINAÇÃO DE INFÉRTEIS ===\n");
        Set<String> ferteis = new HashSet<>();
        boolean novaDescoberta;
        int iteracao = 1;

        // 1. Encontrando os símbolos férteis (Conjuntos N)
        do {
            novaDescoberta = false;
            for (Regra regra : regras) {
                String nt = regra.getNaoTerminal();
                if (!ferteis.contains(nt)) {
                    String[] alternativas = regra.getSubstituicao().split("\\|");
                    for (String alt : alternativas) {
                        if (isAlternativaFertil(alt, ferteis)) {
                            ferteis.add(nt);
                            novaDescoberta = true;
                            break;
                        }
                    }
                }
            }
            log.append("Conjunto N").append(iteracao).append(" = ").append(ferteis).append("\n");
            iteracao++;
        } while (novaDescoberta);

        // 2. Reconstruindo as regras apenas com os blocos férteis
        List<Regra> regrasAtualizadas = new ArrayList<>();
        for (Regra regra : regras) {
            if (ferteis.contains(regra.getNaoTerminal())) {
                String[] alternativas = regra.getSubstituicao().split("\\|");
                List<String> alternativasFerteis = new ArrayList<>();
                for (String alt : alternativas) {
                    if (isAlternativaFertil(alt, ferteis)) {
                        alternativasFerteis.add(alt.trim());
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

        return log.toString();
    }

    public String eliminaInalcancaveis() {
        StringBuilder log = new StringBuilder("\n=== ETAPA 2: ELIMINAÇÃO DE INALCANÇÁVEIS ===\n");
        Set<String> vtAlc = new HashSet<>();
        Set<String> vnAlc = new HashSet<>();
        vnAlc.add(this.simboloInicial);

        boolean novaDescoberta;
        int iteracao = 0;
        log.append("Conjunto V").append(iteracao).append(" (Alcançáveis) = ").append(vnAlc).append("\n");

        // O laço para achar os símbolos alcançáveis
        do {
            novaDescoberta = false;
            iteracao++;

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
                            } else if (this.vt.contains(simbolo) && !vtAlc.contains(simbolo)) {
                                vtAlc.add(simbolo);
                                novaDescoberta = true;
                            }
                        }
                    }
                }
            }
            if(novaDescoberta) {
                Set<String> todosAlcancaveis = new HashSet<>(vnAlc);
                todosAlcancaveis.addAll(vtAlc);
                log.append("Conjunto V").append(iteracao).append(" (Alcançáveis) = ").append(todosAlcancaveis).append("\n");
            }
        } while (novaDescoberta);

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