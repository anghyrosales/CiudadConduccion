package com.graphics;

import static org.lwjgl.glfw.GLFW.*;

public class VerificacionCamara extends VerificacionGrafica {
    private int paso;

    @Override
    protected void actualizar(float deltaTime) {
        paso++;
        if (paso == 1) {
            glfwSwapInterval(0);
            noche = false;
            tecla(GLFW_KEY_H);
        }
        if (paso == 15) {
            botonRaton(GLFW_MOUSE_BUTTON_RIGHT, GLFW_PRESS);
            moverRaton(100, 200);
            moverRaton(100, -100);
        }
        if (paso == 30) {
            moverRaton(100, 600);
        }
        if (paso == 40) {
            moverRaton(200, 600);
        }
        if (paso == 55) {
            botonRaton(GLFW_MOUSE_BUTTON_RIGHT, GLFW_RELEASE);
            tecla(GLFW_KEY_HOME);
        }
        if (paso == 65) {
            tecla(GLFW_KEY_C);
        }
        if (paso == 75) {
            glfwSetWindowShouldClose(ventana, true);
        }
    }

    @Override
    protected void dibujarFrame() {
        super.dibujarFrame();
        if (paso == 10 || paso == 20 || paso == 35 || paso == 45 || paso == 60 || paso == 70) {
            capturar("camara-" + paso + ".png");
        }
    }

    public static void main(String[] args) {
        new VerificacionCamara().run();
        System.out.println("Vistas de cámara verificadas sin errores OpenGL.");
    }
}
