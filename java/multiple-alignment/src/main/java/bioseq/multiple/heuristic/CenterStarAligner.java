package bioseq.multiple.heuristic;

import bioseq.core.gap.GapCost;
import bioseq.core.gap.LinearGapCost;
import bioseq.core.model.Sequence;
import bioseq.core.scoring.ScoreMatrix;
import bioseq.multiple.api.MultipleAligner;
import bioseq.multiple.model.MultipleAlignmentResult;
import bioseq.multiple.scoring.SumOfPairsCost;
import bioseq.pairwise.global.GlobalLinearAligner;
import bioseq.pairwise.model.AlignmentResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Center-star heuristic for multiple-sequence alignment.
 *
 * <p>This method is heuristic: it first chooses one input sequence as the center (minimum total
 * pairwise distance), then progressively aligns all other sequences to that center while keeping
 * a single shared gap pattern for the center row. Because the search space is not explored
 * exhaustively, the returned alignment is not guaranteed to be globally optimal.
 */
public final class CenterStarAligner implements MultipleAligner {
  private final ScoreMatrix defaultMatrix;
  private final GapCost defaultGap;

  /**
   * Creates a center-star aligner with default scoring parameters.
   *
   * @param matrix default substitution/distance matrix
   * @param gap default gap-cost model
   * @throws NullPointerException if any argument is {@code null}
   */
  public CenterStarAligner(ScoreMatrix matrix, GapCost gap) {
    this.defaultMatrix = Objects.requireNonNull(matrix, "matrix must not be null");
    this.defaultGap = Objects.requireNonNull(gap, "gap must not be null");
  }

  /**
   * Builds a multiple alignment using the center-star heuristic.
   *
   * <p>Pipeline:
   * <ol>
   *   <li>Validate input size and symbols.</li>
   *   <li>Compute all pairwise distances and choose center sequence.</li>
   *   <li>Progressively align each non-center sequence to the center using pairwise DP.</li>
   *   <li>Merge center gap patterns so all rows share one aligned coordinate system.</li>
   *   <li>Compute final sum-of-pairs score and return immutable result.</li>
   * </ol>
   *
   * @param sequences input sequences (must contain at least 3)
   * @param matrix substitution/distance matrix; when {@code null}, constructor default is used
   * @param gap gap model; when {@code null}, constructor default is used
   * @return heuristic multiple-alignment result
   * @throws NullPointerException if sequences or sequence entries are {@code null}
   * @throws IllegalArgumentException if fewer than 3 sequences are supplied, if symbols are invalid,
   *     or if gap model is not compatible with linear pairwise center-star steps
   */
  @Override
  public MultipleAlignmentResult align(List<Sequence> sequences, ScoreMatrix matrix, GapCost gap) {
    List<Sequence> validatedSequences = validateSequences(sequences);
    ScoreMatrix effectiveMatrix = resolveMatrix(matrix);
    GapCost effectiveGap = resolveGap(gap);
    LinearGapCost linearGap = requireLinearGap(effectiveGap);

    // Validate all residues up front against the matrix alphabet.
    for (Sequence sequence : validatedSequences) {
      effectiveMatrix.alphabet().validate(sequence.getResidues());
    }

    GlobalLinearAligner pairwiseAligner = new GlobalLinearAligner();

    // Step 1-2: all-vs-all pairwise costs.
    int n = validatedSequences.size();
    int[][] pairwiseCosts = new int[n][n];
    for (int i = 0; i < n; i++) {
      for (int j = i + 1; j < n; j++) {
        int cost = pairwiseAligner.computeCost(
            validatedSequences.get(i), validatedSequences.get(j), effectiveMatrix, linearGap);
        pairwiseCosts[i][j] = cost;
        pairwiseCosts[j][i] = cost;
      }
    }

    // Step 3: choose center sequence with minimum total distance to all others.
    int centerIndex = 0;
    long bestTotalDistance = Long.MAX_VALUE;
    for (int i = 0; i < n; i++) {
      long totalDistance = 0L;
      for (int j = 0; j < n; j++) {
        totalDistance += pairwiseCosts[i][j];
      }
      if (totalDistance < bestTotalDistance) {
        bestTotalDistance = totalDistance;
        centerIndex = i;
      }
    }

    // Step 4: progressively align each non-center sequence to the original center.
    List<Integer> activeIndices = new ArrayList<>();
    List<String> activeAligned = new ArrayList<>();
    activeIndices.add(centerIndex);
    activeAligned.add(validatedSequences.get(centerIndex).getResidues());

    for (int seqIndex = 0; seqIndex < n; seqIndex++) {
      if (seqIndex == centerIndex) {
        continue;
      }

      AlignmentResult pairwise = pairwiseAligner.align(
          validatedSequences.get(centerIndex),
          validatedSequences.get(seqIndex),
          effectiveMatrix,
          linearGap);

      // Merge pairwise center gaps into the running center gap pattern.
      activeAligned = mergeWithCurrentAlignment(activeAligned, pairwise.getAligned1(), pairwise.getAligned2());
      activeIndices.add(seqIndex);
    }

    // Reorder to input order so ids and aligned rows are predictable for callers.
    String[] alignedByInputIndex = new String[n];
    for (int row = 0; row < activeIndices.size(); row++) {
      alignedByInputIndex[activeIndices.get(row)] = activeAligned.get(row);
    }

    List<String> alignedSequences = new ArrayList<>(n);
    List<String> sequenceIds = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      if (alignedByInputIndex[i] == null) {
        throw new IllegalStateException("Center-star merge failed to produce aligned row for input index " + i);
      }
      alignedSequences.add(alignedByInputIndex[i]);
      sequenceIds.add(validatedSequences.get(i).getId());
    }

