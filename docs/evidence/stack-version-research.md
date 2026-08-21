# Pesquisa de versões do stack — 21 de agosto de 2026

Esta nota registra as fontes externas consultadas durante a auditoria de obsolescência e desempenho. Ela não recomenda atualização automática: qualquer mudança de versão deve passar por compatibilidade, testes e reconstrução controlada.

| Componente | Constatação relevante | Fonte oficial |
|---|---|---|
| Spring Boot | O projeto usa Spring Boot 4.0.0; a linha 4.1.0 foi anunciada em junho de 2026 e reúne correções e atualizações posteriores à 4.0. | [Spring Boot 4.1.0 available now](https://spring.io/blog/2026/06/10/spring-boot-4-1-0-available-now/) |
| Expo | O Expo mantém uma trilha oficial de atualização por SDK e orienta consultar as notas da versão alvo para mudanças incompatíveis. | [Upgrade Expo SDK](https://docs.expo.dev/workflow/upgrading-expo-sdk-walkthrough/) |
| Ollama | A imagem Docker oficial é distribuída como `ollama/ollama`; a auditoria local recomenda trocar tags móveis por versão e digest testados. | [Repositório oficial Ollama](https://github.com/ollama/ollama) |
| PDFBox | A linha estável PDFBox 3.0.x disponibiliza a versão 3.0.8 e exige somente Java 8, portanto é compatível com o Java 21 do MX. | [PDFBox download](https://pdfbox.apache.org/download.html) |
| Apache POI | A página oficial indica Apache POI 5.5.1 como versão estável para formatos Office. | [Apache POI download](https://poi.apache.org/download.html) |

## Uso na decisão

As fontes acima embasam apenas a classificação de risco de versão. As métricas de CPU, memória, GPU, armazenamento e comportamento dos serviços foram coletadas diretamente na máquina local e prevalecem para as recomendações de desempenho do MX.
