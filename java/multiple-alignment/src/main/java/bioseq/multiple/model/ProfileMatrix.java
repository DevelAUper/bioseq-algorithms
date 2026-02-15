package bioseq.multiple.model;

import bioseq.core.scoring.Alphabet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Residue-frequency profile computed from an existing aligned sequence set.
 *
 * <p>A profile matrix summarizes each alignment column by storing how frequently each residue
 * occurs at that position across all aligned sequences. This is a common building block for
 * progressive and profile-based multiple alignment methods.
 */
public final class ProfileMatrix {
  private final int[][] counts;
  private final Map<Character, Integer> residueIndex;
  private final int length;
  private final int sequenceCount;

  /**
   * Builds a profile from aligned sequences.
   *
   * @param alignedSequences aligned sequence strings (same length, gaps allowed as {@code '-'})
   * @param alphabet allowed residue alphabet (gap is handled separately)
   * @throws NullPointerException if any input is {@code null}
   * @throws IllegalArgumentException if aligned sequences are empty, lengths differ, or contain
   *     residues outside the alphabet
   */
  public ProfileMatrix(List<String> alignedSequences, Alphabet alphabet) {
    Objects.requireNonNull(alignedSequences, "alignedSequences must not be null");
    Objects.requireNonNull(alphabet, "alphabet must not be null");
    if (alignedSequences.isEmpty()) {
      throw new IllegalArgumentException("alignedSequences must contain at least one sequence");
    }

    int alignedLength = -1;
    for (int i = 0; i < alignedSequences.size(); i++) {
      String seq = Objects.requireNonNull(
          alignedSequences.get(i), "aligned sequence at index " + i + " must not be null");
      if (alignedLength == -1) {
        alignedLength = seq.length();
      } else if (seq.length() != alignedLength) {
        throw new IllegalArgumentException(
            "All aligned sequences must have equal length; expected "
                + alignedLength + " but found " + seq.length() + " at index " + i);
      }
    }

    this.length = alignedLength;
    this.sequenceCount = alignedSequences.size();

    this.residueIndex = new LinkedHashMap<>();
    int idx = 0;
    for (char symbol : alphabet.symbols()) {
      residueIndex.put(symbol, idx++);
    }
    this.counts = new int[length][residueIndex.size()];

    for (String aligned : alignedSequences) {
      for (int pos = 0; pos < aligned.length(); pos++) {
        char residue = Character.toUpperCase(aligned.charAt(pos));
        if (residue == '-') {
          continue;
        }
        Integer residueIdx = residueIndex.get(residue);
        if (residueIdx == null) {
          throw new IllegalArgumentException(
              "Invalid residue '" + residue + "' at alignment position " + pos);
        }
        counts[pos][residueIdx]++;
      }
    }
  }

  /**
   * Returns residue frequency at a given profile position.
   *
   * @param position zero-based alignment/profile column index
   * @param residue residue symbol to query
   * @return fraction in [0,1] equal to occurrences / sequenceCount
   * @throws IllegalArgumentException if position is out of range or residue is not in the alphabet
   */
  public double frequency(int position, char residue) {
    if (position < 0 || position >= length) {
      throw new IllegalArgumentException(
          "position must be in [0," + (length - 1) + "], got: " + position);
    }
    char normalized = Character.toUpperCase(residue);
    Integer idx = residueIndex.get(normalized);
    if (idx == null) {
      throw new IllegalArgumentException("Residue not in profile alphabet: '" + normalized + "'");
    }
    return counts[position][idx] / (double) sequenceCount;
  }

  /**
   * Returns profile length (number of alignment columns).
   *
   * @return profile length
   */
  public int length() {
    return length;
  }

  /**
   * Returns number of aligned sequences used to build this profile.
   *
   * @return aligned-sequence count
   */
  public int sequenceCount() {
    return sequenceCount;
  }
}
