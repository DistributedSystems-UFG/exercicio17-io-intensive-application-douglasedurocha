[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/sMLHUOxM)

# I/O-Intensive Application — três modelos de concorrência

Serviço de rede (TCP) que lê dados de um **arquivo** (`data.txt`) e os devolve aos
clientes, implementado em **três versões** que só diferem no modelo de threads,
para comparar a **vazão (requisições por segundo)**:

1. **Single-threaded** — `SingleThreadedServer.java`
2. **Uma thread por requisição** — `ThreadPerRequestServer.java`
3. **Pool de threads** — `ThreadPoolServer.java` (esboço do Slide 35, `newFixedThreadPool`)

As três usam o mesmo `Service.java`, que lê uma linha do arquivo e atende a
conexão. `Client.java` é o cliente de carga que mede a vazão.

## Interface

Protocolo de texto: `GET <id>` → `OK <linha do arquivo>` (ou `ERROR ...`); `QUIT` encerra.
`GET` é a operação que permite ao cliente **ler dados do arquivo**.

## Como rodar

```bash
javac *.java

# subir UMA versão (porta, arquivo [, poolSize])
java SingleThreadedServer   5000 data.txt
java ThreadPerRequestServer 5000 data.txt
java ThreadPoolServer       5000 data.txt 50

# gerar carga: host porta concorrência duração(s) maxId
java Client localhost 5000 16 6 1000
```

Comparação automática das três versões: `./benchmark.sh`

## Resultados — vazão (req/s), 12 núcleos

| servidor    |    1 |     4 |    16 |    64 |
|-------------|-----:|------:|------:|------:|
| single      | 5048 |  2683 |  4458 |  4348 |
| per-request | 5161 | 14995 | 34464 | 58196 |
| pool (50)   | 3972 | 15323 | 38002 | 44810 |

## Análise

- **Single-threaded:** vazão **estável (~4–5k)** independente do nº de clientes —
  atende um por vez, os demais esperam na fila e dão *timeout*. Simples, mas não
  aproveita os núcleos nem sobrepõe as esperas de I/O.
- **Thread por requisição:** **escala** com a concorrência (~58k com 64 clientes),
  pois enquanto uma thread bloqueia no I/O as outras avançam. Custo: cria uma
  thread por conexão, sem limite (risco sob carga muito alta).
- **Pool de threads:** escala igual à anterior **enquanto clientes ≤ pool**; acima
  disso (64 > 50) limita a concorrência a 50 e a vazão para de crescer. Em troca,
  **reaproveita threads** e protege contra sobrecarga.

**Conclusão:** para um serviço I/O-intensivo as versões multi-thread vencem de
longe a single-threaded; o **pool** é o melhor equilíbrio em produção (vazão alta
sem criar threads sem limite), bastando dimensionar o pool conforme a carga.
