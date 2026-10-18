package com.graphics;

import static org.lwjgl.glfw.GLFW.*;

public class VerificacionInteraccion extends VerificacionGrafica {
    private int cuadro;

    @Override
    protected void actualizar(float deltaTime) {
        cuadro++;
        if (cuadro == 1) glfwSwapInterval(0);
        if (cuadro == 12) tecla(GLFW_KEY_ENTER);
        if (cuadro == 14) {
            autoX = 50;
            autoZ = -46;
            angulo = 0;
            chata.reiniciar(autoX, autoZ, angulo);
            alturaCamara = 4;
        }
        if (cuadro == 25) {
            autoZ = -50;
            chata.reiniciar(autoX, autoZ, angulo);
            actualizarEntregas(0.05f);
        }
        if (cuadro == 35) tecla(GLFW_KEY_P);
        if (cuadro == 40) {
            int[] r = PanelInfo.rectMenu(ancho, alto, 2);
            glfwSetCursorPos(ventana, r[0] + r[2] / 2.0, alto - r[1] - r[3] / 2.0);
            botonRaton(GLFW_MOUSE_BUTTON_LEFT, GLFW_PRESS);
            botonRaton(GLFW_MOUSE_BUTTON_LEFT, GLFW_RELEASE);
            if (!silenciado) throw new IllegalStateException("Boton de sonido sin respuesta");
        }
        if (cuadro == 45) { tecla(GLFW_KEY_ENTER); alChocar(6); }
        if (cuadro == 55) { reiniciar(); glfwSetWindowSize(ventana, 600, 900); tecla(GLFW_KEY_P); }
        if (cuadro == 62) {
            tecla(GLFW_KEY_ENTER);
            for (float[] destino : DESTINOS) {
                autoX = destino[0]; autoZ = destino[1]; velocidad = 0;
                actualizarEntregas(0.05f);
            }
            choques = 2;
        }
        actualizarPartida(0.05f);
        if (cuadro == 70) glfwSetWindowShouldClose(ventana, true);
    }

    @Override
    protected void dibujarFrame() {
        super.dibujarFrame();
        if (cuadro == 5 || cuadro == 20 || cuadro == 30 || cuadro == 38 || cuadro == 48 || cuadro == 60 || cuadro == 64) {
            capturar("interaccion-" + cuadro + ".png");
        }
    }

    public static void main(String[] args) { new VerificacionInteraccion().run(); }
}
