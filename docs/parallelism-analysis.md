# Parallelism Analysis

This note explains how we parallelize dynamic programming (DP) for global sequence alignment, and how to benchmark the effect in practice.

## Wavefront (Anti-Diagonal) Parallelism

In global alignment, each DP cell `(i, j)` depends on three earlier cells:
- `(i-1, j-1)` (diagonal predecessor)
- `(i-1, j)` (up predecessor)
- `(i, j-1)` (left predecessor)

If we define the anti-diagonal index as `d = i + j`, then:
- `(i-1, j-1)` is on diagonal `d-2`
- `(i-1, j)` and `(i, j-1)` are on diagonal `d-1`

This means every cell on diagonal `d` depends only on diagonals that are already finished (`d-1` or `d-2`), not on other cells from the same diagonal. So:
- We process diagonals in order (`d = 1, 2, ...`), and
- We compute cells inside one diagonal in parallel.

That pattern is called **wavefront** parallelism.

```mermaid
flowchart TD
  A[Diagonal d-2] --> C[Cell (i,j) on diagonal d]
  B[Diagonal d-1] --> C
  D[Same diagonal d] -. independent cells .-> C
```

## Why This Works for Alignment

For both linear-gap and affine-gap DP:
- Dependencies always point to previous diagonals.
- No cell in diagonal `d` needs another cell from diagonal `d`.

For affine DP there are three layers (`D`, `I`, `S`), but the same rule holds: all dependencies still point to older diagonals. Therefore, we can parallelize per anti-diagonal in both models.

## Practical Detail: Threshold-Based Scheduling

Parallel work has overhead (task creation, synchronization, scheduling). Very short diagonals near the top-left and bottom-right corners of the matrix do not have enough cells to benefit from threading.

So the implementation uses a simple threshold:
- **Small diagonals**: compute sequentially
- **Large diagonals**: compute in parallel

This gives better real performance than forcing every diagonal through parallel scheduling.

## Running the Benchmark

From the repository root:

```bash
python scripts/benchmark_parallelism.py
```

What the script does:
1. Builds the CLI jar (`mvnw.cmd -q -pl cli -am package -DskipTests`).
2. Runs 2 warmup alignments at length 1000 (ignored).
3. Benchmarks `global_linear` with thread counts `1, 2, 4, 8`.
4. Uses sequence lengths `5000, 10000, 15000`.
5. Repeats each configuration 3 times and keeps the median runtime.
6. Writes:
   - `results/parallelism_analysis.csv`
   - `results/parallelism_speedup.png`
   - `results/parallelism_runtime.png`

## How to Interpret the Outputs

### CSV (`parallelism_analysis.csv`)
Columns:
- `length`: sequence length
- `threads`: number of threads used
- `median_seconds`: median runtime from 3 repeats
- `speedup`: `T1 / TN` for the same length

### Speedup Plot (`parallelism_speedup.png`)
- X-axis: threads
- Y-axis: speedup relative to 1 thread
- One line per sequence length
- Includes an ideal dashed line (`speedup = threads`)

If measured lines are below the ideal line, that is expected in real systems.

### Runtime Plot (`parallelism_runtime.png`)
- X-axis: threads
- Y-axis: runtime in seconds
- One line per sequence length

This plot shows absolute time savings directly.

## Experimental Results

### Correctness

The wavefront parallel implementations (linear and affine) produce identical alignment results and costs compared with the sequential implementations. This equivalence is verified by the test suite, including dedicated parity tests across multiple thread counts.

### Observed Performance

For the tested problem sizes (lengths 5000, 10000, 15000) and thread counts (1, 2, 4, 8), the parallel wavefront implementation is slower than the sequential baseline in all measured configurations.

This is an important and valid experimental outcome: the architecture is correct, but the runtime economics at this workload do not favor threading.

### Benchmark Data

| length | threads | median_seconds | speedup |
| ---: | ---: | ---: | ---: |
| 5000 | 1 | 0.419059 | 1.000000 |
| 5000 | 2 | 0.684480 | 0.612231 |
| 5000 | 4 | 0.694650 | 0.603267 |
| 5000 | 8 | 0.783433 | 0.534902 |
| 10000 | 1 | 1.099226 | 1.000000 |
| 10000 | 2 | 1.908057 | 0.576097 |
| 10000 | 4 | 1.651078 | 0.665762 |
| 10000 | 8 | 1.716728 | 0.640303 |
| 15000 | 1 | 2.358032 | 1.000000 |
| 15000 | 2 | 3.803853 | 0.619906 |
| 15000 | 4 | 2.926888 | 0.805645 |
| 15000 | 8 | 3.063726 | 0.769662 |

### Why Parallel Is Slower Here

The measurements are consistent with known parallel-DP overheads:

1. Per-cell computation is extremely cheap (roughly a few integer additions/comparisons, on the order of nanoseconds), so there is very little work to amortize scheduling costs.
2. Thread scheduling, task submission, and synchronization per anti-diagonal cost more than the arithmetic saved by parallel execution.
3. The JVM parallel-stream/ForkJoin infrastructure introduces additional overhead (stream setup, lambda execution overhead, work-stealing coordination).
4. The algorithm has `n + m` anti-diagonals, and each diagonal acts as a barrier: the next wave cannot start until the current one is complete, so the dependency chain still enforces substantial sequential structure.

### When Wavefront Parallelism Would Help

Wavefront parallelism is still the right architectural pattern and can become beneficial when per-cell work is heavier or parallel execution is much cheaper:

1. More expensive scoring logic per cell (for example, profile/profile or richer multiple-alignment scoring).
2. Much larger sequence lengths (for example, 100,000+) where diagonals are wide enough for better thread utilization.
3. GPU-style execution where thousands of cells run concurrently with much lower scheduling overhead than CPU thread orchestration.

### Conclusion

This result is a strength of the project: it demonstrates both correct parallel algorithm design and disciplined empirical validation.  
The wavefront implementation is architecturally correct and valuable for future extension, but for the current global linear workload and tested sizes, sequential execution is the optimal runtime choice. The `--threads` option is therefore retained as forward-looking infrastructure for algorithms with heavier per-cell computation.

## Expected Behavior in General

Speedup is usually **sublinear** (for example, 4 threads giving less than 4x speedup). Main reasons:
- Thread coordination and synchronization overhead
- Task scheduling overhead
- Limited work on short diagonals at the matrix start/end
- Memory bandwidth and cache effects

In short: larger matrices usually benefit more, but perfect linear scaling is not realistic.
