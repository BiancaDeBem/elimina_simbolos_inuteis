package elimina_simbolo_inuteis;

public class Regra {
	String nao_terminal;
	String substituicao;
	
	public Regra(String nao_terminal, String substituicao) {
		this.nao_terminal = nao_terminal;
		this.substituicao = substituicao;
	}
	
	@Override
	public String toString() {
		return nao_terminal + " -> " + substituicao;
	}

	public String getNaoTerminal() {
		return nao_terminal;
	}

	public String getSubstituicao() {
		return substituicao;
	}
}
