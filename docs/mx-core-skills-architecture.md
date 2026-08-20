# MX Core — Arquitetura central com Skills especialistas

**Versão:** 1.0
**Decisão:** adotada para a próxima etapa de implementação

## 1. Princípio central

O usuário conversa somente com o **MX Core**. Skills não são interfaces independentes nem agentes que conversam diretamente com o usuário. Elas são capacidades especializadas, registradas no núcleo, selecionadas pelo núcleo e executadas sob o contexto e a política do núcleo.

```text
Usuário
  |
  v
MX Core API / Conversation
  |
  +--> Context Builder
  +--> Intent & Skill Router
  +--> Skill Registry
  +--> Policy / Approval Engine
  +--> Skill Supervisor
  +--> Model Gateway
  +--> Tool Policy + Tool Executor
  +--> Audit + Metrics + Tracing
  |
  +--> Skill: Development
  +--> Skill: Research
  +--> Skill: Personal Organization
  +--> Skill: Finance (future, isolated)
  +--> Skill: Testing (future)
```

O MX deve ser mais do que um roteador de palavras-chave. Ele é o **orquestrador responsável** pelo contexto, segurança, escolha, execução, verificação e explicação do trabalho. Uma skill deve resolver uma especialidade; o MX deve decidir quando, como e com quais limites ela pode ser usada.

## 2. Responsabilidades do MX Core

| Responsabilidade | Regra |
|---|---|
| Identidade | Resolve usuário e escopo de dados antes de qualquer execução |
| Contexto | Monta contexto limitado, relevante e autorizado |
| Seleção | Escolhe uma ou mais skills com base em intenção, capacidade e confiança |
| Política | Bloqueia ou solicita aprovação antes de ações sensíveis |
| Execução | Controla tempo, tentativas, cancelamento, estados e idempotência |
| Supervisão | Verifica formato, evidências, resultado e consistência mínima |
| Comunicação | Entrega uma resposta única e compreensível ao usuário |
| Auditoria | Registra decisão, skill, ferramentas, aprovação, falhas e métricas |
| Aprendizado | Armazena feedback e memórias apenas quando permitido e confirmado |

As skills não devem acessar diretamente `SecurityContextHolder`, banco, filesystem irrestrito ou serviços externos. Elas recebem um `SkillExecutionContext` já filtrado pelo MX.

## 3. Contrato de uma Skill

Toda skill deve possuir uma definição declarativa e uma implementação executável:

```java
public interface Skill {
    SkillDefinition definition();
    SkillResult execute(SkillRequest request, SkillExecutionContext context);
}
```

A definição deve conter nome estável, versão, descrição, capacidades, modelo preferido, ferramentas permitidas, nível máximo de autonomia, timeout, limite de tentativas e formato de saída. A implementação não deve decidir aumentar suas próprias permissões.

```java
public record SkillDefinition(
        String name,
        String version,
        String description,
        Set<SkillCapability> capabilities,
        Set<String> allowedTools,
        AutonomyLevel maximumAutonomy,
        Duration timeout
) {}
```

O resultado deve ser estruturado, mesmo quando a resposta final for texto:

```java
public record SkillResult(
        String skillName,
        String answer,
        boolean complete,
        List<Evidence> evidence,
        List<ProposedAction> proposedActions,
        Map<String, Object> metadata
) {}
```

A skill pode propor uma ação, mas a execução da ação depende do `PolicyEngine` do MX. Isso impede que instruções escondidas em um prompt ou em um documento substituam a política do sistema.

## 4. Seleção e roteamento

O roteador será evolutivo. Na primeira fase, uma seleção determinística baseada em descritores e regras simples permite testar o fluxo. Depois, o MX poderá usar um classificador local estruturado. O classificador deve retornar **skill, confiança, justificativa curta e necessidade de esclarecimento**, nunca executar uma ferramenta.

| Confiança | Comportamento |
|---|---|
| Alta | Selecionar a skill se a capacidade e a política forem compatíveis |
| Média | Selecionar skill geral ou fazer uma pergunta curta de esclarecimento |
| Baixa | Não executar skill especializada; responder ou pedir contexto |
| Conflito | Não escolher silenciosamente; informar a ambiguidade e solicitar direção |

A seleção deve considerar intenção, capacidades requeridas, escopo do usuário, custo/latência, disponibilidade da skill, autonomia permitida e histórico recente. A skill padrão não é uma autorização geral: é apenas o fallback conversacional.