    // Step 5: score the final alignment with sum-of-pairs.
    int totalCost = new SumOfPairsCost().totalCost(alignedSequences, effectiveMatrix, effectiveGap);
    return new MultipleAlignmentResult(totalCost, alignedSequences, sequenceIds);
  }

  /**
   * Computes only center-star alignment cost.
   *
   * @param sequences input sequences (must contain at least 3)
   * @param matrix substitution/distance matrix; when {@code null}, constructor default is used
   * @param gap gap model; when {@code null}, constructor default is used
   * @return sum-of-pairs cost of the heuristic center-star alignment
   */
  @Override
  public int computeCost(List<Sequence> sequences, ScoreMatrix matrix, GapCost gap) {
    MultipleAlignmentResult result = align(sequences, matrix, gap);
    ScoreMatrix effectiveMatrix = resolveMatrix(matrix);
    GapCost effectiveGap = resolveGap(gap);
    return new SumOfPairsCost().totalCost(result.getAlignedSequences(), effectiveMatrix, effectiveGap);
  }

  /**
   * Merges a newly produced pairwise (center, sequence) alignment into the existing multi-alignment.
   *
   * <p>{@code currentAligned.get(0)} is the current center row. {@code pairCenterAligned}
   * is the center row from a new pairwise alignment against one additional sequence.
   * The method harmonizes both center gap patterns by inserting columns where needed.
   */
  private static List<String> mergeWithCurrentAlignment(
      List<String> currentAligned,
      String pairCenterAligned,
      String pairSequenceAligned) {
    Objects.requireNonNull(currentAligned, "currentAligned must not be null");
    Objects.requireNonNull(pairCenterAligned, "pairCenterAligned must not be null");
    Objects.requireNonNull(pairSequenceAligned, "pairSequenceAligned must not be null");
    if (currentAligned.isEmpty()) {
      throw new IllegalArgumentException("currentAligned must contain at least the center row");
    }
    if (pairCenterAligned.length() != pairSequenceAligned.length()) {
      throw new IllegalArgumentException("Pairwise aligned strings must have equal length");
    }

    String currentCenter = currentAligned.get(0);
    List<StringBuilder> mergedExistingRows = new ArrayList<>(currentAligned.size());
    for (int row = 0; row < currentAligned.size(); row++) {
      mergedExistingRows.add(new StringBuilder());
    }
    StringBuilder mergedNewRow = new StringBuilder();

    int i = 0;
    int j = 0;
    while (i < currentCenter.length() || j < pairCenterAligned.length()) {
      char centerCurrent = i < currentCenter.length() ? currentCenter.charAt(i) : '\0';
      char centerPair = j < pairCenterAligned.length() ? pairCenterAligned.charAt(j) : '\0';

      // Both center rows consume a real residue from the original center sequence.
      if (i < currentCenter.length()
          && j < pairCenterAligned.length()
          && centerCurrent != '-'
          && centerPair != '-') {
        if (centerCurrent != centerPair) {
          throw new IllegalStateException(
              "Center residue mismatch while merging center-star alignments: '"
                  + centerCurrent + "' vs '" + centerPair + "'");
        }
        appendExistingColumn(currentAligned, mergedExistingRows, i);
        mergedNewRow.append(pairSequenceAligned.charAt(j));
        i++;
        j++;
        continue;
      }

      // Existing alignment has a center gap column that the new pairwise result does not.
      if (i < currentCenter.length()
          && centerCurrent == '-'
          && (j >= pairCenterAligned.length() || centerPair != '-')) {
        appendExistingColumn(currentAligned, mergedExistingRows, i);
        mergedNewRow.append('-');
        i++;
        continue;
      }

      // New pairwise center has a gap column not present in current alignment: insert in all rows.
      if (j < pairCenterAligned.length()
          && centerPair == '-'
          && (i >= currentCenter.length() || centerCurrent != '-')) {
        appendGapColumn(mergedExistingRows);
        mergedNewRow.append(pairSequenceAligned.charAt(j));
        j++;
        continue;
      }

      // Both representations have a center gap in this column.
      if (i < currentCenter.length()
          && j < pairCenterAligned.length()
          && centerCurrent == '-'
          && centerPair == '-') {
        appendExistingColumn(currentAligned, mergedExistingRows, i);
        mergedNewRow.append(pairSequenceAligned.charAt(j));
        i++;
        j++;
        continue;
      }

      // If one center has remaining non-gap residues after the other is exhausted, merge is invalid.
      throw new IllegalStateException("Unable to reconcile center gap patterns during center-star merge");
    }

    List<String> merged = new ArrayList<>(currentAligned.size() + 1);
    for (StringBuilder rowBuilder : mergedExistingRows) {
      merged.add(rowBuilder.toString());
    }
    merged.add(mergedNewRow.toString());
    return merged;
  }

  /** Appends one existing alignment column to all current rows. */
  private static void appendExistingColumn(
      List<String> currentAligned,
      List<StringBuilder> mergedExistingRows,
      int column) {
    for (int row = 0; row < currentAligned.size(); row++) {
      mergedExistingRows.get(row).append(currentAligned.get(row).charAt(column));
    }
  }

  /** Inserts a synthetic all-gap column into already integrated rows. */
  private static void appendGapColumn(List<StringBuilder> mergedExistingRows) {
    for (StringBuilder row : mergedExistingRows) {
      row.append('-');
    }
  }

  /** Resolves matrix argument with fallback to constructor default. */
  private ScoreMatrix resolveMatrix(ScoreMatrix matrix) {
    if (matrix != null) {
      return matrix;
    }
    return defaultMatrix;
  }

  /** Resolves gap argument with fallback to constructor default. */
  private GapCost resolveGap(GapCost gap) {
    if (gap != null) {
      return gap;
    }
    return defaultGap;
  }

  /** Validates sequence list shape and nullness, and returns an immutable copy. */
  private static List<Sequence> validateSequences(List<Sequence> sequences) {
    Objects.requireNonNull(sequences, "sequences must not be null");
    if (sequences.size() < 3) {
      throw new IllegalArgumentException("Center-star alignment requires at least 3 sequences");
    }
    List<Sequence> copy = new ArrayList<>(sequences.size());
    for (int i = 0; i < sequences.size(); i++) {
      copy.add(Objects.requireNonNull(sequences.get(i), "sequence at index " + i + " must not be null"));
    }
    return List.copyOf(copy);
  }

  /** Ensures center-star pairwise subproblems use the linear gap model required by the implementation. */
  private static LinearGapCost requireLinearGap(GapCost gap) {
    if (gap instanceof LinearGapCost linearGapCost) {
      return linearGapCost;
    }
    throw new IllegalArgumentException(
        "CenterStarAligner currently requires LinearGapCost for pairwise center-star alignments");
  }
}
