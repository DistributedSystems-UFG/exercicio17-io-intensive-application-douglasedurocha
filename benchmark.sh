#!/usr/bin/env bash
# Compila e mede a vazao (req/s) das tres versoes com 1, 4, 16 e 64 clientes.
set -e
cd "$(dirname "$0")"
javac *.java
DUR=${DUR:-6}

printf '%-10s %8s %8s %8s %8s\n' "servidor" 1 4 16 64
for srv in "single SingleThreadedServer 5001" \
           "per-req ThreadPerRequestServer 5002" \
           "pool ThreadPoolServer 5003 50"; do
  set -- $srv
  name=$1; class=$2; port=$3; pool=${4:-}
  java "$class" "$port" data.txt $pool >/dev/null 2>&1 &
  pid=$!
  sleep 1
  printf '%-10s' "$name"
  for c in 1 4 16 64; do
    r=$(java Client localhost "$port" "$c" "$DUR" 1000)
    printf '%8s' "$(echo "$r" | sed -n 's/.*throughput=\([0-9]*\).*/\1/p')"
  done
  echo
  kill $pid 2>/dev/null || true
  wait $pid 2>/dev/null || true
done
