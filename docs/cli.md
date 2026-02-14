# CLI Contract (Project 1)

## Overview
This project defines two command-line programs for min-cost global alignment with a linear gap penalty:
- `global_linear`: computes one optimal alignment and its total cost
- `global_count`: counts the number of optimal alignments (same scoring model)

## Inputs
You can provide sequences directly or via FASTA files:
- Direct sequences: `--seq1 <string>` and `--seq2 <string>`
- FASTA inputs: `--fasta1 <path>` and `--fasta2 <path>`
  - Only the first FASTA record from each file is used.

Required options:
- `--matrix <path>`: distance-based scoring matrix file
- `--gap <int>`: linear gap penalty (non-negative integer)

Optional options:
- `--traceback`: include alignment strings in output (when applicable)
- `--wrap <int>`: wrap alignment lines to this width
- `--out <path>`: write output to a file instead of stdout
- `--threads <int>`: future extension for parallelism (currently may be ignored)

## Output formats
`global_linear` (min-cost alignment)
- Always prints total cost as an integer.
- If `--traceback` is set, also prints aligned sequences on separate lines.

`global_count` (number of optimal alignments)
- Prints total cost and the count of optimal alignments.

## Examples
Windows PowerShell:
```powershell
# direct sequences
java -jar java/cli/target/bioseq-cli.jar global_linear --seq1 ACTG --seq2 AC-G --matrix data/matrices/example.txt --gap 2 --traceback

# FASTA inputs
java -jar java/cli/target/bioseq-cli.jar global_count --fasta1 data/fasta/a.fa --fasta2 data/fasta/b.fa --matrix data/matrices/example.txt --gap 2
```

bash:
```bash
# direct sequences
java -jar java/cli/target/bioseq-cli.jar global_linear --seq1 ACTG --seq2 ACG --matrix data/matrices/example.txt --gap 2 --traceback

# FASTA inputs
java -jar java/cli/target/bioseq-cli.jar global_count --fasta1 data/fasta/a.fa --fasta2 data/fasta/b.fa --matrix data/matrices/example.txt --gap 2
```

## Project 1 Evaluation (project1_eval.txt)
Use this section to reproduce the Project 1 tables on Windows PowerShell.

1. Create the matrix file `data/matrices/project1_M.txt`:
```powershell
@'
4
A 0 2 5 2
C 2 0 2 5
G 5 2 0 2
T 2 5 2 0
'@ | Set-Content -Encoding UTF8 data/matrices/project1_M.txt
```

2. Create the FASTA file `data/fasta/project1_eval_seqs.fa` with records `seq1`..`seq5` (copy the exact sequences from your assignment handout):
```powershell
@'
>seq1
PASTE_SEQ1_HERE
>seq2
PASTE_SEQ2_HERE
>seq3
PASTE_SEQ3_HERE
>seq4
PASTE_SEQ4_HERE
>seq5
PASTE_SEQ5_HERE
'@ | Set-Content -Encoding UTF8 data/fasta/project1_eval_seqs.fa
```

3. Compute the cost/count tables (`gap=5`) and print them:
```powershell
$jar = "java/cli/target/bioseq-cli.jar"
$matrix = "data/matrices/project1_M.txt"
$gap = 5
$fasta = "data/fasta/project1_eval_seqs.fa"

# Parse FASTA into an ordered map.
$seqs = [ordered]@{}
$id = $null; $buf = ""
Get-Content $fasta | ForEach-Object {
  $line = $_.Trim()
  if ($line -eq "") { return }
  if ($line.StartsWith(">")) {
    if ($id) { $seqs[$id] = $buf.ToUpper(); $buf = "" }
    $id = $line.Substring(1).Trim()
  } else {
    $buf += $line
  }
}
if ($id) { $seqs[$id] = $buf.ToUpper() }

$ids = $seqs.Keys
"Cost table:"
foreach ($i in $ids) {
  $row = @()
  foreach ($j in $ids) {
    $cost = java -jar $jar global_linear --seq1 $seqs[$i] --seq2 $seqs[$j] --matrix $matrix --gap $gap
    $row += $cost
  }
  "{0}: {1}" -f $i, ($row -join "`t")
}

"Count table:"
foreach ($i in $ids) {
  $row = @()
  foreach ($j in $ids) {
    $out = java -jar $jar global_count --seq1 $seqs[$i] --seq2 $seqs[$j] --matrix $matrix --gap $gap
    $count = ($out | Select-String "^count:\s+(\d+)$").Matches[0].Groups[1].Value
    $row += $count
  }
  "{0}: {1}" -f $i, ($row -join "`t")
}
```

4. Output one optimal alignment (`seq1` vs `seq2`) with traceback:
```powershell
java -jar java/cli/target/bioseq-cli.jar global_linear `
  --seq1 "<SEQ1>" --seq2 "<SEQ2>" `
  --matrix data/matrices/project1_M.txt --gap 5 --traceback --wrap 60
```
Replace `<SEQ1>` and `<SEQ2>` with the same strings used in `project1_eval_seqs.fa`.
