# Módulo 14 — Azure: rede, segurança, balanceamento e monitoramento

## Fonte
[Azure Cloud: segurança e recursos avançados](https://cursos.alura.com.br/course/azure-cloud-seguranca-recursos-avancados). A ementa aborda Azure Virtual Network, subnets, integração front-end/back-end, Network Security Group, Azure Firewall, Load Balancer e Application Insights.

## Aplicação ao MX
A arquitetura em nuvem deve separar rede pública, camada de aplicação, workers e dados. Subnets e regras de entrada limitam exposição. NSG e Firewall aplicam defesa em profundidade. Load Balancer distribui tráfego entre instâncias stateless. Dados, brokers e endpoints de modelos devem permanecer em redes privadas sempre que possível.

## Monitoramento
Application Insights deve acompanhar requisições, dependências, exceções, disponibilidade e desempenho. A correlação deve usar o mesmo `trace_id` do MX Core, Kafka/RabbitMQ e chamadas de modelo. O conteúdo de prompts e documentos deve ser redigido ou excluído da telemetria.

## Skill derivada
**Projetar implantação segura em Azure:** definir zonas e subnets; reduzir portas públicas; aplicar NSG e Firewall; separar identidade e segredo; distribuir instâncias; configurar health probes; instrumentar aplicação; criar alertas; testar failover e revisar exposição.

## Referências
[1]: https://cursos.alura.com.br/course/azure-cloud-seguranca-recursos-avancados "Curso da Alura — Azure Cloud: segurança e recursos avançados"
