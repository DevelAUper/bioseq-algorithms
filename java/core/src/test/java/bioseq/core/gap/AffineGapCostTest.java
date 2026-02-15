package bioseq.core.gap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AffineGapCostTest {
  @Test
  void testCostZero() {
    AffineGapCost gap = new AffineGapCost(10, 3);
    assertEquals(0, gap.cost(0));
  }

  @Test
  void testCostOne() {
    AffineGapCost gap = new AffineGapCost(10, 3);
    assertEquals(13, gap.cost(1));
  }

  @Test
  void testCostThree() {
    AffineGapCost gap = new AffineGapCost(10, 3);
    assertEquals(19, gap.cost(3));
  }

  @Test
  void testNegativeAlphaThrows() {
    assertThrows(IllegalArgumentException.class, () -> new AffineGapCost(-1, 3));
  }

  @Test
  void testNegativeLengthThrows() {
    AffineGapCost gap = new AffineGapCost(10, 3);
    assertThrows(IllegalArgumentException.class, () -> gap.cost(-1));
  }
}
