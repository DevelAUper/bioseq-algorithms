package bioseq.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import bioseq.core.gap.LinearGapCost;
import bioseq.core.model.Sequence;
import org.junit.jupiter.api.Test;

public class CoreSmokeTest {
  @Test
  void sequencePropertiesAreExposed() {
    Sequence sequence = new Sequence("test", "ACGT");

    assertEquals("test", sequence.getId());
    assertEquals("ACGT", sequence.getResidues());
    assertEquals(4, sequence.length());
  }

  @Test
  void linearGapCostComputesExpectedValues() {
    LinearGapCost gap = new LinearGapCost(3);

    assertEquals(0, gap.cost(0));
    assertEquals(3, gap.cost(1));
    assertEquals(12, gap.cost(4));
  }
}
