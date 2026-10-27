# Decisões técnicas

## Valores monetários

BigDecimal evita erros binários de ponto flutuante. Cada lançamento tem duas casas; valores que exigiriam arredondamento são rejeitados. Valores devem ser positivos e o tipo determina o sinal. O livro pressupõe uma única moeda: não mistura nem converte moedas.

## Histórico e idempotência

Lançamentos são records imutáveis em um livro append-only. Repetir o mesmo id e conteúdo não duplica o saldo; reutilizar id com conteúdo diferente falha. Não há edição, remoção ou estorno nesta versão.

## Relatórios e CSV

YearMonth filtra o período; o resumo por categoria inclui somente despesas e retorna mapa imutável ordenado. CSV preserva a ordem de inclusão, usa CRLF e aspas duplicadas em textos. Prefixos de fórmula recebem apóstrofo; isso altera o texto exportado para reduzir interpretação como fórmula em planilhas. Importação CSV não está implementada.

## Limitações

As operações sincronizadas garantem consistência apenas nesta instância. Não há armazenamento, auditoria externa ou regras tributárias. Próximos passos possíveis: adaptador JDBC, moeda explícita e estorno referenciado; nenhum deles está implementado.
