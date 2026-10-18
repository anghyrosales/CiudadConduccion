package com.graphics;

import java.util.HashSet;
import java.util.Set;
import junit.framework.TestCase;

public class ModelosTest extends TestCase {
    public void testMallasTienenNormalesValidasYExteriores() {
        float[][] mallas = {Malla.prisma(0.86f, 0.94f), Malla.prisma(0.76f, 0.44f),
                            Malla.prisma(0, 1), Malla.cilindro(24)};
        for (float[] datos : mallas) {
            assertTrue(datos.length > 0);
            assertEquals(0, datos.length % 18);
            for (int i = 0; i < datos.length; i += 6) {
                for (int j = 0; j < 6; j++) {
                    assertTrue(Float.isFinite(datos[i + j]));
                }
                float nx = datos[i + 3];
                float ny = datos[i + 4];
                float nz = datos[i + 5];
                assertEquals(1f, nx * nx + ny * ny + nz * nz, 0.0001f);
                assertTrue(datos[i] * nx + datos[i + 1] * ny + datos[i + 2] * nz > 0.05f);
            }
        }
    }

    public void testTodosLosModelosQuedanDentroDeSuManzana() {
        Registro registro = new Registro();
        Set<Integer> tipos = new HashSet<>();
        for (int fila = 0; fila < clase1.MAPA.length; fila++) {
            for (int columna = 0; columna < clase1.MAPA[fila].length; columna++) {
                if (clase1.MAPA[fila][columna] != 1) {
                    continue;
                }
                tipos.add(ModelosEdificios.tipo(fila, columna));
                registro.centroX = clase1.centro(columna);
                registro.centroZ = clase1.centro(fila);
                ModelosEdificios.dibujar(registro, fila, columna);
            }
        }
        assertEquals(4, tipos.size());
        assertTrue(registro.piezas > 100);
    }

    public void testDeportivoCabeEnElRadioConRuedasGiradasYModeloRotado() {
        Registro registro = new Registro();
        registro.autoX = 0;
        registro.autoZ = 0;
        registro.centroX = 0;
        registro.centroZ = 0;
        registro.auto = true;
        for (float angulo : new float[] {0, 0.7f, 1.57f, -2.1f}) {
            for (float volante : new float[] {-0.24f, 0, 0.24f}) {
                registro.angulo = angulo;
                registro.giroRuedas = volante;
                registro.rotacionRuedas = 0.65f;
                new ModeloAuto(registro).dibujar();
            }
        }
    }

    public void testRuedasRespondenAlMovimientoYLucesAlFrenado() {
        ConduccionSimulada juego = new ConduccionSimulada();
        juego.velocidad = 4;
        juego.actualizar(0.05f);
        assertTrue(juego.rotacionRuedas > 0);
        juego.freno = true;
        juego.actualizar(0.05f);
        assertTrue(juego.frenando);
        juego.reiniciar();
        assertFalse(juego.frenando);
        assertEquals(0f, juego.rotacionRuedas, 0f);
        assertEquals(0f, juego.giroRuedas, 0f);
        juego.autoX = 50;
        juego.autoZ = -53.3f;
        juego.velocidad = 10;
        juego.freno = false;
        juego.actualizar(0.05f);
        assertEquals(0f, juego.velocidad, 0f);
        assertEquals(0f, juego.rotacionRuedas, 0f);
    }

    private static class ConduccionSimulada extends clase2 {
        boolean freno;

        @Override
        protected boolean pulsada(int key) {
            return freno && key == org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
        }

        @Override
        protected void actualizarTitulo() {

        }
    }

    private static class Registro extends clase2 {
        float centroX;
        float centroZ;
        boolean auto;
        int piezas;

        @Override
        protected void entero(String nombre, int valor) {

        }

        @Override
        protected void cajaGirada(float x, float y, float z, float sx, float sy, float sz,
                                  float r, float g, float b, float giro) {
            for (float px : new float[] {-0.5f, 0.5f}) {
                for (float py : new float[] {-0.5f, 0.5f}) {
                    for (float pz : new float[] {-0.5f, 0.5f}) {
                        comprobar(px, py, pz, x, y, z, sx, sy, sz, giro, 0, 0);
                    }
                }
            }
            piezas++;
        }

        @Override
        protected void malla(Malla malla, float x, float y, float z, float sx, float sy, float sz,
                             float r, float g, float b, float giroY, float giroX, float giroZ) {
            for (int i = 0; i < malla.vertices.length; i += 6) {
                comprobar(malla.vertices[i], malla.vertices[i + 1], malla.vertices[i + 2],
                           x, y, z, sx, sy, sz, giroY, giroX, giroZ);
            }
            piezas++;
        }

        private void comprobar(float px, float py, float pz, float x, float y, float z,
                                float sx, float sy, float sz, float ry, float rx, float rz) {
            float ax = px * sx;
            float ay = py * sy;
            float az = pz * sz;
            float by = (float) Math.cos(rx) * ay - (float) Math.sin(rx) * az;
            float bz = (float) Math.sin(rx) * ay + (float) Math.cos(rx) * az;
            float cx = (float) Math.cos(rz) * ax - (float) Math.sin(rz) * by;
            float cy = (float) Math.sin(rz) * ax + (float) Math.cos(rz) * by;
            float mundialX = x + (float) Math.cos(ry) * cx + (float) Math.sin(ry) * bz;
            float mundialZ = z - (float) Math.sin(ry) * cx + (float) Math.cos(ry) * bz;
            if (auto) {
                assertTrue("Pieza fuera del círculo: " + mundialX + ", " + mundialZ,
                           Math.hypot(mundialX - centroX, mundialZ - centroZ) <= RADIO_AUTO + 0.001f);
            } else {
                assertTrue("Edificio invade calle en X", Math.abs(mundialX - centroX) < CELDA / 2);
                assertTrue("Edificio invade calle en Z", Math.abs(mundialZ - centroZ) < CELDA / 2);
                assertTrue(y + cy >= 0);
                assertTrue("Edificio oculta indicador del mapa", y + cy < 24);
            }
        }
    }
}
