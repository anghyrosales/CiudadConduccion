package com.graphics;

import junit.framework.TestCase;
import static org.lwjgl.glfw.GLFW.*;

public class CamaraTest extends TestCase {
    public void testArrastreLimitesYSinSaltosAlVolverAPulsar() {
        Juego juego = new Juego();
        juego.moverRaton(100, 200);
        assertEquals(2.8f, juego.alturaCamara, 0.001f);
        juego.botonRaton(GLFW_MOUSE_BUTTON_RIGHT, GLFW_PRESS);
        juego.moverRaton(300, 200);
        juego.moverRaton(400, 100);
        assertEquals(4.3f, juego.alturaCamara, 0.001f);
        assertEquals(0.6f, juego.orbitaSeguimiento, 0.001f);
        juego.moverRaton(400, -2000);
        assertEquals(8f, juego.alturaCamara, 0.001f);
        juego.moverRaton(400, 2000);
        assertEquals(1.6f, juego.alturaCamara, 0.001f);
        juego.botonRaton(GLFW_MOUSE_BUTTON_RIGHT, GLFW_RELEASE);
        juego.moverRaton(20, 20);
        assertEquals(1.6f, juego.alturaCamara, 0.001f);
        juego.botonRaton(GLFW_MOUSE_BUTTON_RIGHT, GLFW_PRESS);
        juego.moverRaton(900, 900);
        assertEquals(1.6f, juego.alturaCamara, 0.001f);
    }

    public void testVistaSePuedeAjustarEnPausaSinMoverLaPartida() {
        Juego juego = new Juego();
        juego.pausa = true;
        juego.subir = true;
        juego.actualizar(0.1f);
        assertEquals(3.1f, juego.alturaCamara, 0.001f);
        assertEquals(0f, juego.tiempo, 0f);
        assertEquals(0f, juego.tiempoUrbano, 0f);
        assertEquals(-30f, juego.autoX, 0f);
        juego.camaraAerea = true;
        juego.actualizar(0.1f);
        assertEquals(3.1f, juego.alturaCamara, 0.001f);
        juego.tecla(GLFW_KEY_HOME);
        assertEquals(2.8f, juego.alturaCamara, 0.001f);
        assertEquals(0f, juego.orbitaSeguimiento, 0.001f);
    }

    public void testOrbitaNoCambiaDireccionYNuncaEntraEnUnaFachada() {
        Juego juego = new Juego();
        juego.autoX = -10;
        juego.autoZ = 0;
        juego.ajustarVista(1000, 0);
        assertTrue(Math.abs(juego.orbitaSeguimiento) <= Math.PI);
        assertEquals(0f, juego.angulo, 0f);
        float distancia = juego.distanciaVista((float) Math.PI / 2);
        assertTrue(distancia < 8.5f);
        assertTrue(distancia >= 2);
    }

    private static class Juego extends clase4 {
        boolean subir;

        @Override
        protected boolean pulsada(int key) {
            return subir && key == GLFW_KEY_PAGE_UP;
        }

        @Override
        protected void actualizarTitulo() {

        }
    }
}
