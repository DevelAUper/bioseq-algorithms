package bioseq.pairwise.global;

import bioseq.core.gap.AffineGapCost;
import bioseq.core.model.Sequence;
import bioseq.core.scoring.ScoreMatrix;
import bioseq.pairwise.api.GlobalAligner;
import bioseq.pairwise.model.AlignmentResult;
import java.util.Arrays;
import java.util.Objects;

/**
 * Global aligner for affine gap penalties using a three-layer dynamic-programming formulation.
 *
 * <p>The DP state is split into three layers:
 * <ul>
 *   <li>{@code D[i][j]}: best cost ending with a residue-residue alignment (diagonal state)</li>
 *   <li>{@code I[i][j]}: best cost ending with a gap in sequence 2 (consume from sequence 1)</li>
 *   <li>{@code S[i][j]}: best cost ending with a gap in sequence 1 (consume from sequence 2)</li>
 * </ul>
 *
 * <p>Separating states this way lets the recurrence distinguish opening a gap
 * ({@code alpha + beta}) from extending an existing gap ({@code beta}).
 */
public final class GlobalAffineAligner implements GlobalAligner<AffineGapCost> {
  private static final int INF = Integer.MAX_VALUE / 2;
  private static final int LAYER_D = 0;
  private static final int LAYER_I = 1;
  private static final int LAYER_S = 2;
  private static final int LAYER_UNSET = -1;

  /**
   * Computes only the optimal global alignment cost under an affine gap model.
   *
   * @param s1 first sequence
   * @param s2 second sequence
   * @param matrix substitution/distance matrix
   * @param gap affine gap cost model
   * @return minimum alignment cost
   * @throws NullPointerException if any input is {@code null}
   * @throws IllegalArgumentException if a sequence contains symbols outside the matrix alphabet
   */
  @Override
  public int computeCost(Sequence s1, Sequence s2, ScoreMatrix matrix, AffineGapCost gap) {
    validateInputs(s1, s2, matrix, gap);

    String a = s1.getResidues();
    String b = s2.getResidues();
    int n = a.length();
    int m = b.length();

    int alpha = gap.getAlpha();
    int beta = gap.getBeta();
    int openAndExtend = alpha + beta;

    int[][] d = new int[n + 1][m + 1];
    int[][] iLayer = new int[n + 1][m + 1];
    int[][] sLayer = new int[n + 1][m + 1];
    fillWithInfinity(d);
    fillWithInfinity(iLayer);
    fillWithInfinity(sLayer);

    // Empty-prefix base state.
    d[0][0] = 0;

    // First column: one contiguous gap of length i in sequence 2.
    for (int i = 1; i <= n; i++) {
      iLayer[i][0] = initialGapCost(alpha, beta, i);
    }
    // First row: one contiguous gap of length j in sequence 1.
    for (int j = 1; j <= m; j++) {
      sLayer[0][j] = initialGapCost(alpha, beta, j);
    }

    // Core affine recurrences over all interior cells.
    for (int i = 1; i <= n; i++) {
      for (int j = 1; j <= m; j++) {
        int substitution = matrix.cost(a.charAt(i - 1), b.charAt(j - 1));

        // D: close/continue with a diagonal symbol-symbol alignment.
        int bestDiagonalPredecessor = min3(d[i - 1][j - 1], iLayer[i - 1][j - 1], sLayer[i - 1][j - 1]);
        d[i][j] = safeAdd(bestDiagonalPredecessor, substitution);

        // I: gap in sequence 2, either open from D or extend from I.
        int openInsertion = safeAdd(d[i - 1][j], openAndExtend);
        int extendInsertion = safeAdd(iLayer[i - 1][j], beta);
        iLayer[i][j] = Math.min(openInsertion, extendInsertion);

        // S: gap in sequence 1, either open from D or extend from S.
        int openSkip = safeAdd(d[i][j - 1], openAndExtend);
        int extendSkip = safeAdd(sLayer[i][j - 1], beta);
        sLayer[i][j] = Math.min(openSkip, extendSkip);
      }
    }

    // Best global alignment can end in any layer.
    return min3(d[n][m], iLayer[n][m], sLayer[n][m]);
  }

