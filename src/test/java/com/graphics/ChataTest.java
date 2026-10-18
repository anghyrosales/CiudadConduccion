package com.graphics;

import junit.framework.TestCase;

public class ChataTest extends TestCase {
    public void testEngancheDuranteCurvaYReversa() {
        Chata chata = new Chata(0, 0, 0);
        float x = 0, z = 0, giro = 0;
        for (int i = 0; i < 100; i++) {
            giro += 0.004f;
            x -= Math.sin(giro) * 0.04f;
            z -= Math.cos(giro) * 0.04f;
            chata.aceptar(chata.proponer(x, z, giro));
            comprobarEnganche(chata, x, z, giro);
        }
        assertTrue(Math.abs(chata.angulo - giro) > 0.01f);
        for (int i = 0; i < 10; i++) {
            x += Math.sin(giro) * 0.02f;
            z += Math.cos(giro) * 0.02f;
            chata.aceptar(chata.proponer(x, z, giro));
            comprobarEnganche(chata, x, z, giro);
        }
    }

    private void comprobarEnganche(Chata chata, float x, float z, float giro) {
        assertEquals(x + Math.sin(giro) * Chata.ENGANCHE,
                     chata.x - Math.sin(chata.angulo) * Chata.LANZA, 0.00001);
        assertEquals(z + Math.cos(giro) * Chata.ENGANCHE,
                     chata.z - Math.cos(chata.angulo) * Chata.LANZA, 0.00001);
    }

    public void testColisionPropiaYReinicio() {
        clase2 ciudad = new clase2();
        assertTrue(ciudad.chata.puedeMover(ciudad.chata.proponer(-30, 30, 0), 0, ciudad));

        Chata borde = new Chata(-50, 52, 0);
        assertTrue(ciudad.puedeCircular(-50, 52));
        assertFalse(borde.puedeMover(borde.proponer(-50, 52, 0), 0, ciudad));
        ciudad.chata.aceptar(new float[] {12, 4, 1});
        ciudad.reiniciar();
        comprobarEnganche(ciudad.chata, ciudad.autoX, ciudad.autoZ, ciudad.angulo);
        assertEquals(0f, ciudad.chata.rotacionRuedas, 0f);
    }

    public void testLimiteArticulacionYPropuestaSinEfectos() {
        Chata chata = new Chata(-30, 30, 0);
        float anteriorZ = chata.z;
        chata.proponer(-30, 29, 0.2f);
        assertEquals(anteriorZ, chata.z, 0f);

        for (float giro : new float[] {0, 3.12f, -3.12f}) {
            for (float lado : new float[] {-1, 1}) {
                float x = -30, z = 30;
                float orientacion = giro + lado * Chata.MAX_ARTICULACION;
                chata.reiniciar(x, z, giro);
                chata.x = x + (float) Math.sin(giro) * Chata.ENGANCHE
                          + (float) Math.sin(orientacion) * Chata.LANZA;
                chata.z = z + (float) Math.cos(giro) * Chata.ENGANCHE
                          + (float) Math.cos(orientacion) * Chata.LANZA;
                chata.angulo = orientacion;
                clase2 ciudad = new clase2();
                float inicioX = chata.x, inicioZ = chata.z;
                for (int i = 0; i < 12; i++) {
                    x += (float) Math.sin(giro) * 0.025f;
                    z += (float) Math.cos(giro) * 0.025f;
                    float[] propuesta = chata.proponer(x, z, giro);
                    float diferencia = propuesta[2] - giro;
                    diferencia = (float) Math.atan2(Math.sin(diferencia), Math.cos(diferencia));
                    assertTrue(Math.abs(diferencia) <= Chata.MAX_ARTICULACION + 0.00001f);
                    assertTrue(chata.puedeMover(propuesta, giro, ciudad));
                    chata.aceptar(propuesta);
                    comprobarEnganche(chata, x, z, giro);
                }
                assertTrue(Math.hypot(chata.x - inicioX, chata.z - inicioZ) > 0.2);
            }
        }
    }

    public void testTraficoSeDetieneAntePlataforma() {
        clase4 ciudad = new clase4();
        VehiculoTrafico auto = ciudad.trafico[0];
        float x = auto.x, z = auto.z;
        for (int i = 0; i < 40; i++) {
            auto.actualizar(0.05f, i * 0.05f, ciudad.cruces, ciudad.trafico,
                            1000, 1000, ciudad, new float[][] {{x, z, Chata.RADIO}});
        }
        assertEquals(x, auto.x, 0f);
        assertEquals(z, auto.z, 0f);
        assertEquals(0f, auto.velocidad, 0f);
    }
}
