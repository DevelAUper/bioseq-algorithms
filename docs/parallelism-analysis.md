# Parallelism Analysis

This page describes opportunities for parallelism in dynamic programming and where synchronization is required. It will include a small diagram to clarify dependencies between anti-diagonals.

## Outline
- Dependency structure
- Anti-diagonal parallelism
- Practical constraints
- Implementation notes

```mermaid
flowchart TD
  A[DP cell (i-1,j-1)] --> C[DP cell (i,j)]
  B[DP cell (i-1,j)] --> C
  D[DP cell (i,j-1)] --> C
```