## 5. Execução supervisionada

Cada solicitação recebe um `correlationId` e cada ciclo recebe um `skillRunId`. O ciclo mínimo é:

```text
RECEIVED
  -> CLASSIFYING
  -> ROUTED
  -> CONTEXT_READY
  -> WAITING_APPROVAL (quando necessário)
  -> EXECUTING
  -> VERIFYING
  -> COMPLETED
```

Falhas devem ir para `FAILED` com código de erro seguro. Cancelamento deve ir para `CANCELLED`. Nenhuma chamada de modelo deve manter uma transação de banco aberta. Estados, tentativas e timestamps devem permitir retomar ou explicar uma execução.

A supervisão verifica pelo menos: resposta não vazia, formato esperado, ausência de ação não autorizada, evidência mínima para tarefas que exigem pesquisa, consistência com o objetivo e limites de tamanho. Em tarefas críticas, o MX pode solicitar uma segunda avaliação, mas essa avaliação também está sujeita a custo e timeout.

## 6. Skills iniciais

| Skill | Foco | Ferramentas iniciais | Autonomia inicial |
|---|---|---|---|
| `general` | Conversação, esclarecimento e síntese | Nenhuma | Responder |
| `development` | Código, arquitetura, debugging e documentação técnica | `workspace.list`, `workspace.read_file`, `git.status` | Somente leitura |
| `research` | Pesquisa, comparação e síntese de fontes | Nenhuma ou busca aprovada | Responder com evidências |
| `personal-organization` | Planejamento, listas e organização pessoal | Memória estruturada, sem ações externas | Propor |
| `testing` | Analisar testes, falhas e cobertura | `workspace.read_file`, `test.inspect` | Somente leitura |

Financeiro, saúde e jurídico devem ser tratados como skills isoladas, com escopo e políticas próprios. A existência de uma skill não significa que o MX deva executar decisões de alto impacto automaticamente.

## 7. Fronteira entre Skill e Tool

Uma **skill** é competência de raciocínio e coordenação de uma especialidade. Uma **tool** é uma capacidade operacional limitada. A skill pode solicitar uma tool; somente o MX autoriza e executa a tool.

Exemplo: `development` pode concluir que precisa ler `pom.xml`; ela propõe `workspace.read_file` com o caminho relativo. O MX valida que o caminho está dentro do workspace autorizado, aplica limites, executa a leitura e devolve o resultado como dado. A skill não pode converter esse pedido em shell arbitrário.

## 8. Extensibilidade

Para adicionar uma skill, a equipe deve criar definição, contrato de entrada/saída, política de ferramentas, testes unitários, testes de integração do registro, teste de roteamento e documentação de limites. O MX Core não deve receber condicionais espalhadas para cada nova skill. O registro e os descritores são a extensão principal.

A separação inicial será por pacotes dentro do monólito. Um processo separado somente será considerado quando uma skill exigir runtime incompatível, recursos muito diferentes, isolamento forte ou ciclo de implantação próprio.

## 9. Critérios de qualidade de uma Skill

Uma skill especialista não é apenas um prompt de sistema. Ela deve possuir objetivo, não-objetivos, entradas necessárias, formato de saída, estratégia de contexto, ferramentas permitidas, política de autonomia, testes de comportamento, métricas de qualidade e mecanismo de recusa. O prompt pode mudar; o contrato e os testes devem proteger o comportamento essencial.

## 10. Estado implementado — agosto de 2026

A composição Spring atual registra as skills `general` e `development`. O usuário continua falando somente com o MX Core; o nome da skill pode aparecer como metadado controlado, mas não cria um canal independente. Cada execução possui `runId` e `correlationId`, e o ciclo atual é persistido como `RECEIVED`, `ROUTED`, `EXECUTING`, `VERIFYING` e estado final, com `AWAITING_APPROVAL` e `CANCELLED` disponíveis no aggregate.

O cliente Expo consome o fluxo síncrono e o fluxo SSE. O Policy Engine protege as tools `workspace.list` e `workspace.write_file`, sendo que a segunda é sandboxed, atômica e exige autonomia mínima `EXECUTE_WITH_APPROVAL`. A integração imediata pendente é transformar `REQUIRE_APPROVAL` em uma proposta persistida de `ExecutionRun` em `AWAITING_APPROVAL`, com nonce, expiração e proteção contra replay.
