package com.graphics;

import java.util.ArrayList;
import java.util.List;
import static org.lwjgl.opengl.GL33.*;

public class Malla {
    final float[] vertices;
    private int vao;
    private int vbo;

    public Malla(float[] vertices) {
        this.vertices = vertices;
    }

    void iniciar() {
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.BYTES, 0L);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * Float.BYTES, 3L * Float.BYTES);
        glEnableVertexAttribArray(1);
        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    void dibujar() {
        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, vertices.length / 6);
    }

    void limpiar() {
        if (vbo != 0) {
            glDeleteBuffers(vbo);
        }
        if (vao != 0) {
            glDeleteVertexArrays(vao);
        }
    }

    static float[] prisma(float anchoSuperior, float largoSuperior) {
        float ax = anchoSuperior / 2;
        float az = largoSuperior / 2;
        float[][] puntos = {
            {-0.5f, -0.5f, -0.5f}, {0.5f, -0.5f, -0.5f},
            {0.5f, -0.5f, 0.5f}, {-0.5f, -0.5f, 0.5f},
            {-ax, 0.5f, -az}, {ax, 0.5f, -az},
            {ax, 0.5f, az}, {-ax, 0.5f, az}
        };
        int[][] caras = {{0, 3, 2, 1}, {4, 5, 6, 7}, {0, 1, 5, 4},
                         {3, 7, 6, 2}, {0, 4, 7, 3}, {1, 2, 6, 5}};
        List<Float> datos = new ArrayList<>();
        for (int[] cara : caras) {
            triangulo(datos, puntos[cara[0]], puntos[cara[2]], puntos[cara[1]]);
            triangulo(datos, puntos[cara[0]], puntos[cara[3]], puntos[cara[2]]);
        }
        return arreglo(datos);
    }

    static float[] cilindro(int segmentos) {
        List<Float> datos = new ArrayList<>();
        for (int i = 0; i < segmentos; i++) {
            double a = i * Math.PI * 2 / segmentos;
            double b = (i + 1) * Math.PI * 2 / segmentos;
            float y1 = (float) Math.cos(a) * 0.5f;
            float z1 = (float) Math.sin(a) * 0.5f;
            float y2 = (float) Math.cos(b) * 0.5f;
            float z2 = (float) Math.sin(b) * 0.5f;
            float[] p1 = {-0.5f, y1, z1};
            float[] p2 = {0.5f, y1, z1};
            float[] p3 = {0.5f, y2, z2};
            float[] p4 = {-0.5f, y2, z2};
            vertice(datos, p1, 0, y1 * 2, z1 * 2);
            vertice(datos, p2, 0, y1 * 2, z1 * 2);
            vertice(datos, p3, 0, y2 * 2, z2 * 2);
            vertice(datos, p1, 0, y1 * 2, z1 * 2);
            vertice(datos, p3, 0, y2 * 2, z2 * 2);
            vertice(datos, p4, 0, y2 * 2, z2 * 2);
            triangulo(datos, new float[] {-0.5f, 0, 0}, p4, p1);
            triangulo(datos, new float[] {0.5f, 0, 0}, p2, p3);
        }
        return arreglo(datos);
    }

    private static void triangulo(List<Float> datos, float[] a, float[] b, float[] c) {
        float ux = b[0] - a[0];
        float uy = b[1] - a[1];
        float uz = b[2] - a[2];
        float vx = c[0] - a[0];
        float vy = c[1] - a[1];
        float vz = c[2] - a[2];
        float nx = uy * vz - uz * vy;
        float ny = uz * vx - ux * vz;
        float nz = ux * vy - uy * vx;
        float largo = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (largo < 0.00001f) {
            return;
        }
        vertice(datos, a, nx / largo, ny / largo, nz / largo);
        vertice(datos, b, nx / largo, ny / largo, nz / largo);
        vertice(datos, c, nx / largo, ny / largo, nz / largo);
    }

    private static void vertice(List<Float> datos, float[] p, float nx, float ny, float nz) {
        datos.add(p[0]);
        datos.add(p[1]);
        datos.add(p[2]);
        datos.add(nx);
        datos.add(ny);
        datos.add(nz);
    }

    private static float[] arreglo(List<Float> datos) {
        float[] resultado = new float[datos.size()];
        for (int i = 0; i < datos.size(); i++) {
            resultado[i] = datos.get(i);
        }
        return resultado;
    }
}
