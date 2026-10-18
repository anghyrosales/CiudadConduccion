package com.graphics;

import junit.framework.TestCase;

public class AcerasTest extends TestCase {
    public void testAceraTransitableSinAtravesarCasa() {
        clase2 ciudad = new clase2();
        assertTrue(ciudad.puedeCircular(-24.9f, -40));
        assertEquals(0.3f, ciudad.alturaSuelo(-24.9f, -40), 0f);
        assertEquals(0f, ciudad.alturaSuelo(-26, -40), 0f);
        assertFalse(ciudad.puedeCircular(-21.8f, -39.65f));
        assertFalse(ciudad.puedeCircular(-20, -35.85f));
    }

    public void testChataPuedePisarAceraPeroNoEdificios() {
        clase2 ciudad = new clase2();
        assertTrue(ciudad.puedeCircularChata(-24.5f, -40, Chata.RADIO));
        assertFalse(ciudad.puedeCircularChata(-21.8f, -39.65f, Chata.RADIO));
        assertTrue(ciudad.puedeCircular(20, 35.4f));
        assertFalse(ciudad.puedeCircular(20, 40));
    }

    public void testParqueSigueProtegidoHastaElCesped() {
        clase2 ciudad = new clase2();
        assertFalse(ciudad.puedeCircular(-40, -40));
        assertFalse(ciudad.puedeCircular(-40, -35.8f));

        assertTrue(ciudad.puedeCircularCiudad(-40, -35.2f, 0.1f));
        assertFalse(ciudad.puedeCircularCiudad(-40, -35.6f, 0.1f));
        assertFalse(ciudad.puedeCircular(54, -50));
    }
}
