package com.graphics;

import java.util.ArrayDeque;
import junit.framework.TestCase;

public class CiudadAmpliadaTest extends TestCase {
    public void testCantidadYConectividad() {
        int[][] mapa = clase1.MAPA;
        assertTrue(mapa.length >= 11);
        int edificios = 0;
        int parques = 0;
        for (int[] fila : mapa) {
            assertEquals(mapa.length, fila.length);
            for (int tipo : fila) {
                if (tipo == 1) { edificios++; }
                if (tipo == 2) { parques++; }
            }
        }
        assertEquals(21, edificios);
        assertEquals(4, parques);
        assertEquals(55f, clase1.LIMITE, 0);
        assertTrue(clase3.LUCES.length >= 9);
        boolean[][] visitado = new boolean[mapa.length][mapa.length];
        ArrayDeque<int[]> pendientes = new ArrayDeque<>();
        pendientes.add(new int[] {8, 2});
        visitado[8][2] = true;
        int[][] direcciones = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!pendientes.isEmpty()) {
            int[] celda = pendientes.remove();
            for (int[] direccion : direcciones) {
                int fila = celda[0] + direccion[0];
                int columna = celda[1] + direccion[1];
                if (fila >= 0 && columna >= 0 && fila < mapa.length && columna < mapa.length
                    && mapa[fila][columna] == 0 && !visitado[fila][columna]) {
                    visitado[fila][columna] = true;
                    pendientes.add(new int[] {fila, columna});
                }
            }
        }
        clase2 ciudad = new clase2();
        for (float[] destino : clase4.DESTINOS) {
            int columna = (int) ((destino[0] + clase1.LIMITE) / clase1.CELDA);
            int fila = (int) ((destino[1] + clase1.LIMITE) / clase1.CELDA);
            assertTrue(visitado[fila][columna]);
            assertTrue(ciudad.puedeCircular(destino[0], destino[1]));
        }
        for (int fila = 0; fila < mapa.length; fila++) {
            for (int columna = 0; columna < mapa.length; columna++) {
                if (mapa[fila][columna] == 0) {
                    assertTrue("Calle aislada", visitado[fila][columna]);
                } else {
                    assertFalse(ciudad.puedeCircular(clase1.centro(columna), clase1.centro(fila)));
                }
            }
        }
    }

    public void testEntregasExigenFrenarYSePuedenRepetir() {
        clase4 juego = new clase4();
        for (int partida = 0; partida < 2; partida++) {
            juego.reiniciar();
            for (int i = 0; i < clase4.DESTINOS.length; i++) {
                juego.autoX = clase4.DESTINOS[i][0];
                juego.autoZ = clase4.DESTINOS[i][1];
                juego.velocidad = 2;
                juego.actualizarEntregas(0.1f);
                assertEquals(i, juego.entregas);
                juego.velocidad = 0;
                juego.actualizarEntregas(0.1f);
                assertEquals(i + 1, juego.entregas);
            }
            float finalPartida = juego.tiempo;
            juego.actualizarEntregas(1);
            assertEquals(finalPartida, juego.tiempo, 0);
            juego.tiempoUrbano += 1;
            juego.actualizarTrafico(0.05f);
        }
    }
}
