package com.graphics;

import junit.framework.TestCase;
import static org.lwjgl.glfw.GLFW.*;

public class InteraccionTest extends TestCase {
    private static class Juego extends clase4 {
        @Override protected boolean pulsada(int tecla) { return false; }
        @Override protected void actualizarTitulo() { }
    }

    public void testInicioPausaYBotones() {
        Juego juego = new Juego();
        juego.enInicio = true;
        juego.velocidad = 4;
        juego.actualizar(0.1f);
        assertEquals(30f, juego.autoZ, 0f);
        assertEquals(0f, juego.tiempo, 0f);
        juego.accionMenu(0);
        juego.tecla(GLFW_KEY_P);
        assertTrue(juego.pausa);
        juego.tecla(GLFW_KEY_ENTER);
        assertFalse(juego.pausa);
        juego.accionMenu(2);
        assertTrue(juego.silenciado);
        juego.entregas = 2;
        juego.accionMenu(1);
        assertEquals(0, juego.entregas);
        assertFalse(juego.enInicio);
    }

    public void testEntregaYAvisoDeChoque() {
        Juego juego = new Juego();
        juego.autoX = 50;
        juego.autoZ = -46;
        assertEquals("Detente para entregar", juego.instruccionDestino());
        juego.autoZ = -50;
        juego.actualizarEntregas(0.1f);
        assertEquals(1, juego.entregas);
        assertEquals(2.5f, juego.avisoEntrega, 0f);
        juego.alChocar(0.5f);
        assertEquals(0f, juego.avisoChoque, 0f);
        juego.alChocar(5);
        assertTrue(juego.avisoChoque > 0);
        juego.reiniciar();
        assertEquals(0f, juego.avisoChoque, 0f);
    }

    public void testBotonesCabenenVentana() {
        for (int[] ventana : new int[][] {{1100, 760}, {600, 900}, {900, 600}, {320, 240}}) {
            int anterior = ventana[1];
            for (int i = 0; i < 3; i++) {
                int[] r = PanelInfo.rectMenu(ventana[0], ventana[1], i);
                assertTrue(r[0] >= 0 && r[1] >= 0);
                assertTrue(r[0] + r[2] <= ventana[0] && r[1] + r[3] <= anterior);
                anterior = r[1];
            }
        }
    }

    public void testAudioGeneradoSinSaturacion() {
        for (boolean choque : new boolean[] {false, true}) {
            for (boolean victoria : new boolean[] {false, true}) {
                float[] datos = SonidoJuego.crearEfecto(choque, victoria);
                assertTrue(datos.length > 3000);
                float energia = 0;
                for (float valor : datos) {
                    assertTrue(Float.isFinite(valor) && Math.abs(valor) <= 1);
                    energia += Math.abs(valor);
                }
                assertTrue(energia > 1);
            }
        }
    }

    public void testRuedasRetornanSuavementeAlCentro() {
        Juego juego = new Juego();
        juego.giroRuedas = 0.24f;
        juego.actualizar(0.05f);
        assertTrue(juego.giroRuedas > 0 && juego.giroRuedas < 0.24f);
    }
}
