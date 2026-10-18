package com.graphics;

import junit.framework.TestCase;

public class TraficoTest extends TestCase {
    private final clase2 ciudad = new clase2();

    public void testSemaforosCoordinados() {
        Cruce cruce = new Cruce(10, 10);
        for (float tiempo = 0; tiempo < 60; tiempo += 0.1f) {
            int horizontal = cruce.color(true, tiempo);
            int vertical = cruce.color(false, tiempo);
            assertFalse(horizontal != Cruce.ROJO && vertical != Cruce.ROJO);
        }
        assertEquals(Cruce.VERDE, cruce.color(true, 0));
        assertEquals(Cruce.AMARILLO, cruce.color(true, 7));
        assertEquals(Cruce.ROJO, cruce.color(true, 9));
        assertEquals(Cruce.VERDE, cruce.color(false, 10));
    }

    public void testRojoDetieneYVerdeLibera() {
        VehiculoTrafico auto = new VehiculoTrafico(30, 20, 1, 1, 0);
        Cruce cruce = new Cruce(30, 10);
        VehiculoTrafico[] autos = {auto};
        Cruce[] cruces = {cruce};
        for (int i = 0; i < 200; i++) {
            auto.actualizar(0.05f, 0, cruces, autos, 1000, 1000, ciudad);
            assertFalse(cruce.contiene(auto.x, auto.z));
        }
        float detenidoZ = auto.z;
        assertEquals(0f, auto.velocidad, 0.01f);
        for (int i = 0; i < 100; i++) {
            auto.actualizar(0.05f, 10, cruces, autos, 1000, 1000, ciudad);
        }
        assertTrue(auto.z > detenidoZ + 5);
    }

    public void testSalidaBloqueadaConVerde() {
        VehiculoTrafico auto = new VehiculoTrafico(30, 20, 1, 1, 0);
        Cruce cruce = new Cruce(30, 10);
        for (int i = 0; i < 200; i++) {
            auto.actualizar(0.05f, 10, new Cruce[] {cruce}, new VehiculoTrafico[] {auto}, 28, 19, ciudad);
            assertFalse(cruce.contiene(auto.x, auto.z));
        }
        assertEquals(0f, auto.velocidad, 0.01f);
    }

    public void testJugadorEnCarrilDetieneYRetirarLoPermiteAvanzar() {
        VehiculoTrafico auto = new VehiculoTrafico(30, 20, 1, 1, 0);
        float jugadorX = auto.x;
        float jugadorZ = auto.z + 8;
        for (int i = 0; i < 120; i++) {
            auto.actualizar(0.05f, 0, new Cruce[0], new VehiculoTrafico[] {auto}, jugadorX, jugadorZ, ciudad);
            assertFalse(Colisiones.autos(auto.x, auto.z, auto.angulo, jugadorX, jugadorZ, ciudad.angulo));
        }
        float detenidoZ = auto.z;
        for (int i = 0; i < 40; i++) {
            auto.actualizar(0.05f, 0, new Cruce[0], new VehiculoTrafico[] {auto}, 1000, 1000, ciudad);
        }
        assertTrue(auto.z > detenidoZ + 2);
    }

    public void testVariasVueltasSinAtravesarManzanas() {
        clase4 juego = new clase4();
        for (int i = 0; i < 16000; i++) {
            juego.tiempoUrbano = i * 0.05f;
            for (VehiculoTrafico auto : juego.trafico) {
                float xAnterior = auto.x;
                float zAnterior = auto.z;
                auto.actualizar(0.05f, juego.tiempoUrbano, juego.cruces, juego.trafico, 1000, 1000, ciudad);
                assertTrue(ciudad.puedeCircular(auto.x, auto.z));
                assertTrue(VehiculoTrafico.distancia(xAnterior, zAnterior, auto.x, auto.z) <= 0.31f);
            }
        }
        for (VehiculoTrafico auto : juego.trafico) {
            assertTrue("El tráfico quedó detenido: " + auto.x + ", " + auto.z, auto.vueltas >= 2);
        }
    }

    public void testAmarilloPermiteFrenarAntesDelCruce() {
        VehiculoTrafico auto = new VehiculoTrafico(30, 20, 1, 1, 0);
        Cruce cruce = new Cruce(30, 10);
        for (int i = 0; i < 200; i++) {
            auto.actualizar(0.05f, 17, new Cruce[] {cruce}, new VehiculoTrafico[] {auto}, 1000, 1000, ciudad);
            assertFalse(cruce.contiene(auto.x, auto.z));
        }
        assertEquals(0f, auto.velocidad, 0.01f);
    }

    public void testAutoDentroDelCruceLoDespejaAunqueCambieARojo() {
        VehiculoTrafico auto = new VehiculoTrafico(30, 20, 1, 1, 0);
        Cruce cruce = new Cruce(30, 10);
        VehiculoTrafico[] autos = {auto};
        Cruce[] cruces = {cruce};
        int cuadros = 0;
        while (!cruce.contiene(auto.x, auto.z) && cuadros++ < 200) {
            auto.actualizar(0.05f, 10, cruces, autos, 1000, 1000, ciudad);
        }
        assertTrue(cruce.contiene(auto.x, auto.z));
        for (int i = 0; i < 100; i++) {
            auto.actualizar(0.05f, 0, cruces, autos, 1000, 1000, ciudad);
        }
        assertFalse(cruce.contiene(auto.x, auto.z));
        assertNull(cruce.ocupante);
    }

    public void testVehiculosEnLaMismaRutaMantienenDistancia() {
        VehiculoTrafico primero = new VehiculoTrafico(30, 30, 1, 0, 0);
        VehiculoTrafico segundo = new VehiculoTrafico(30, 20, 0, 1, 0);
        VehiculoTrafico[] autos = {primero, segundo};
        for (int i = 0; i < 2000; i++) {
            for (VehiculoTrafico auto : autos) {
                auto.actualizar(0.05f, 0, new Cruce[0], autos, 1000, 1000, ciudad);
            }
            assertFalse(Colisiones.autos(primero.x, primero.z, primero.angulo, segundo.x, segundo.z, segundo.angulo));
        }
        assertTrue(primero.vueltas > 0);
        assertTrue(segundo.vueltas > 0);
    }

    public void testMovimientoComparableConDiferentesFrecuencias() {
        VehiculoTrafico lento = new VehiculoTrafico(50, 30, 1, 0, 0);
        VehiculoTrafico rapido = new VehiculoTrafico(50, 30, 1, 0, 0);
        for (int i = 0; i < 400; i++) {
            lento.actualizar(0.05f, 0, new Cruce[0], new VehiculoTrafico[] {lento}, 1000, 1000, ciudad);
        }
        for (int i = 0; i < 2000; i++) {
            rapido.actualizar(0.01f, 0, new Cruce[0], new VehiculoTrafico[] {rapido}, 1000, 1000, ciudad);
        }
        assertTrue(VehiculoTrafico.distancia(lento.x, lento.z, rapido.x, rapido.z) < 0.3f);
    }
}
