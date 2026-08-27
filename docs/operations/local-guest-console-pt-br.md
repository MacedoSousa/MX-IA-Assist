# Console local efêmero sem cadastro

O script `scripts/mx-local-chat.ps1` oferece uma conversa local pelo CMD ou PowerShell **sem criar conta nem sessão**. Ele usa exclusivamente o Ollama nativo em `127.0.0.1:11435` e descarta a interação ao encerrar o terminal.

```powershell
cd D:\MX

# Conversa interativa com o modelo padrão do MX
.\scripts\mx-local-chat.ps1

# Uma pergunta única, sem histórico
.\scripts\mx-local-chat.ps1 -Prompt "Explique Clean Architecture em duas frases."

# Perfil local menor para respostas rápidas de terminal
.\scripts\mx-local-chat.ps1 -Model qwen3:8b -Prompt "Responda somente: pronto."
```

| Capacidade | Console sem cadastro | Interface autenticada do MX |
|---|---|---|
| Conversa local | Sim, efêmera | Sim, associada à conta |
| Histórico e memória | Não | Sim, conforme o fluxo existente |
| Conhecimento documental/RAG | Não | Sim, com citações rastreáveis |
| Anexos, mídia e documentos | Não | Sim, conforme recursos habilitados |
| Arquivos, workspace e aprovações | Não | Sim, com aprovação humana e allowlists |
| Internet e autoanálise | Não | Somente conforme política e configuração do Core |

> O console não é um atalho de autenticação nem uma forma de contornar as proteções do MX. Ele não chama o Core, não lê segredos e não tem acesso a banco, arquivos, memória, ferramentas, Docker ou publicação.

A validação local confirmou a resposta pelo perfil `qwen3:8b`. O `deepseek-r1:14b` continua como modelo padrão potente do MX, mas pode ter maior latência no console em função do hardware e do carregamento atual do modelo.
