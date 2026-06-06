package elimina_simbolo_inuteis;
import java.util.*;

public class Gramatica {
	Set<String> vn;
	Set<String> vt;
	List<Regra> regras;
	List<Regra> regras_atualizadas;
	String simboloInicial;
	
	public Gramatica(Set<String> vn, Set<String> vt, List<Regra> regras, String simboloInicial) {
		this.vn = vn;
		this.vt = vt;
		this.regras = regras;
		this.simboloInicial = simboloInicial;
	}
	
	private boolean isAlternativaFertil(String alternativa, Set<String> ferteis) {
        String altLimpa = alternativa.trim();
        if (altLimpa.equals("ε")) return true;

        for (int i = 0; i < altLimpa.length(); i++) {
            String simbolo = String.valueOf(altLimpa.charAt(i));
            if (!vt.contains(simbolo) && !ferteis.contains(simbolo)) {
                return false; 
            }
        }
        return true; 
    }
	
	// Método auxiliar: Valida se o lado direito tem APENAS terminais ou não-terminais já férteis
    private boolean verificaFertilidade(String ladoDireito, Set<String> ferteis) {
        // O épsilon (ε) é considerado terminal por padrão nas derivações que somem
        if (ladoDireito.equals("ε")) return true;

        // Verifica caractere por caractere da string
        for (int i = 0; i < ladoDireito.length(); i++) {
            String simbolo = String.valueOf(ladoDireito.charAt(i));
            
            // Se o símbolo atual NÃO é um terminal E TAMBÉM NÃO é um não-terminal fértil...
            if (!vt.contains(simbolo) && !ferteis.contains(simbolo)) {
                return false; // A regra falha, pois ainda depende de alguém infértil
            }
        }
        return true; 
    }
		
    public void eliminaInferteis() {
        Set<String> ferteis = new HashSet<>();
        boolean novaDescoberta;

        // 1. Encontrando os símbolos férteis (Lidando com os pipes dinamicamente)
        do {
            novaDescoberta = false;
            
            for (Regra regra : regras) {
                String nt = regra.getNaoTerminal();
                
                if (!ferteis.contains(nt)) {
                    // Quebra a string bruta em várias alternativas
                    String[] alternativas = regra.getSubstituicao().split("\\|");
                    boolean regraTemAlternativaFertil = false;
                    
                    for (String alt : alternativas) {
                        if (isAlternativaFertil(alt, ferteis)) {
                            regraTemAlternativaFertil = true;
                            break; // Se achou uma alternativa que funciona, o NT já é fértil
                        }
                    }
                    
                    if (regraTemAlternativaFertil) {
                        ferteis.add(nt);
                        novaDescoberta = true; 
                    }
                }
            }
        } while (novaDescoberta);

        System.out.println("-> Símbolos férteis encontrados (VN1): " + ferteis);

        // 2. Reconstruindo as regras apenas com os blocos férteis
        List<Regra> regrasAtualizadas = new ArrayList<>();
        
        for (Regra regra : regras) {
            if (ferteis.contains(regra.getNaoTerminal())) {
                String[] alternativas = regra.getSubstituicao().split("\\|");
                List<String> alternativasFerteis = new ArrayList<>();
                
                // Filtra as alternativas, mantendo apenas as que se provaram férteis
                for (String alt : alternativas) {
                    if (isAlternativaFertil(alt, ferteis)) {
                        alternativasFerteis.add(alt.trim());
                    }
                }
                
                // Se sobrou alguma alternativa válida, recria a string com os pipes
                if (!alternativasFerteis.isEmpty()) {
                    String novaSubstituicao = String.join(" | ", alternativasFerteis);
                    regrasAtualizadas.add(new Regra(regra.getNaoTerminal(), novaSubstituicao));
                }
            }
        }

        this.regras = regrasAtualizadas;
        this.vn.retainAll(ferteis); 
    }

    public void eliminaInalcancaveis() {
        // Inicialização conforme o pseudocódigo:
        // VT[i] <- {}
        // VN[i] <- {S}
        Set<String> vtAlc = new HashSet<>(); 
        Set<String> vnAlc = new HashSet<>(); 
        vnAlc.add(this.simboloInicial); // O Símbolo Inicial é sempre alcançável

        boolean novaDescoberta;

        // O laço REPITA ... ATÉ
        do {
            novaDescoberta = false;
            
            for (Regra regra : regras) {
                // A condição chave: A pertence a VN[i-1] (O lado esquerdo já é alcançável?)
                if (vnAlc.contains(regra.getNaoTerminal())) {
                    
                    String textoSubstituicao = regra.getSubstituicao();
                    if (textoSubstituicao == null) continue; // Nossa salvaguarda contra o NullPointerException

                    // Quebramos o pipe dinamicamente
                    String[] alternativas = textoSubstituicao.split("\\|");
                    
                    for (String alt : alternativas) {
                        String altLimpa = alt.trim();
                        
                        // O épsilon é um caso especial de terminal
                        if (altLimpa.equals("ε")) {
                            if (!vtAlc.contains("ε")) {
                                vtAlc.add("ε");
                                novaDescoberta = true;
                            }
                            continue;
                        }

                        // Varremos o lado direito para extrair X (não-terminais) e x (terminais)
                        for (int i = 0; i < altLimpa.length(); i++) {
                            String simbolo = String.valueOf(altLimpa.charAt(i));
                            
                            // É um Não-Terminal e ainda não estava no conjunto de alcançáveis?
                            if (this.vn.contains(simbolo) && !vnAlc.contains(simbolo)) {
                                vnAlc.add(simbolo);
                                novaDescoberta = true; // Equivalente matemático a adicionar um elemento na união e o tamanho do conjunto aumentar
                            }
                            // É um Terminal e ainda não estava no conjunto?
                            else if (this.vt.contains(simbolo) && !vtAlc.contains(simbolo)) {
                                vtAlc.add(simbolo);
                                novaDescoberta = true;
                            }
                        }
                    }
                }
            }
        // Condição de parada: (VN[i] = VN[i-1]) E (VT[i] = VT[i-1]) 
        // Na prática, paramos quando a varredura inteira não encontra nenhum símbolo novo.
        } while (novaDescoberta); 

        System.out.println("-> Símbolos Não-Terminais Alcançáveis (VN'): " + vnAlc);
        System.out.println("-> Símbolos Terminais Alcançáveis (VT'): " + vtAlc);

        // Operação de Interseção do final do pseudocódigo
        // VN' <- VN1 interseção VN[i]
        // VT' <- VT interseção VT[i]
        this.vn.retainAll(vnAlc); 
        this.vt.retainAll(vtAlc);

        // P' <- possui as mesmas regras de P1, exceto aquelas inúteis
        List<Regra> regrasAtualizadas = new ArrayList<>();
        for (Regra regra : regras) {
            // Se o não-terminal gerador dessa regra (lado esquerdo) sobreviveu à interseção e está em vn, a regra é mantida
            if (this.vn.contains(regra.getNaoTerminal())) {
                regrasAtualizadas.add(regra);
            }
        }
        
        this.regras = regrasAtualizadas;
    }
    
    public void imprimirRegras() {
        for (Regra r : regras) {
            System.out.println(r);
        }
    }
}