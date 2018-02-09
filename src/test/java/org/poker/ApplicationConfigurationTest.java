package org.poker;


import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ApplicationConfigurationTest {
  @Test
  public void gammaIsDefault() {
    assertEquals(Stage.Gamma, new ApplicationConfiguration().getStage());
  }
}
