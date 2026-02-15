# Scoring Matrices

The project treats substitution values as alignment costs (lower is better). Dynamic programming minimizes total cost from substitution plus gap penalties.

## File Format (Phylip-like)
Matrix files are parsed by `ScoreMatrix.fromPhylipLikeFile(...)` and follow this format:

1. First non-empty line: matrix size `n`
2. Next `n` non-empty lines:  
   `SYMBOL value1 value2 ... valueN`

Example DNA matrix:
```text
4
A 0 2 5 2
C 2 0 2 5
G 5 2 0 2
T 2 5 2 0
```

## Parsing Rules
- Symbols are normalized to uppercase.
- Duplicate symbols are rejected.
- Each row must contain exactly `1 + n` tokens.
- Numeric cells must parse as integers.
- Blank lines are ignored.

## Alphabet and Validation
The matrix defines an `Alphabet` used to validate sequences before alignment:
- Any unknown residue triggers an `IllegalArgumentException`.
- Validation is case-insensitive due to uppercase normalization.

## Lookup Semantics
`matrix.cost(a, b)` returns integer cost for residue pair `(a, b)`.

The API also provides backward-compatible alias:
- `matrix.score(a, b)` -> same as `matrix.cost(a, b)`

## Related Files
- `java/core/src/main/java/bioseq/core/scoring/ScoreMatrix.java`
- `java/core/src/main/java/bioseq/core/scoring/Alphabet.java`
