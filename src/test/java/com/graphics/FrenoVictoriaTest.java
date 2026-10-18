package com.graphics;

import junit.framework.TestCase;
import static org.lwjgl.glfw.GLFW.*;

public class FrenoVictoriaTest extends TestCase {
    public void testEspacioDetieneAunqueSeMantengaElAcelerador() {
        Juego juego = new Juego();
        juego.velocidad = 10;
        juego.espacio = true;
        juego.acelerador = true;
        for (int i = 0; i < 20; i++) {
            juego.actualizar(0.05f);
        }
        assertEquals(0f, juego.velocidad, 0f);
        assertTrue(juego.frenando);
        float x = juego.autoX;
        float z = juego.autoZ;
        juego.acelerador = false;
        juego.espacio = false;
        for (int i = 0; i < 20; i++) {
            juego.actualizar(0.05f);
        }
        assertEquals(x, juego.autoX, 0f);
        assertEquals(z, juego.autoZ, 0f);
    }

    public void testBotonFrenaReversaYSoltarFueraLiberaElPedal() {
        Juego juego = new Juego();
        int[] r = PanelInfo.rectFreno(juego.ancho, juego.alto);
        juego.moverRaton(r[0] + r[2] / 2.0, juego.alto - r[1] - r[3] / 2.0);
        juego.botonRaton(GLFW_MOUSE_BUTTON_LEFT, GLFW_PRESS);
        assertTrue(juego.frenoPorBoton);
        juego.velocidad = -5;
        for (int i = 0; i < 10; i++) {
            juego.actualizar(0.05f);
            assertTrue(juego.velocidad <= 0);
        }
        assertEquals(0f, juego.velocidad, 0f);
        juego.moverRaton(20, 20);
        juego.botonRaton(GLFW_MOUSE_BUTTON_LEFT, GLFW_RELEASE);
        assertFalse(juego.frenoPorBoton);
        juego.botonRaton(GLFW_MOUSE_BUTTON_LEFT, GLFW_PRESS);
        assertFalse(juego.frenoPorBoton);
    }

    public void testAceleracionNoQuedaBloqueadaConCuadrosMuyCortos() {
        Juego juego = new Juego();
        juego.acelerador = true;
        for (int i = 0; i < 100; i++) {
            juego.actualizar(0.001f);
        }
        assertTrue(juego.velocidad > 0.5f);
    }

    public void testVictoriaSoloAlCompletarTodasYReinicioLaElimina() {
        Juego juego = new Juego();
        assertFalse(juego.tieneVictoria());
        for (int i = 0; i < clase4.DESTINOS.length; i++) {
            juego.autoX = clase4.DESTINOS[i][0];
            juego.autoZ = clase4.DESTINOS[i][1];
            juego.actualizarEntregas(0.05f);
            assertEquals(i == clase4.DESTINOS.length - 1, juego.tieneVictoria());
        }
        juego.frenoPorBoton = true;
        juego.reiniciar();
        assertFalse(juego.tieneVictoria());
        assertFalse(juego.frenoPorBoton);
    }

    private static class Juego extends clase4 {
        boolean espacio;
        boolean acelerador;

        @Override
        protected boolean pulsada(int key) {
            return key == GLFW_KEY_SPACE && espacio || key == GLFW_KEY_W && acelerador;
        }

        @Override
        protected void actualizarTitulo() {

        }
    }
}
