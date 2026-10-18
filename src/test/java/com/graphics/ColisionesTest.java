package com.graphics;

import junit.framework.TestCase;

public class ColisionesTest extends TestCase {
    public void testAutosPuedenPasarCercaPorElCostado() {
        assertFalse(Colisiones.autos(0, 0, 0, 2.15f, 0, 0));
        assertTrue(Colisiones.autos(0, 0, 0, 2, 0, 0));
        assertFalse(Colisiones.autos(0, 0, 0, 0, 3, 0));
        assertTrue(Colisiones.autos(0, 0, 0, 0, 2.85f, 0));
    }

    public void testRespetaOrientacionYEsSimetrico() {
        float giro = (float) Math.PI / 2;
        assertFalse(Colisiones.autos(0, 0, giro, 0, 2.15f, giro));
        assertTrue(Colisiones.autos(0, 0, giro, 2.85f, 0, giro));
        for (float a = -3; a < 3; a += 0.3f) {
            assertEquals(Colisiones.autos(0, 0, a, 2, 1, 0.5f),
                         Colisiones.autos(2, 1, 0.5f, 0, 0, a));
        }
    }

    public void testChataContraCostadoDelAuto() {
        assertFalse(Colisiones.circuloAuto(2.3f, 0, Chata.RADIO, 0, 0, 0));
        assertTrue(Colisiones.circuloAuto(2.1f, 0, Chata.RADIO, 0, 0, 0));
    }
}
