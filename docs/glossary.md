# Glossary

Short definitions for key terms used across the BioSeq project.

- Alignment: arrangement of biological sequences so homologous residues appear in the same column.
- Global alignment: end-to-end alignment of full sequences (not just local regions).
- Dynamic programming (DP): method that solves large optimization problems by combining optimal solutions to smaller subproblems.
- Scoring matrix: table defining substitution costs between residue symbols.
- Gap penalty: cost assigned when a residue is aligned against `-` (a gap).
- Linear gap model: gap cost grows linearly with length, usually `penalty * length`.
- Affine gap model: gap cost uses separate opening and extension penalties, typically `alpha + beta * length`.
- Optimal alignment count: number of distinct alignments that achieve the same minimum total cost.
- Wavefront parallelism: anti-diagonal DP scheduling where cells on the same diagonal are computed in parallel.
- Sum-of-pairs (SP) scoring: multiple-alignment objective summing pairwise column costs over all sequence pairs.
- Center-star heuristic: multiple-alignment strategy that picks one center sequence and aligns all others to it progressively.
- Profile matrix: per-column residue frequency representation of an existing multiple alignment.
- Phylogeny: inferred evolutionary relationship structure among taxa.
- UPGMA: agglomerative tree-building method using weighted average cluster distances and ultrametric assumption.
- Neighbor-Joining: distance-based tree-building method using Q-matrix minimization without strict molecular-clock assumption.
- Newick format: standard parenthesized text format for phylogenetic trees, optionally with branch lengths.
