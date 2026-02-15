package bioseq.pairwise.global;

import bioseq.core.gap.AffineGapCost;
import bioseq.core.model.Sequence;
import bioseq.core.scoring.ScoreMatrix;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

/**
 * Counts optimal global alignments under an affine gap model.
 *
 * <p>The implementation mirrors the three-layer affine cost DP:
 * <ul>
 *   <li>{@code D}: diagonal state (residue-residue)</li>
 *   <li>{@code I}: gap in sequence 2</li>
 *   <li>{@code S}: gap in sequence 1</li>
 * </ul>
 *
 * <p>Each layer has both a cost table and a count table. A count cell stores the number of ways
 * to realize that cell's optimal cost in that layer.
 */
public final class OptimalAffineAlignmentCounter {
  private static final int INF = Integer.MAX_VALUE / 2;

  /**
   * Counts how many optimal affine-gap global alignments exist.
   *
   * @param s1 first sequence
   * @param s2 second sequence
   * @param matrix substitution/distance matrix
   * @param gap affine gap model
   * @return number of optimal alignments as {@link BigInteger}
   * @throws NullPointerException if any input is {@code null}
   * @throws IllegalArgumentException if a sequence contains symbols outside the matrix alphabet
   */
  public BigInteger countOptimalAlignments(Sequence s1, Sequence s2, ScoreMatrix matrix, AffineGapCost gap) {
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
    BigInteger[][] countD = new BigInteger[n + 1][m + 1];
    BigInteger[][] countI = new BigInteger[n + 1][m + 1];
    BigInteger[][] countS = new BigInteger[n + 1][m + 1];

    fillWithInfinity(d);
    fillWithInfinity(iLayer);
    fillWithInfinity(sLayer);
    fillWithZeroCounts(countD);
    fillWithZeroCounts(countI);
    fillWithZeroCounts(countS);

    // Base case: one way to align two empty prefixes in the diagonal state.
    d[0][0] = 0;
    countD[0][0] = BigInteger.ONE;

    // First column: exactly one way to build a single contiguous gap in sequence 2.
    for (int i = 1; i <= n; i++) {
      iLayer[i][0] = initialGapCost(alpha, beta, i);
      countI[i][0] = BigInteger.ONE;
    }

    // First row: exactly one way to build a single contiguous gap in sequence 1.
    for (int j = 1; j <= m; j++) {
      sLayer[0][j] = initialGapCost(alpha, beta, j);
      countS[0][j] = BigInteger.ONE;
    }

    // Interior cells: compute optimal layer costs, then sum counts from optimal predecessors only.
    for (int i = 1; i <= n; i++) {
      for (int j = 1; j <= m; j++) {
        int substitution = matrix.cost(a.charAt(i - 1), b.charAt(j - 1));

        int fromDDiag = safeAdd(d[i - 1][j - 1], substitution);
        int fromIDiag = safeAdd(iLayer[i - 1][j - 1], substitution);
        int fromSDiag = safeAdd(sLayer[i - 1][j - 1], substitution);
        d[i][j] = min3(fromDDiag, fromIDiag, fromSDiag);

        BigInteger dCount = BigInteger.ZERO;
        if (fromDDiag == d[i][j]) {
          dCount = dCount.add(countD[i - 1][j - 1]);
        }
        if (fromIDiag == d[i][j]) {
          dCount = dCount.add(countI[i - 1][j - 1]);
        }
        if (fromSDiag == d[i][j]) {
          dCount = dCount.add(countS[i - 1][j - 1]);
        }
        countD[i][j] = dCount;

        int fromDI = safeAdd(d[i - 1][j], openAndExtend);
        int fromII = safeAdd(iLayer[i - 1][j], beta);
        iLayer[i][j] = Math.min(fromDI, fromII);

        BigInteger iCount = BigInteger.ZERO;
        if (fromDI == iLayer[i][j]) {
          iCount = iCount.add(countD[i - 1][j]);
        }
        if (fromII == iLayer[i][j]) {
          iCount = iCount.add(countI[i - 1][j]);
        }
        countI[i][j] = iCount;

        int fromDS = safeAdd(d[i][j - 1], openAndExtend);
        int fromSS = safeAdd(sLayer[i][j - 1], beta);
        sLayer[i][j] = Math.min(fromDS, fromSS);

        BigInteger sCount = BigInteger.ZERO;
        if (fromDS == sLayer[i][j]) {
          sCount = sCount.add(countD[i][j - 1]);
        }
        if (fromSS == sLayer[i][j]) {
          sCount = sCount.add(countS[i][j - 1]);
        }
        countS[i][j] = sCount;
      }
    }

    // Final count is the sum over all layers that achieve the global minimum cost.
    int best = min3(d[n][m], iLayer[n][m], sLayer[n][m]);
    BigInteger total = BigInteger.ZERO;
    if (d[n][m] == best) {
      total = total.add(countD[n][m]);
    }
    if (iLayer[n][m] == best) {
      total = total.add(countI[n][m]);
    }
    if (sLayer[n][m] == best) {
      total = total.add(countS[n][m]);
    }
    return total;
  }

  /** Validates inputs before DP computation. */
  private static void validateInputs(Sequence s1, Sequence s2, ScoreMatrix matrix, AffineGapCost gap) {
    Objects.requireNonNull(s1, "s1 must not be null");
    Objects.requireNonNull(s2, "s2 must not be null");
    Objects.requireNonNull(matrix, "score matrix must not be null");
    Objects.requireNonNull(gap, "gap cost must not be null");
    matrix.alphabet().validate(s1.getResidues());
    matrix.alphabet().validate(s2.getResidues());
  }

  /** Fills an int matrix with the infinity sentinel. */
  private static void fillWithInfinity(int[][] matrix) {
    for (int[] row : matrix) {
      Arrays.fill(row, INF);
    }
  }

  /** Fills a BigInteger matrix with zeros. */
  private static void fillWithZeroCounts(BigInteger[][] matrix) {
    for (BigInteger[] row : matrix) {
      Arrays.fill(row, BigInteger.ZERO);
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
