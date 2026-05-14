import argparse
import sys

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--api_key", type=str, help="Sua chave secreta")
    parser.add_argument("--model", type=str, help="Nome do modelo")
    parser.add_argument("--temperature", type=float, default=0.7)
    parser.add_argument("story_file", help="Arquivo de entrada")
    
    args, _ = parser.parse_known_args()
    
    # Simula uma resposta do LLM
    print(f"Feature: Teste de Importacao Customizada")
    print(f"  Scenario: Validando motor externo")
    print(f"    Given que o usuario importou este script")
    print(f"    When ele executa o plugin com o modelo {args.model}")
    print(f"    Then o script recebe a chave com {len(args.api_key)} caracteres")
    print(f"    And gera este BDD de teste com temperatura {args.temperature}")

if __name__ == "__main__":
    main()
