package com.graphics;

import static org.lwjgl.glfw.GLFW.*;

public class VerificacionChata extends VerificacionGrafica {
    private int cuadro;

    @Override
    protected void actualizar(float deltaTime) {
        cuadro++;
        if (cuadro == 1) {
            noche = false;
            alturaCamara = 6;
            orbitaSeguimiento = -0.4f;
            tecla(GLFW_KEY_H);
        }
        if (cuadro < 50) velocidad = 4;
        superMovimiento();
        if (cuadro == 60) glfwSetWindowShouldClose(ventana, true);
    }

    private void superMovimiento() {
        float px = autoX - (float) Math.sin(angulo) * 0.04f;
        float pz = autoZ - (float) Math.cos(angulo) * 0.04f;
        float giro = angulo + (cuadro < 30 ? 0.008f : 0);
        float[] propuesta = chata.proponer(px, pz, giro);
        if (puedeCircular(px, pz) && chata.puedeMover(propuesta, giro, this)) {
            autoX = px;
            autoZ = pz;
            angulo = giro;
            chata.aceptar(propuesta);
        }
    }

    @Override
    protected void dibujarFrame() {
        super.dibujarFrame();
        if (cuadro == 45) capturar("chata.png");
    }

    public static void main(String[] args) {
        new VerificacionChata().run();
    }
}
