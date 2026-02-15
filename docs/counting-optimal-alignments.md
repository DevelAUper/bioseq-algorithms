# Counting Optimal Alignments

In addition to the minimum alignment cost, this project can count how many distinct alignments achieve that optimal cost.

## Why Counting Matters
Different traceback paths can lead to the same optimal score. Counting tells you whether the optimum is unique or highly degenerate.

Counts can grow very quickly, so implementations use `BigInteger` rather than fixed-width integer types.

## Linear Gap Counting
`OptimalAlignmentCounter` tracks:
- `dpCost[i][j]`: best cost for prefixes
- `dpCount[i][j]`: number of ways to realize `dpCost[i][j]`

At each cell:
1. Compute diagonal, up, and left candidate costs.
2. Set `best = min(diag, up, left)`.
3. Sum counts from every predecessor that achieves `best`.

This ensures all co-optimal predecessors contribute to the final count.

## Affine Gap Counting
`OptimalAffineAlignmentCounter` mirrors affine DP with three layers:
- `D`: residue-residue state
- `I`: gap in sequence 2
- `S`: gap in sequence 1

It maintains both cost and count tables for each layer:
- `D`, `I`, `S`
- `countD`, `countI`, `countS`

For each cell/layer, counts are summed from exactly those predecessor transitions that match the optimal layer cost. Final count is the sum across layers that achieve global minimum at `(n, m)`.

## Complexity
Linear counting:
- Time `O(n * m)`
- Memory `O(n * m)`

Affine counting:
- Time `O(n * m)` with larger constant factor (three layers)
- Memory `O(n * m)` with three cost and three count grids

## CLI Access
- Linear counting:
```bash
java -jar java/cli/target/bioseq-cli.jar global_count --seq1 ACGT --seq2 AGT --matrix data/matrices/dna_example.txt --gap 2
```

- Affine counting (included in `global_affine` output):
```bash
java -jar java/cli/target/bioseq-cli.jar global_affine --seq1 ACGT --seq2 AGT --matrix data/matrices/dna_example.txt --alpha 10 --beta 3
```

## Key Classes
- `java/pairwise-alignment/src/main/java/bioseq/pairwise/global/OptimalAlignmentCounter.java`
- `java/pairwise-alignment/src/main/java/bioseq/pairwise/global/OptimalAffineAlignmentCounter.java`
