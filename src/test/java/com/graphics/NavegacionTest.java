package com.graphics;

import junit.framework.TestCase;

public class NavegacionTest extends TestCase {
    public void testRumboSegunLaOrientacionDelAuto() {
        clase4 juego = new clase4();
        juego.autoX = 50;
        juego.autoZ = -40;
        juego.angulo = 0;
        assertEquals(0f, juego.rumboDestino(), 0.0001f);
        assertEquals("Sigue adelante", juego.textoRumbo());
        juego.angulo = (float) Math.PI / 2;
        assertEquals("A tu derecha", juego.textoRumbo());
        juego.angulo = -(float) Math.PI / 2;
        assertEquals("A tu izquierda", juego.textoRumbo());
        juego.angulo = (float) Math.PI;
        assertEquals("Detras de ti", juego.textoRumbo());
        juego.autoZ = -50;
        assertEquals(0f, juego.rumboDestino(), 0f);
    }

    public void testUnSoloChoqueHastaSepararse() {
        clase4 juego = new clase4();
        juego.alChocar(5);
        for (int i = 0; i < 20; i++) {
            juego.contactoActual = true;
            juego.actualizarContacto(0.05f);
            juego.alChocar(5);
        }
        assertEquals(1, juego.choques);
        juego.contactoActual = false;
        juego.actualizarContacto(0.3f);
        juego.alChocar(5);
        assertEquals(2, juego.choques);
    }

    public void testResumenCongeladoYReinicio() {
        clase4 juego = new clase4();
        juego.tiempo = 125;
        juego.choques = 2;
        juego.entregas = 3;
        String resumen = juego.resumenFinal();
        assertTrue(resumen.contains("125 s"));
        assertTrue(resumen.contains("Entregas: 3/3"));
        assertTrue(resumen.contains("Choques: 2"));
        juego.alChocar(5);
        juego.actualizarEntregas(1);
        assertEquals(resumen, juego.resumenFinal());
        juego.reiniciar();
        assertEquals(0, juego.choques);
    }
}