  /**
   * Computes the optimal cost and reconstructs one optimal affine-gap alignment.
   *
   * @param s1 first sequence
   * @param s2 second sequence
   * @param matrix substitution/distance matrix
   * @param gap affine gap cost model
   * @return alignment result containing one optimal traceback
   * @throws NullPointerException if any input is {@code null}
   * @throws IllegalArgumentException if a sequence contains symbols outside the matrix alphabet
   */
  @Override
  public AlignmentResult align(Sequence s1, Sequence s2, ScoreMatrix matrix, AffineGapCost gap) {
    validateInputs(s1, s2, matrix, gap);

    String a = s1.getResidues();
    String b = s2.getResidues();
    int n = a.length();
    int m = b.length();

    int alpha = gap.getAlpha();
    int beta = gap.getBeta();
    int openAndExtend = alpha + beta;

    int[][] d = new int[n + 1][m + 1];
    int[][] iLayer = new int[n + 1][m + 1];
    int[][] sLayer = new int[n + 1][m + 1];
    int[][] backD = new int[n + 1][m + 1];
    int[][] backI = new int[n + 1][m + 1];
    int[][] backS = new int[n + 1][m + 1];

    fillWithInfinity(d);
    fillWithInfinity(iLayer);
    fillWithInfinity(sLayer);
    fillWithUnset(backD);
    fillWithUnset(backI);
    fillWithUnset(backS);

    // Empty-prefix base state.
    d[0][0] = 0;

    // First column: build one long gap in sequence 2; back pointers stay in I after opening.
    for (int i = 1; i <= n; i++) {
      iLayer[i][0] = initialGapCost(alpha, beta, i);
      backI[i][0] = (i == 1) ? LAYER_D : LAYER_I;
    }
    // First row: build one long gap in sequence 1; back pointers stay in S after opening.
    for (int j = 1; j <= m; j++) {
      sLayer[0][j] = initialGapCost(alpha, beta, j);
      backS[0][j] = (j == 1) ? LAYER_D : LAYER_S;
    }

    // Fill all three DP layers and remember the predecessor layer used by each state.
    for (int i = 1; i <= n; i++) {
      for (int j = 1; j <= m; j++) {
        int substitution = matrix.cost(a.charAt(i - 1), b.charAt(j - 1));

        // D layer predecessor is whichever of D/I/S at (i-1, j-1) is cheapest.
        int bestDiagonalPredecessor = d[i - 1][j - 1];
        int bestDiagonalLayer = LAYER_D;
        if (iLayer[i - 1][j - 1] < bestDiagonalPredecessor) {
          bestDiagonalPredecessor = iLayer[i - 1][j - 1];
          bestDiagonalLayer = LAYER_I;
        }
        if (sLayer[i - 1][j - 1] < bestDiagonalPredecessor) {
          bestDiagonalPredecessor = sLayer[i - 1][j - 1];
          bestDiagonalLayer = LAYER_S;
        }
        d[i][j] = safeAdd(bestDiagonalPredecessor, substitution);
        backD[i][j] = bestDiagonalLayer;

        // I layer: either open from D or extend in I.
        int openInsertion = safeAdd(d[i - 1][j], openAndExtend);
        int extendInsertion = safeAdd(iLayer[i - 1][j], beta);
        if (extendInsertion < openInsertion) {
          iLayer[i][j] = extendInsertion;
          backI[i][j] = LAYER_I;
        } else {
          iLayer[i][j] = openInsertion;
          backI[i][j] = LAYER_D;
        }

        // S layer: either open from D or extend in S.
        int openSkip = safeAdd(d[i][j - 1], openAndExtend);
        int extendSkip = safeAdd(sLayer[i][j - 1], beta);
        if (extendSkip < openSkip) {
          sLayer[i][j] = extendSkip;
          backS[i][j] = LAYER_S;
        } else {
          sLayer[i][j] = openSkip;
          backS[i][j] = LAYER_D;
        }
      }
    }

    // Traceback starts from the best layer at (n, m).
    int bestCost = d[n][m];
    int layer = LAYER_D;
    if (iLayer[n][m] < bestCost) {
      bestCost = iLayer[n][m];
      layer = LAYER_I;
    }
    if (sLayer[n][m] < bestCost) {
      bestCost = sLayer[n][m];
      layer = LAYER_S;
    }

    StringBuilder aligned1 = new StringBuilder();
    StringBuilder aligned2 = new StringBuilder();
    int i = n;
    int j = m;

    // Follow layer-specific back pointers until the origin.
    while (i > 0 || j > 0) {
      if (layer == LAYER_D) {
        if (i <= 0 || j <= 0) {
          throw new IllegalStateException("Invalid D-layer traceback state at (" + i + "," + j + ")");
        }
        aligned1.append(a.charAt(i - 1));
        aligned2.append(b.charAt(j - 1));
        int previousLayer = backD[i][j];
        i--;
        j--;
        layer = previousLayer;
      } else if (layer == LAYER_I) {
        if (i <= 0) {
          throw new IllegalStateException("Invalid I-layer traceback state at (" + i + "," + j + ")");
        }
        aligned1.append(a.charAt(i - 1));
        aligned2.append('-');
        int previousLayer = backI[i][j];
        i--;
        layer = previousLayer;
      } else if (layer == LAYER_S) {
        if (j <= 0) {
          throw new IllegalStateException("Invalid S-layer traceback state at (" + i + "," + j + ")");
        }
        aligned1.append('-');
        aligned2.append(b.charAt(j - 1));
        int previousLayer = backS[i][j];
        j--;
        layer = previousLayer;
      } else {
        throw new IllegalStateException("Traceback layer not set at (" + i + "," + j + ")");
      }
    }

    return new AlignmentResult(bestCost, aligned1.reverse().toString(), aligned2.reverse().toString());
  }

