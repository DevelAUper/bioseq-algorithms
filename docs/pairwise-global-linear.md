# Pairwise Global Alignment (Linear Gap)

This module implements Needleman-Wunsch-style global alignment under a linear gap penalty model.

## Problem Definition
Given sequences `s1` and `s2`, a substitution cost matrix, and gap penalty `g`, find an end-to-end alignment with minimum total cost.

Cost model:
- residue vs residue: `matrix.cost(a, b)`
- residue vs gap: `g`
- gap vs gap: not used by pairwise global DP recurrence

## Dynamic Programming Recurrence
Let `dp[i][j]` be the minimum cost for prefixes `s1[0..i)` and `s2[0..j)`.

Initialization:
- `dp[0][0] = 0`
- `dp[i][0] = dp[i-1][0] + g`
- `dp[0][j] = dp[0][j-1] + g`

Transition:
```text
dp[i][j] = min(
  dp[i-1][j-1] + matrix.cost(s1[i-1], s2[j-1]),  // diagonal
  dp[i-1][j]   + g,                               // up (gap in s2)
  dp[i][j-1]   + g                                // left (gap in s1)
)
```

## Traceback
The aligner stores one predecessor move per cell:
- `DIAG` for residue-residue
- `UP` for residue-gap
- `LEFT` for gap-residue

Starting from `(n, m)`, it walks backward to `(0, 0)` and reconstructs one optimal aligned pair.

## Complexity
- Time: `O(n * m)`
- Memory: `O(n * m)` for full DP + traceback table

## Parallel Variant
When CLI `--threads > 1`, the command uses a wavefront implementation that computes anti-diagonals in parallel:
- `WavefrontLinearAligner`

## Key Classes
- `java/pairwise-alignment/src/main/java/bioseq/pairwise/global/GlobalLinearAligner.java`
- `java/pairwise-alignment/src/main/java/bioseq/pairwise/parallel/WavefrontLinearAligner.java`
