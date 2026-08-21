# Evolução de aprendizagem, desempenho e multimodalidade do MX

## Objetivo

O MX continuará sendo o único ponto de comunicação do usuário. O Ollama permanece local e o MX Core continua responsável por roteamento, segurança, histórico, memória, skills e observabilidade. As novas capacidades serão adicionadas por contratos internos, sem transformar o conteúdo importado em instrução privilegiada.

## Princípios de operação

O sistema distinguirá quatro camadas. A **memória documental** contém os materiais autorizados de estudos e seus hashes. A **memória de conversa** contém mensagens e resumos vinculados a uma conversa. A **memória do usuário** contém apenas fatos ou preferências explicitamente confirmados, editáveis e removíveis. A **execução** contém runs, tools, aprovações e resultados operacionais. Nenhuma camada poderá instruir diretamente uma tool sem passar pelo MX Core e pelas políticas de segurança.

> O conteúdo de estudos orienta o raciocínio quando for pertinente; ele não define uma resposta fixa, não substitui evidência atual e não autoriza ações externas.

## Fluxo rápido de uma mensagem

1. O cliente envia texto, referências de anexos e identificador idempotente.
2. O Core valida sessão, tamanho, MIME, ownership e limites.
3. O roteador classifica o assunto e seleciona somente os trechos de estudo relevantes.
4. O Core acrescenta um resumo curto da conversa e memórias confirmadas, quando aplicável.
5. O Ollama recebe opções de geração explícitas, `keep_alive`, contexto controlado e streaming.
6. Tokens são transmitidos ao cliente; o resultado final é persistido com duração, modelo, fontes e status.
7. O avaliador registra feedback e, quando o usuário confirmar, transforma uma preferência em memória editável.

## Desempenho local

O perfil padrão continuará usando `qwen3:8b` por qualidade. Um perfil rápido com modelo menor será opcional e deverá ser ativado por variável de ambiente após benchmark de qualidade. O Ollama terá keep-alive, um modelo carregado por vez e uma fila de concorrência conservadora, porque a auditoria encontrou GPU NVIDIA no Windows, mas o runtime Docker não conseguiu inicializar NVML. Portanto, não será declarado uso de GPU até que `--gpus all` e `nvidia-smi` funcionem efetivamente.

A latência será reduzida sem aumentar indiscriminadamente o contexto: prompts serão enxutos, o índice de estudos selecionará trechos por pontuação lexical, o cache guardará contexto por hash de prompt e versão de conhecimento, e o histórico será resumido em vez de concatenado integralmente.

## Aprendizagem incremental

O bootstrap usa SHA-256 e não reimportará conteúdo idêntico. A atualização produzirá um manifesto incremental, taxonomia por domínio/tipo/nível/status e um índice de chunks. Cada chunk preservará origem, hash, headings e versão. O processo será dry-run primeiro, não executará arquivos importados e bloqueará caminhos fora da raiz autorizada.

## Memória e ensino

O MX não inferirá automaticamente atributos sensíveis. Memórias terão origem, confiança, data, status ativo e possibilidade de remoção. O modo de ensino adaptativo usará nível, objetivos, erros recorrentes e preferência de explicação somente quando existirem dados confirmados. A resposta poderá alternar entre explicação, exemplo, exercício, dica, correção e avaliação; a escolha será contextual e não fixa.

## Multimodalidade

Anexos serão recebidos por upload seguro com UUID, MIME allowlist, tamanho máximo, checksum e vínculo ao usuário. Arquivos de texto serão extraídos com limites; áudio será transcrito por um adaptador local configurável; imagens serão encaminhadas somente a modelos com visão declarada. Geração de imagem ou áudio será exposta como capability separada, com fallback explícito quando o ambiente local não possuir o modelo correspondente. Nenhum conteúdo multimídia será executado como código.

## Histórico

A API adotará paginação, busca por conversa e resumos. Mensagens poderão referenciar anexos e metadados de execução sem misturar tokens ou dados de autenticação ao texto. O cliente fará sincronização incremental e manterá rascunhos localmente quando o endpoint estiver indisponível.

## Observabilidade e critérios de aceite

Cada chamada registrará duração até o primeiro token, duração total, modelo, número de tokens quando disponível, tamanho do contexto, cache hit/miss e erro sanitizado. Os critérios de aceite são: nenhuma regressão nos testes atuais; importação idempotente; contexto de estudos menor que a síntese completa; login e SSE funcionando pelo Tailscale; anexos rejeitando MIME/tamanho inválidos; memórias removíveis; e benchmark comparando perfil padrão e rápido sem ocultar limitações do hardware.