  /** Validates inputs before dynamic-programming computation. */
  private static void validateInputs(Sequence s1, Sequence s2, ScoreMatrix matrix, AffineGapCost gap) {
    Objects.requireNonNull(s1, "s1 must not be null");
    Objects.requireNonNull(s2, "s2 must not be null");
    Objects.requireNonNull(matrix, "score matrix must not be null");
    Objects.requireNonNull(gap, "gap cost must not be null");
    matrix.alphabet().validate(s1.getResidues());
    matrix.alphabet().validate(s2.getResidues());
  }

  /** Fills all cells of a 2D int array with the infinity sentinel. */
  private static void fillWithInfinity(int[][] matrix) {
    for (int[] row : matrix) {
      Arrays.fill(row, INF);
    }
  }

  /** Fills all cells of a 2D int array with unset layer markers. */
  private static void fillWithUnset(int[][] matrix) {
    for (int[] row : matrix) {
      Arrays.fill(row, LAYER_UNSET);
    }
  }

  /** Computes alpha + beta * length and clamps unreachable/overflow values to INF. */
  private static int initialGapCost(int alpha, int beta, int length) {
    long value = (long) alpha + (long) beta * length;
    return value >= INF ? INF : (int) value;
  }

  /** Adds a delta to a DP value while preserving INF as an unreachable sentinel. */
  private static int safeAdd(int value, int delta) {
    if (value >= INF) {
      return INF;
    }
    long sum = (long) value + delta;
    if (sum >= INF) {
      return INF;
    }
    if (sum <= -INF) {
      return -INF;
    }
    return (int) sum;
  }

  /** Returns the minimum of three int values. */
  private static int min3(int a, int b, int c) {
    return Math.min(a, Math.min(b, c));
  }
}
